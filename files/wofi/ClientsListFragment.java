package com.example.wofi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * מסך רשימת הלקוחות
 * מציג את רשימת כל הלקוחות במערכת
 * מאפשר ניווט לפרופיל של כל לקוח
 */
public class ClientsListFragment extends Fragment {

    /** רשימה גלילה של הלקוחות */
    private RecyclerView recyclerView;
    
    /** מתאם להצגת הלקוחות ברשימה */
    private ClientsAdapter adapter;
    
    /** סרגל התקדמות */
    private ProgressBar progressBar;
    
    /** הודעת ריק - מוצגת כשאין לקוחות */
    private TextView emptyView;
    
    /** מופע של Firebase Firestore */
    private FirebaseFirestore db;
    
    /** מופע של Firebase Authentication */
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_clients_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // אתחול מופעי Firebase
        initializeFirebase();

        // אתחול רכיבי הממשק
        initializeViews(view);

        // הגדרת סרגל הכלים
        setupToolbar(view);

        // הגדרת רשימת הלקוחות
        setupRecyclerView(view);

        // טעינת הלקוחות
        loadClients();
    }

    /**
     * מאתחל את מופעי Firebase
     */
    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * מאתחל את רכיבי הממשק ומקשר אותם למשתני המחלקה
     * @param view תצוגת ה-Fragment
     */
    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.clients_recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyView = view.findViewById(R.id.empty_view);
    }

    /**
     * מגדיר את סרגל הכלים ואת כפתור החזרה
     * @param view תצוגת ה-Fragment
     */
    private void setupToolbar(View view) {
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> 
            Navigation.findNavController(view).navigateUp()
        );
    }

    /**
     * מגדיר את רשימת הלקוחות ואת המתאם שלה
     * @param view תצוגת ה-Fragment
     */
    private void setupRecyclerView(View view) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ClientsAdapter(new ArrayList<>(), client -> {
            Bundle args = new Bundle();
            args.putString("userId", client.getUserId());
            Navigation.findNavController(view)
                    .navigate(R.id.action_clientsListFragment_to_clientProfileFragment, args);
        });
        recyclerView.setAdapter(adapter);
    }

    /**
     * טוען את רשימת הלקוחות מ-Firestore
     * מציג סרגל התקדמות בזמן הטעינה
     * מציג הודעת ריק אם אין לקוחות
     */
    private void loadClients() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        // בדיקה שהמשתמש מחובר
        if (mAuth.getCurrentUser() == null) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "יש להתחבר למערכת כדי לצפות ברשימת הלקוחות",
                    Toast.LENGTH_SHORT).show();
            // ניתוב חזרה למסך הבית
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_clientsListFragment_to_homeFragment);
            return;
        }

        String currentUserId = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .whereEqualTo("userType", "לקוח")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> clients = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User client = new User();
                        client.setUserId(document.getId());
                        client.setUsername(document.getString("username"));
                        client.setPhone(document.getString("phone"));
                        clients.add(client);
                    }

                    progressBar.setVisibility(View.GONE);
                    if (clients.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        adapter.updateClients(clients);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "שגיאה בטעינת הלקוחות: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
} 