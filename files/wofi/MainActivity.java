package com.example.wofi;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

/**
 * הפעילות הראשית של האפליקציה
 * מנהלת את הניווט בין המסכים השונים ומטפלת בקישורים עמוקים
 */
public class MainActivity extends AppCompatActivity {
    /** בקר הניווט - מנהל את המעבר בין המסכים */
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // הפעלת תמיכה במסך מלא
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // הגדרת בקר הניווט
        setupNavigation();

        // טיפול בקישורים עמוקים
        handleDeepLinks();

        // טיפול בפערי המערכת (system insets)
        handleSystemInsets();
    }

    /**
     * מגדיר את בקר הניווט ומקשר אותו ל-NavHostFragment
     */
    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }
    }

    /**
     * מטפל בקישורים עמוקים שמגיעים לאפליקציה
     * לדוגמה: ניווט ישיר לרשימת בעלי המקצוע
     */
    private void handleDeepLinks() {
        if (getIntent() != null && "professionals".equals(getIntent().getStringExtra("navigate_to"))) {
            if (navController != null) {
                navController.navigate(R.id.professionalsListFragment);
            }
        }
    }

    /**
     * מטפל בפערי המערכת (system insets)
     * מוודא שהתוכן לא מוסתר על ידי סרגלי המערכת
     */
    private void handleSystemInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
