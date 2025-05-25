package com.example.wofi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProfessionalsListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProfessionalAdapter adapter;
    private Spinner filterSpinner;
    private FirebaseFirestore db;
    private View titleText;
    private NavController navController;

    private final String[] professionOptions = {
            "כל המקצועות", "בייביסיטר", "דוגווקר", "מורה פרטי"
    };

    public ProfessionalsListFragment() {
        super(R.layout.fragment_professionals_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_professionals_list, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.professionals_recycler_view);
        titleText = view.findViewById(R.id.title_text);
        filterSpinner = view.findViewById(R.id.filter_spinner);

        db = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProfessionalAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        adapter.setOnProfessionalClickListener(user -> {
            Bundle bundle = new Bundle();
            bundle.putString("username", user.username);
            bundle.putString("phone", user.phone);
            bundle.putString("profession", user.profession);
            bundle.putString("email", user.email);
            bundle.putString("userId", user.userId);

            if (navController != null) {
                navController.navigate(R.id.action_professionalsListFragment_to_professionalProfileFragment, bundle);
            }
        });

        // Load professionals
        loadProfessionals();

        // הגדרת Spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                professionOptions
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(spinnerAdapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = professionOptions[position];
                loadProfessionals(selected);
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {
                loadProfessionals("כל המקצועות");
            }
        });

        return view;
    }

    // טעינת בעלי מקצוע לפי תחום
    private void loadProfessionals(String filter) {
        db.collection("users")
                .whereEqualTo("userType", "בעל מקצוע")
                .get()
                .addOnSuccessListener(query -> {
                    List<User> filteredList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        User user = doc.toObject(User.class);
                        user.setUserId(doc.getId());  // Set the document ID as the user ID
                        if ("כל המקצועות".equals(filter) || filter.equals(user.profession)) {
                            filteredList.add(user);
                        }
                    }
                    adapter.updateList(filteredList);
                });
    }

    private void loadProfessionals() {
        loadProfessionals("כל המקצועות");
    }
}
