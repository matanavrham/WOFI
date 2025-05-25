package com.example.wofi;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class HomeFragment extends Fragment {

    private TextView welcomeText;
    private TextView subtitleText;
    private MaterialCardView mainActionCard;
    private NavController navController;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final String NEW_PROFESSIONAL_ACTION = "com.example.wofi.NEW_PROFESSIONAL";
    private static final String DAILY_REMINDER_ACTION = "com.example.wofi.DAILY_REMINDER";

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        welcomeText = view.findViewById(R.id.welcome_text);
        subtitleText = view.findViewById(R.id.subtitle_text);
        mainActionCard = view.findViewById(R.id.main_action_card);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // שמירת זמן הכניסה
        Timestamp loginTime = Timestamp.now();

        if (currentUser != null) {
            String uid = currentUser.getUid();

            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            String userType = documentSnapshot.getString("userType");

                            welcomeText.setText("שלום " + username + "!");
                            subtitleText.setText("את/ה " + userType);

                            // Set up the main action card based on user type
                            TextView cardTitle = view.findViewById(R.id.card_title);
                            TextView cardSubtitle = view.findViewById(R.id.card_subtitle);
                            View cardIcon = view.findViewById(R.id.card_icon);

                            if ("לקוח".equals(userType)) {
                                // Customer view
                                cardTitle.setText("מצא בעל מקצוע");
                                cardSubtitle.setText("חפש בעלי מקצוע מומלצים");
                                cardIcon.setBackgroundResource(R.drawable.ic_work);

                                // Set up click listener for professionals list
                                mainActionCard.setOnClickListener(v -> {
                                    if (navController != null) {
                                        navController.navigate(R.id.action_homeFragment_to_professionalsListFragment);
                                    }
                                });

                                // קביעת התזכורת היומית
                                scheduleDailyReminder(requireContext());

                                // מאזין להוספת בעלי מקצוע חדשים
                                FirebaseFirestore.getInstance().collection("users")
                                        .addSnapshotListener((snapshots, error) -> {
                                            if (error != null || snapshots == null) return;

                                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                                if (dc.getType() == DocumentChange.Type.ADDED) {
                                                    String addedUserType = dc.getDocument().getString("userType");
                                                    String addedUsername = dc.getDocument().getString("username");
                                                    Timestamp createdAt = dc.getDocument().getTimestamp("createdAt");

                                                    // התראה רק אם נוסף אחרי שנכנסנו
                                                    if ("בעל מקצוע".equals(addedUserType) &&
                                                            createdAt != null &&
                                                            createdAt.compareTo(loginTime) > 0) {
                                                        showImmediateNewProfessionalNotification(addedUsername);
                                                    }
                                                }
                                            }
                                        });
                            } else {
                                // Professional view
                                cardTitle.setText("לקוחות");
                                cardSubtitle.setText("צפה ברשימת הלקוחות שלך");
                                cardIcon.setBackgroundResource(R.drawable.ic_people);

                                // Set up click listener for clients list
                                mainActionCard.setOnClickListener(v -> {
                                    if (navController != null) {
                                        navController.navigate(R.id.action_homeFragment_to_clientsListFragment);
                                    }
                                });
                            }
                        } else {
                            welcomeText.setText("לא נמצאו נתוני משתמש");
                            subtitleText.setText("");
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "שגיאה בגישה ל-Firestore", Toast.LENGTH_SHORT).show()
                    );

        } else {
            welcomeText.setText("לא מחובר");
            subtitleText.setText("");
        }

        return view;
    }

    // תזכורת יומית בשעה 12:00
    private void scheduleDailyReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(DAILY_REMINDER_ACTION);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Set the alarm to start at 12:00
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // If the time has already passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Schedule the alarm to repeat daily
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

    // התראה מיידית על בעל מקצוע חדש
    private void showImmediateNewProfessionalNotification(String professionalName) {
        String channelId = "new_professional_channel";
        NotificationManager notificationManager =
                (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "New Professional Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra("navigate_to", "professionals");

        PendingIntent pendingIntent = PendingIntent.getActivity(
                requireContext(),
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("בעל מקצוע חדש הצטרף!")
                .setContentText("המשתמש " + professionalName + " נוסף לאפליקציה.")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
