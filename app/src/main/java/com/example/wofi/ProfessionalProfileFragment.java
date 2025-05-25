package com.example.wofi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfessionalProfileFragment extends Fragment {

    private TextView nameTextView;
    private TextView phoneTextView;
    private TextView professionTextView;
    private TextView emailTextView;
    private TextView callTextView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private static final int CALL_PHONE_PERMISSION_REQUEST = 1;

    public ProfessionalProfileFragment() {
        super(R.layout.fragment_professional_profile);  // קובץ ה-XML שנשתמש בו
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_professional_profile, container, false);

        // Initialize views
        nameTextView = view.findViewById(R.id.profile_name);
        phoneTextView = view.findViewById(R.id.profile_phone);
        professionTextView = view.findViewById(R.id.profile_profession);
        emailTextView = view.findViewById(R.id.profile_email);
        callTextView = view.findViewById(R.id.call_text);

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Get professional data from arguments
        if (getArguments() != null) {
            String username = getArguments().getString("username");
            String phone = getArguments().getString("phone");
            String profession = getArguments().getString("profession");
            String email = getArguments().getString("email");

            // Update UI with professional data
            nameTextView.setText(username != null ? username : "לא צוין");
            professionTextView.setText(profession != null ? profession : "לא צוין");
            phoneTextView.setText(phone != null ? phone : "לא צוין");
            emailTextView.setText(email != null ? email : "לא צוין");
        } else {
            Toast.makeText(getContext(), "שגיאה: לא נמצאו נתוני בעל מקצוע", Toast.LENGTH_SHORT).show();
        }

        // Set up click listener for call text
        callTextView.setOnClickListener(v -> {
            String phoneNumber = phoneTextView.getText().toString();
            if (!phoneNumber.equals("לא צוין")) {
                makePhoneCall(phoneNumber);
            }
        });

        return view;
    }

    private void makePhoneCall(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CALL_PHONE},
                    CALL_PHONE_PERMISSION_REQUEST);
        } else {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        if (requestCode == CALL_PHONE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String phoneNumber = phoneTextView.getText().toString();
                if (!phoneNumber.equals("לא צוין")) {
                    makePhoneCall(phoneNumber);
                }
            } else {
                Toast.makeText(getContext(), "הרשאה נדרשת לביצוע שיחה", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadProfessionalData(String userId) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Update UI with professional data
                    String username = documentSnapshot.getString("username");
                    String phone = documentSnapshot.getString("phone");
                    String profession = documentSnapshot.getString("profession");
                    String email = documentSnapshot.getString("email");

                    // Format the text with proper styling
                    nameTextView.setText(username != null ? username : "לא צוין");
                    professionTextView.setText(profession != null ? profession : "לא צוין");
                    phoneTextView.setText(phone != null ? phone : "לא צוין");
                    emailTextView.setText(email != null ? email : "לא צוין");
                } else {
                    Toast.makeText(getContext(), "לא נמצאו נתוני משתמש", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "שגיאה בטעינת נתונים: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
}
