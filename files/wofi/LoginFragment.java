package com.example.wofi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;
import androidx.navigation.Navigation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * מסך התחברות לאפליקציה
 * מאפשר למשתמשים להתחבר למערכת באמצעות דוא"ל וסיסמה
 * כולל אפשרות לניווט למסך ההרשמה
 */
public class LoginFragment extends Fragment {

    /** שדה קלט לכתובת דוא"ל */
    private EditText emailInput;
    
    /** שדה קלט לסיסמה */
    private EditText passwordInput;
    
    /** כפתור התחברות */
    private Button loginButton;
    
    /** מופע של Firebase Authentication */
    private FirebaseAuth mAuth;
    
    /** קישור למסך ההרשמה */
    private TextView signupLink;

    /**
     * בנאי ריק - נדרש עבור Fragment
     */
    public LoginFragment() {
        // קונסטרקטור ריק – חובה לפרגמנט
        super(R.layout.fragment_login);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // קישור בין רכיבי ה-XML למחלקה
        initializeViews(view);

        // התחברות ל-Firebase
        initializeFirebase();

        // מאזין ללחיצה על כפתור LOGIN
        setupClickListeners();

        return view;
    }

    /**
     * מאתחל את רכיבי הממשק ומקשר אותם למשתני המחלקה
     * @param view תצוגת ה-Fragment
     */
    private void initializeViews(View view) {
        emailInput = view.findViewById(R.id.email_input);
        passwordInput = view.findViewById(R.id.password_input);
        loginButton = view.findViewById(R.id.login_button);
        signupLink = view.findViewById(R.id.signup_link);
    }

    /**
     * מאתחל את מופע Firebase Authentication
     */
    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * מגדיר את מאזיני הלחיצה לרכיבים השונים
     */
    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> login());
        signupLink.setOnClickListener(v -> navigateToSignup());
    }

    /**
     * מבצע את תהליך ההתחברות
     * בודק את תקינות הקלט ומנסה להתחבר ל-Firebase
     */
    private void login() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(getContext(), "התחברת בהצלחה!", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(loginButton).navigate(R.id.action_loginFragment_to_homeFragment);
                        } else {
                            Toast.makeText(getContext(),
                                    "ההתחברות נכשלה: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    /**
     * מנווט למסך ההרשמה
     */
    private void navigateToSignup() {
        Navigation.findNavController(loginButton).navigate(R.id.action_loginFragment_to_signupFragment);
    }
}