package com.example.wofi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProfessionalsAdapter extends RecyclerView.Adapter<ProfessionalsAdapter.ProfessionalViewHolder> {

    private List<User> professionals;
    private final OnProfessionalClickListener listener;

    public interface OnProfessionalClickListener {
        void onProfessionalClick(User professional);
    }

    public ProfessionalsAdapter(List<User> professionals, OnProfessionalClickListener listener) {
        this.professionals = professionals;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProfessionalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_professional, parent, false);
        return new ProfessionalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfessionalViewHolder holder, int position) {
        User professional = professionals.get(position);
        holder.bind(professional, listener);
    }

    @Override
    public int getItemCount() {
        return professionals.size();
    }

    public void updateList(List<User> newList) {
        this.professionals = newList;
        notifyDataSetChanged();
    }

    static class ProfessionalViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView professionTextView;

        public ProfessionalViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.professional_name);
            professionTextView = itemView.findViewById(R.id.professional_profession);
        }

        public void bind(User professional, OnProfessionalClickListener listener) {
            nameTextView.setText(professional.username);
            professionTextView.setText(professional.profession);
            itemView.setOnClickListener(v -> listener.onProfessionalClick(professional));
        }
    }
} 