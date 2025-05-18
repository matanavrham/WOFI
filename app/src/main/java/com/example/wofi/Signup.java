package com.example.wofi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class Signup extends Fragment {

    private EditText usernameInput, emailInput, phoneInput, passwordInput;
    private Button signupButton;
    private TextView loginLink;
    private RadioGroup userTypeGroup;
    private RadioButton selectedUserType;

    public Signup() {
        // Required empty public constructor
        super(R.layout.fragment_singup); // שים לב לשם הנכון של הקובץ
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_singup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // התחברות לרכיבי המסך
        usernameInput = view.findViewById(R.id.username_input);
        emailInput = view.findViewById(R.id.email_input);
        phoneInput = view.findViewById(R.id.phone_input);
        passwordInput = view.findViewById(R.id.password_input);
        signupButton = view.findViewById(R.id.signup_btn);
        loginLink = view.findViewById(R.id.login_nbtn);
        userTypeGroup = view.findViewById(R.id.user_type_group);

        // כפתור מעבר למסך התחברות
        loginLink.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_signup_to_loginFragment));

        // כפתור הרשמה
        signupButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            int selectedId = userTypeGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(getContext(), "יש לבחור סוג משתמש", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedUserType = view.findViewById(selectedId);
            String userType = selectedUserType.getText().toString();

            if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
                return;
            }

            // כאן תוכל להוסיף את הקוד לשמירה ב־Firebase או מעבר למסך הבא
            Toast.makeText(getContext(), "נרשמת בהצלחה כ-" + userType, Toast.LENGTH_SHORT).show();
        });
    }
}
