package com.example.wofi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// מודל שמכיל נתוני משתמש
public class ProfessionalAdapter extends RecyclerView.Adapter<ProfessionalAdapter.ProfessionalViewHolder> {

    private List<User> userList;
    private OnProfessionalClickListener listener;

    public ProfessionalAdapter(List<User> userList) {
        this.userList = userList;
    }

    public void updateList(List<User> newList) {
        this.userList = newList;
        notifyDataSetChanged();
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
        User user = userList.get(position);
        holder.nameText.setText("שם: " + user.username);
        holder.professionText.setText("תחום: " + user.profession);
        holder.phoneText.setText("טלפון: " + user.phone);

        // הפעלת listener בעת לחיצה על השורה
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ProfessionalViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, professionText, phoneText;

        public ProfessionalViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.professional_name);
            professionText = itemView.findViewById(R.id.professional_profession);
            phoneText = itemView.findViewById(R.id.professional_phone);
        }
    }

    public interface OnProfessionalClickListener {
        void onClick(User user);
    }

    public void setOnProfessionalClickListener(OnProfessionalClickListener listener) {
        this.listener = listener;
    }
}
