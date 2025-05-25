package com.example.wofi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupFragment extends Fragment {

    private EditText usernameInput, emailInput, phoneInput, passwordInput;
    private Button signupButton;
    private TextView loginLink;
    private RadioGroup userTypeGroup, professionGroup;
    private LinearLayout professionSection;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public SignupFragment() {
        super(R.layout.fragment_singup);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_singup, container, false);

        usernameInput = view.findViewById(R.id.username_input);
        emailInput = view.findViewById(R.id.email_input);
        phoneInput = view.findViewById(R.id.phone_input);
        passwordInput = view.findViewById(R.id.password_input);
        userTypeGroup = view.findViewById(R.id.user_type_group);
        signupButton = view.findViewById(R.id.signup_button);
        loginLink = view.findViewById(R.id.login_link);
        professionGroup = view.findViewById(R.id.profession_group);
        professionSection = view.findViewById(R.id.profession_section);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // הצגת קטע תחום רק אם נבחר בעל מקצוע
        userTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.professional_radio) {
                professionSection.setVisibility(View.VISIBLE);
            } else {
                professionSection.setVisibility(View.GONE);
            }
        });

        // מעבר למסך התחברות
        loginLink.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_signupFragment_to_loginFragment)
        );

        // לחיצה על כפתור הרשמה
        signupButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            int userTypeId = userTypeGroup.getCheckedRadioButtonId();
            if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || userTypeId == -1) {
                Toast.makeText(getContext(), "אנא מלא את כל השדות ובחר סוג משתמש", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton typeBtn = view.findViewById(userTypeId);
            String userType = typeBtn.getText().toString();

            String profession;
            if ("בעל מקצוע".equals(userType)) {
                int professionId = professionGroup.getCheckedRadioButtonId();
                if (professionId == -1) {
                    Toast.makeText(getContext(), "אנא בחר תחום מקצוע", Toast.LENGTH_SHORT).show();
                    return;
                }
                profession = ((RadioButton) view.findViewById(professionId)).getText().toString();
            } else {
                profession = "";
            }

            // יצירת חשבון בפיירבייס
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid = mAuth.getCurrentUser().getUid();

                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("username", username);
                            userMap.put("email", email);
                            userMap.put("phone", phone);
                            userMap.put("userType", userType);
                            userMap.put("profession", profession);
                            userMap.put("createdAt", FieldValue.serverTimestamp()); // ✅ שדה זמן יצירה

                            db.collection("users").document(uid)
                                    .set(userMap)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(getContext(), "נרשמת בהצלחה!", Toast.LENGTH_SHORT).show();
                                        Navigation.findNavController(view).navigate(R.id.action_signupFragment_to_loginFragment);
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(getContext(), "שמירת המשתמש נכשלה: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                    );
                        } else {
                            Toast.makeText(getContext(), "שגיאה: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        return view;
    }
}
