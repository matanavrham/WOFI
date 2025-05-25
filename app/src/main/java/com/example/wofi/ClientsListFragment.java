package com.example.wofi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class ClientsListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProfessionalAdapter adapter;
    private FirebaseFirestore db;
    private View titleText;
    private NavController navController;

    public ClientsListFragment() {
        super(R.layout.fragment_clients_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clients_list, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.clients_recycler_view);
        titleText = view.findViewById(R.id.title_text);

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
                navController.navigate(R.id.action_clientsListFragment_to_professionalProfileFragment, bundle);
            }
        });

        // Load clients
        loadClients();

        return view;
    }

    private void loadClients() {
        db.collection("users")
                .whereEqualTo("userType", "לקוח")
                .get()
                .addOnSuccessListener(query -> {
                    List<User> clientList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        User user = doc.toObject(User.class);
                        user.setUserId(doc.getId());
                        clientList.add(user);
                    }
                    adapter.updateList(clientList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה בטעינת לקוחות: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
    }
} 