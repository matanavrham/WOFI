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

public class LoginFragment extends Fragment {

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private TextView signupLink;

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
        emailInput = view.findViewById(R.id.email_input);
        passwordInput = view.findViewById(R.id.password_input);
        loginButton = view.findViewById(R.id.login_button);
        signupLink = view.findViewById(R.id.signup_link);

        // התחברות ל-Firebase
        mAuth = FirebaseAuth.getInstance();

        // מאזין ללחיצה על כפתור LOGIN
        loginButton.setOnClickListener(v -> login());

        signupLink.setOnClickListener(v -> navigateToSignup());

        return view;
    }

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

    private void navigateToSignup() {
        Navigation.findNavController(loginButton).navigate(R.id.action_loginFragment_to_signupFragment);
    }
}