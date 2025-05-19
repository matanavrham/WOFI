package com.example.wofi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class HomeFragment extends Fragment {

    private TextView welcomeText;
    private Button actionBtn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        welcomeText = view.findViewById(R.id.welcome_text);
        actionBtn = view.findViewById(R.id.action_btn);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // קריאה להגדרת התזכורת (פעם אחת)
        scheduleDailyNotification(requireContext());

        if (currentUser != null) {
            String uid = currentUser.getUid();

            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            String userType = documentSnapshot.getString("userType");

                            welcomeText.setText("שלום " + username + "!\nאתה " + userType);

                            if ("לקוח".equals(userType)) {
                                actionBtn.setText("מצא בעל מקצוע");
                                actionBtn.setOnClickListener(v ->
                                        Toast.makeText(getContext(), "בעתיד: מעבר לרשימת בעלי מקצוע", Toast.LENGTH_SHORT).show()
                                );
                            } else if ("בעל מקצוע".equals(userType)) {
                                actionBtn.setText("צפה בפרופיל");
                                actionBtn.setOnClickListener(v ->
                                        Toast.makeText(getContext(), "בעתיד: מעבר לפרופיל האישי שלך", Toast.LENGTH_SHORT).show()
                                );
                            }
                        } else {
                            welcomeText.setText("לא נמצאו נתוני משתמש");
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "שגיאה בגישה ל-Firestore", Toast.LENGTH_SHORT).show()
                    );

        } else {
            welcomeText.setText("לא מחובר");
        }

        return view;
    }

    // פונקציה לקביעת התראה יומית
    private void scheduleDailyNotification(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        //שנה את השעה כאן לבדיקת התראה מיידית
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }
}
