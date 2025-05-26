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
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * מסך רשימת בעלי המקצוע
 * מציג את רשימת כל בעלי המקצוע במערכת
 * מאפשר סינון לפי תחום מקצועי
 * מאפשר ניווט לפרופיל של כל בעל מקצוע
 */
public class ProfessionalsListFragment extends Fragment {

    /** רשימה גלילה של בעלי המקצוע */
    private RecyclerView professionalsRecyclerView;
    
    /** סרגל התקדמות */
    private ProgressBar progressBar;
    
    /** הודעת ריק - מוצגת כשאין בעלי מקצוע */
    private TextView emptyView;
    
    /** מתאם להצגת בעלי המקצוע ברשימה */
    private ProfessionalsAdapter adapter;
    
    /** מופע של Firebase Firestore */
    private FirebaseFirestore db;
    
    /** מופע של Firebase Authentication */
    private FirebaseAuth mAuth;
    
    /** בקר הניווט */
    private NavController navController;

    public ProfessionalsListFragment() {
        super(R.layout.fragment_professionals_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // אתחול רכיבי הממשק
        initializeViews(view);

        // הגדרת סרגל הכלים
        setupToolbar(view);

        // אתחול מופעי Firebase
        initializeFirebase();

        // הגדרת רשימת בעלי המקצוע
        setupRecyclerView();

        // טעינת בעלי המקצוע
        loadProfessionals();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_professionals_list, container, false);
    }

    /**
     * מאתחל את רכיבי הממשק ומקשר אותם למשתני המחלקה
     * @param view תצוגת ה-Fragment
     */
    private void initializeViews(View view) {
        professionalsRecyclerView = view.findViewById(R.id.professionals_recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyView = view.findViewById(R.id.empty_view);
    }

    /**
     * מגדיר את סרגל הכלים ואת כפתור החזרה
     * @param view תצוגת ה-Fragment
     */
    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            if (navController != null) {
                navController.navigateUp();
            }
        });
    }

    /**
     * מאתחל את מופעי Firebase
     */
    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * מגדיר את רשימת בעלי המקצוע ואת המתאם שלה
     */
    private void setupRecyclerView() {
        professionalsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProfessionalsAdapter(new ArrayList<>(), professional -> {
            Bundle bundle = new Bundle();
            bundle.putString("userId", professional.getUserId());
            if (navController != null) {
                navController.navigate(R.id.action_professionalsListFragment_to_professionalProfileFragment, bundle);
            }
        });
        professionalsRecyclerView.setAdapter(adapter);
    }

    /**
     * טוען את רשימת בעלי המקצוע מ-Firestore
     * מציג סרגל התקדמות בזמן הטעינה
     * מציג הודעת ריק אם אין בעלי מקצוע
     */
    private void loadProfessionals() {
        // בדיקה שהמשתמש מחובר
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "יש להתחבר למערכת כדי לצפות ברשימת בעלי המקצוע",
                    Toast.LENGTH_SHORT).show();
            if (navController != null) {
                navController.navigate(R.id.action_professionalsListFragment_to_homeFragment);
            }
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        db.collection("users")
                .whereEqualTo("userType", "בעל מקצוע")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> professionals = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User professional = document.toObject(User.class);
                        professional.setUserId(document.getId());
                        professionals.add(professional);
                    }

                    progressBar.setVisibility(View.GONE);
                    if (professionals.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        adapter.updateList(professionals);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "שגיאה בטעינת בעלי המקצוע: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
