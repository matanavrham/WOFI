package com.example.wofi;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
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
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

/**
 * מסך הבית של האפליקציה
 * מציג מידע מותאם אישית למשתמש ומאפשר גישה לפונקציות העיקריות
 * תומך בהתראות על בעלי מקצוע חדשים ותזכורות יומיות
 */
public class HomeFragment extends Fragment {

    /** כותרת ברכה למשתמש */
    private TextView welcomeText;
    
    /** כותרת משנה המציגה את סוג המשתמש */
    private TextView subtitleText;
    
    /** כרטיס פעולה ראשי - משתנה בהתאם לסוג המשתמש */
    private MaterialCardView mainActionCard;
    
    /** בקר ניווט - מנהל את המעבר בין המסכים */
    private NavController navController;

    /** מופע של Firebase Authentication */
    private FirebaseAuth mAuth;
    
    /** מופע של Firebase Firestore */
    private FirebaseFirestore db;
    
    /** פעולה להתראה על בעל מקצוע חדש */
    private static final String NEW_PROFESSIONAL_ACTION = "com.example.wofi.NEW_PROFESSIONAL";
    
    /** פעולה לתזכורת יומית */
    private static final String DAILY_REMINDER_ACTION = "com.example.wofi.DAILY_REMINDER";

    /** מזהה ערוץ ההתראות */
    private static final String NOTIFICATION_CHANNEL_ID = "wofi_channel";

    /** כפתור התנתקות */
    private MaterialButton logoutButton;

    /** מנהל ההתראות */
    private NotificationManager notificationManager;

    /**
     * בנאי ריק - נדרש עבור Fragment
     */
    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        
        // בדיקת הרשאות התראות
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // אתחול רכיבי הממשק
        initializeViews(view);

        // אתחול Firebase
        initializeFirebase();

        // טעינת נתוני המשתמש
        loadUserData(view);

        return view;
    }

    /**
     * מאתחל את רכיבי הממשק ומקשר אותם למשתני המחלקה
     * @param view תצוגת ה-Fragment
     */
    private void initializeViews(View view) {
        welcomeText = view.findViewById(R.id.welcome_text);
        subtitleText = view.findViewById(R.id.subtitle_text);
        mainActionCard = view.findViewById(R.id.main_action_card);
        logoutButton = view.findViewById(R.id.logout_button);

        // הגדרת מאזין לכפתור ההתנתקות - מבצע התנתקות וחזרה למסך ההתחברות
        logoutButton.setOnClickListener(v -> {
            if (mAuth != null) {
                try {
                    // ביטול כל ההתראות
                    cancelAllNotifications();
                    
                    mAuth.signOut();
                    if (navController != null) {
                        Toast.makeText(getContext(), "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();
                        navController.navigate(R.id.action_homeFragment_to_loginFragment);
                    } else {
                        Toast.makeText(getContext(), "שגיאה בניווט", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(), "שגיאה בהתנתקות", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "לא מחובר למערכת", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * מאתחל את מופעי Firebase
     */
    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * טוען את נתוני המשתמש מ-Firestore ומציג אותם בממשק
     * @param view תצוגת ה-Fragment
     */
    private void loadUserData(View view) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
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

                            setupMainActionCard(view, userType, loginTime);
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
    }

    /**
     * מגדיר את כרטיס הפעולה הראשי בהתאם לסוג המשתמש
     * @param view תצוגת ה-Fragment
     * @param userType סוג המשתמש (לקוח/בעל מקצוע)
     * @param loginTime זמן הכניסה של המשתמש
     */
    private void setupMainActionCard(View view, String userType, Timestamp loginTime) {
        TextView cardTitle = view.findViewById(R.id.card_title);
        TextView cardSubtitle = view.findViewById(R.id.card_subtitle);
        View cardIcon = view.findViewById(R.id.card_icon);

        if ("לקוח".equals(userType)) {
            setupCustomerCard(cardTitle, cardSubtitle, cardIcon, loginTime);
        } else {
            setupProfessionalCard(cardTitle, cardSubtitle, cardIcon);
        }
    }

    /**
     * מגדיר את כרטיס הפעולה עבור לקוח
     */
    private void setupCustomerCard(TextView cardTitle, TextView cardSubtitle, View cardIcon, Timestamp loginTime) {
        cardTitle.setText("מצא בעל מקצוע");
        cardSubtitle.setText("חפש בעלי מקצוע מומלצים");
        cardIcon.setBackgroundResource(R.drawable.ic_work);

        mainActionCard.setOnClickListener(v -> {
            if (navController != null) {
                navController.navigate(R.id.action_homeFragment_to_professionalsListFragment);
            }
        });

        scheduleDailyReminder(requireContext());
        setupNewProfessionalListener(loginTime);
    }

    /**
     * מגדיר את כרטיס הפעולה עבור בעל מקצוע
     */
    private void setupProfessionalCard(TextView cardTitle, TextView cardSubtitle, View cardIcon) {
        cardTitle.setText("לקוחות");
        cardSubtitle.setText("צפה ברשימת הלקוחות שלך");
        cardIcon.setBackgroundResource(R.drawable.ic_people);

        mainActionCard.setOnClickListener(v -> {
            if (navController != null) {
                navController.navigate(R.id.action_homeFragment_to_clientsListFragment);
            }
        });
    }

    /**
     * מגדיר מאזין להוספת בעלי מקצוע חדשים
     * @param loginTime זמן הכניסה של המשתמש
     */
    private void setupNewProfessionalListener(Timestamp loginTime) {
        FirebaseFirestore.getInstance().collection("users")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            String addedUserType = dc.getDocument().getString("userType");
                            String addedUsername = dc.getDocument().getString("username");
                            Timestamp createdAt = dc.getDocument().getTimestamp("createdAt");

                            if ("בעל מקצוע".equals(addedUserType) &&
                                    createdAt != null &&
                                    createdAt.compareTo(loginTime) > 0) {
                                showImmediateNewProfessionalNotification(addedUsername);
                            }
                        }
                    }
                });
    }

    /**
     * מגדיר תזכורת יומית בשעה 12:00
     * @param context הקונטקסט של האפליקציה
     */
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

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // אם השעה 12:00 כבר עברה היום, נקבע ל-12:00 מחר
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // מגדיר התראה חוזרת כל יום בשעה 12:00
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

    /**
     * מציג התראה מיידית על בעל מקצוע חדש
     * @param professionalName שם בעל המקצוע החדש
     */
    private void showImmediateNewProfessionalNotification(String professionalName) {
        if (notificationManager == null) return;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "WOFI Notifications",
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

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("בעל מקצוע חדש הצטרף!")
                .setContentText("המשתמש " + professionalName + " נוסף לאפליקציה.")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    /**
     * מבטל את כל ההתראות הפעילות
     */
    private void cancelAllNotifications() {
        if (notificationManager != null) {
            // ביטול התראות תזכורת יומית
            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            Intent reminderIntent = new Intent(requireContext(), ReminderReceiver.class);
            reminderIntent.setAction(DAILY_REMINDER_ACTION);
            PendingIntent reminderPendingIntent = PendingIntent.getBroadcast(
                    requireContext(),
                    0,
                    reminderIntent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );
            if (alarmManager != null) {
                alarmManager.cancel(reminderPendingIntent);
            }

            // ביטול התראות בעלי מקצוע חדשים
            notificationManager.cancelAll();
        }
    }
}
