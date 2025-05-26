package com.example.wofi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * מסך הרשמה לאפליקציה
 * מאפשר למשתמשים חדשים להירשם למערכת
 * תומך בהרשמת לקוחות ובעלי מקצוע (בייביסיטר, דוגווקר, מורה פרטי)
 */
public class SignupFragment extends Fragment {

    /** שדות קלט למשתמש */
    private EditText usernameInput, emailInput, passwordInput, phoneInput, descriptionInput;
    
    /** קבוצות בחירה לסוג משתמש ומקצוע */
    private RadioGroup userTypeGroup, professionGroup;
    
    /** כפתורי רדיו לסוג משתמש */
    private RadioButton customerRadio, professionalRadio;
    
    /** כפתורי רדיו למקצועות */
    private RadioButton babysitterRadio, dogwalkerRadio, teacherRadio;
    
    /** אזור שדות נוספים לבעלי מקצוע */
    private View professionalFields;
    
    /** כפתור הרשמה */
    private Button signupButton;
    
    /** קישור למסך התחברות */
    private TextView loginLink;
    
    /** סרגל התקדמות */
    private ProgressBar progressBar;
    
    /** מופע של Firebase Authentication */
    private FirebaseAuth mAuth;
    
    /** מופע של Firebase Firestore */
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // אתחול מופעי Firebase
        initializeFirebase();

        // אתחול רכיבי הממשק
        initializeViews(view);

        // הגדרת מאזינים
        setupListeners();
    }

    /**
     * מאתחל את מופעי Firebase
     */
    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * מאתחל את רכיבי הממשק ומקשר אותם למשתני המחלקה
     * @param view תצוגת ה-Fragment
     */
    private void initializeViews(View view) {
        usernameInput = view.findViewById(R.id.username_input);
        emailInput = view.findViewById(R.id.email_input);
        passwordInput = view.findViewById(R.id.password_input);
        phoneInput = view.findViewById(R.id.phone_input);
        descriptionInput = view.findViewById(R.id.description_input);
        userTypeGroup = view.findViewById(R.id.user_type_group);
        professionGroup = view.findViewById(R.id.profession_group);
        customerRadio = view.findViewById(R.id.customer_radio);
        professionalRadio = view.findViewById(R.id.professional_radio);
        babysitterRadio = view.findViewById(R.id.babysitter_radio);
        dogwalkerRadio = view.findViewById(R.id.dogwalker_radio);
        teacherRadio = view.findViewById(R.id.teacher_radio);
        professionalFields = view.findViewById(R.id.professional_fields);
        signupButton = view.findViewById(R.id.signup_button);
        loginLink = view.findViewById(R.id.login_link);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    /**
     * מגדיר את מאזיני הלחיצה והשינוי לרכיבים השונים
     */
    private void setupListeners() {
        // מאזין לבחירת סוג משתמש
        userTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            professionalFields.setVisibility(checkedId == R.id.professional_radio ? View.VISIBLE : View.GONE);
        });

        // מאזיני לחיצה
        signupButton.setOnClickListener(v -> signup());
        loginLink.setOnClickListener(v -> 
            Navigation.findNavController(requireView()).navigate(R.id.action_signupFragment_to_loginFragment)
        );
    }

    /**
     * מבצע את תהליך ההרשמה
     * בודק את תקינות הקלט, יוצר משתמש חדש ב-Firebase
     * ושומר את פרטי המשתמש ב-Firestore
     */
    private void signup() {
        // קבלת ערכי הקלט
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        boolean isProfessional = professionalRadio.isChecked();

        // בדיקת תקינות הקלט
        if (!validateInputs(username, email, password, phone, isProfessional, description)) {
            return;
        }

        // הצגת סרגל התקדמות
        showProgress(true);

        // יצירת משתמש ב-Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        saveUserData(userId, username, email, phone, isProfessional, description);
                    } else {
                        handleSignupError(task.getException().getMessage());
                    }
                });
    }

    /**
     * בודק את תקינות הקלט
     * @return true אם כל השדות תקינים, false אחרת
     */
    private boolean validateInputs(String username, String email, String password, 
                                 String phone, boolean isProfessional, String description) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            Toast.makeText(getContext(), "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (isProfessional) {
            if (!babysitterRadio.isChecked() && !dogwalkerRadio.isChecked() && !teacherRadio.isChecked()) {
                Toast.makeText(getContext(), "אנא בחר תחום מקצועי", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (description.isEmpty()) {
                Toast.makeText(getContext(), "אנא הוסף תיאור קצר על עצמך", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    /**
     * שומר את נתוני המשתמש ב-Firestore
     */
    private void saveUserData(String userId, String username, String email, 
                            String phone, boolean isProfessional, String description) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("phone", phone);
        user.put("userType", isProfessional ? "בעל מקצוע" : "לקוח");
        
        if (isProfessional) {
            String profession = getSelectedProfession();
            user.put("profession", profession);
            user.put("description", description);
        }

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    showProgress(false);
                    Toast.makeText(getContext(), "ההרשמה הושלמה בהצלחה", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigate(R.id.action_signupFragment_to_homeFragment);
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    Toast.makeText(getContext(), "שגיאה בשמירת נתוני המשתמש: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * מחזיר את המקצוע שנבחר על ידי המשתמש
     */
    private String getSelectedProfession() {
        if (babysitterRadio.isChecked()) return "בייביסיטר";
        if (dogwalkerRadio.isChecked()) return "דוגווקר";
        if (teacherRadio.isChecked()) return "מורה פרטי";
        return "";
    }

    /**
     * מציג או מסתיר את סרגל ההתקדמות
     */
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        signupButton.setEnabled(!show);
    }

    /**
     * מטפל בשגיאות הרשמה
     */
    private void handleSignupError(String errorMessage) {
        showProgress(false);
        Toast.makeText(getContext(), "שגיאה בהרשמה: " + errorMessage,
                Toast.LENGTH_SHORT).show();
    }
}
