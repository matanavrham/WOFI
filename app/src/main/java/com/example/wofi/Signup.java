package com.example.wofi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class Signup extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_singup, container, false);

        Button loginButton = view.findViewById(R.id.login_nbtn);
        loginButton.setOnClickListener(v -> {
            // יצירת Fragment חדש
            LoginFragment loginFragment = new LoginFragment();

            // החלפת ה-Fragment בתוך ה-FragmentContainerView שב- MainActivity
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, loginFragment); // שימוש ב-ID של המיכל הראשי
            transaction.addToBackStack(null); // מאפשר חזרה אחורה
            transaction.commit();
        });

        return view;
    }
}
