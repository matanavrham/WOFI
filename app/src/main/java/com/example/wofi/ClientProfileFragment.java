package com.example.wofi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

public class ClientProfileFragment extends Fragment {

    private TextView nameTextView;
    private TextView phoneTextView;
    private TextView emailTextView;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_client_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        nameTextView = view.findViewById(R.id.client_name);
        phoneTextView = view.findViewById(R.id.client_phone);
        emailTextView = view.findViewById(R.id.client_email);
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);

        // Set up toolbar
        toolbar.setNavigationOnClickListener(v -> 
            Navigation.findNavController(view).navigateUp()
        );

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get client ID from arguments
        String clientId = getArguments() != null ? getArguments().getString("userId") : null;
        if (clientId != null) {
            loadClientData(clientId);
        } else {
            Toast.makeText(getContext(), "שגיאה בטעינת נתוני לקוח", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigateUp();
        }
    }

    private void loadClientData(String clientId) {
        db.collection("users").document(clientId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String username = documentSnapshot.getString("username");
                    String phone = documentSnapshot.getString("phone");
                    String email = documentSnapshot.getString("email");

                    nameTextView.setText("שם: " + (username != null ? username : "לא צוין"));
                    phoneTextView.setText("טלפון: " + (phone != null ? phone : "לא צוין"));
                    emailTextView.setText("אימייל: " + (email != null ? email : "לא צוין"));
                } else {
                    Toast.makeText(getContext(), "לא נמצאו נתוני לקוח", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "שגיאה בטעינת נתונים: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            });
    }
} 