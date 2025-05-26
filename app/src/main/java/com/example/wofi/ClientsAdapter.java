package com.example.wofi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * מתאם להצגת רשימת הלקוחות
 * מנהל את הצגת הנתונים של הלקוחות ברשימה הגלילה
 * ומטפל באירועי לחיצה על פריטים ברשימה
 */
public class ClientsAdapter extends RecyclerView.Adapter<ClientsAdapter.ClientViewHolder> {

    /** רשימת הלקוחות להצגה */
    private List<User> clients;
    
    /** מאזין ללחיצות על לקוחות */
    private final OnClientClickListener listener;

    /**
     * ממשק למאזין ללחיצות על לקוחות
     * מאפשר לטפל באירועי לחיצה על פריטים ברשימה
     */
    public interface OnClientClickListener {
        /**
         * נקרא כאשר המשתמש לוחץ על לקוח ברשימה
         * @param client הלקוח שנלחץ
         */
        void onClientClick(User client);
    }

    /**
     * בנאי - יוצר מתאם חדש
     * @param clients רשימת הלקוחות להצגה
     * @param listener מאזין ללחיצות על לקוחות
     */
    public ClientsAdapter(List<User> clients, OnClientClickListener listener) {
        this.clients = clients;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_client, parent, false);
        return new ClientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientViewHolder holder, int position) {
        User client = clients.get(position);
        holder.bind(client, listener);
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }

    /**
     * מעדכן את רשימת הלקוחות המוצגים
     * @param newClients הרשימה החדשה של הלקוחות
     */
    public void updateClients(List<User> newClients) {
        this.clients = newClients;
        notifyDataSetChanged();
    }

    /**
     * מחזיק תצוגה (ViewHolder) עבור פריט ברשימת הלקוחות
     * מנהל את הצגת הנתונים של לקוח בודד
     */
    static class ClientViewHolder extends RecyclerView.ViewHolder {
        /** שדה טקסט לשם הלקוח */
        private final TextView nameTextView;
        
        /** שדה טקסט למספר הטלפון */
        private final TextView phoneTextView;
        
        /** כרטיס החיפוי של הפריט */
        private final MaterialCardView cardView;

        /**
         * בנאי - יוצר מחזיק תצוגה חדש
         * @param itemView תצוגת הפריט
         */
        public ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.client_name);
            phoneTextView = itemView.findViewById(R.id.client_phone);
            cardView = (MaterialCardView) itemView;
        }

        /**
         * מקשר את נתוני הלקוח לתצוגה
         * ומגדיר את מאזין הלחיצה
         * @param client הלקוח להצגה
         * @param listener מאזין ללחיצות
         */
        public void bind(User client, OnClientClickListener listener) {
            nameTextView.setText(client.getUsername());
            phoneTextView.setText(client.getPhone());
            cardView.setOnClickListener(v -> listener.onClientClick(client));
        }
    }
} 