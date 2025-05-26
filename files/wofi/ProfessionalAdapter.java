package com.example.wofi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * מתאם להצגת רשימת בעלי המקצוע
 * מנהל את הצגת הנתונים של בעלי המקצוע ברשימה הגלילה
 * ומטפל באירועי לחיצה על פריטים ברשימה
 */
public class ProfessionalAdapter extends RecyclerView.Adapter<ProfessionalAdapter.ProfessionalViewHolder> {

    /** רשימת בעלי המקצוע להצגה */
    private List<User> professionals;
    
    /** מאזין ללחיצות על בעלי מקצוע */
    private OnProfessionalClickListener listener;

    /**
     * ממשק למאזין ללחיצות על בעלי מקצוע
     * מאפשר לטפל באירועי לחיצה על פריטים ברשימה
     */
    public interface OnProfessionalClickListener {
        /**
         * נקרא כאשר המשתמש לוחץ על בעל מקצוע ברשימה
         * @param professional בעל המקצוע שנלחץ
         */
        void onProfessionalClick(User professional);
    }

    /**
     * בנאי - יוצר מתאם חדש
     * @param professionals רשימת בעלי המקצוע להצגה
     */
    public ProfessionalAdapter(List<User> professionals) {
        this.professionals = professionals;
    }

    /**
     * מגדיר מאזין ללחיצות על בעלי מקצוע
     * @param listener המאזין החדש
     */
    public void setOnProfessionalClickListener(OnProfessionalClickListener listener) {
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

    /**
     * מעדכן את רשימת בעלי המקצוע המוצגים
     * @param newList הרשימה החדשה של בעלי המקצוע
     */
    public void updateList(List<User> newList) {
        this.professionals = newList;
        notifyDataSetChanged();
    }

    /**
     * מחזיק תצוגה (ViewHolder) עבור פריט ברשימת בעלי המקצוע
     * מנהל את הצגת הנתונים של בעל מקצוע בודד
     */
    static class ProfessionalViewHolder extends RecyclerView.ViewHolder {
        /** שדה טקסט לשם בעל המקצוע */
        private final TextView nameTextView;
        
        /** שדה טקסט למקצוע */
        private final TextView professionTextView;

        /**
         * בנאי - יוצר מחזיק תצוגה חדש
         * @param itemView תצוגת הפריט
         */
        public ProfessionalViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.professional_name);
            professionTextView = itemView.findViewById(R.id.professional_profession);
        }

        /**
         * מקשר את נתוני בעל המקצוע לתצוגה
         * ומגדיר את מאזין הלחיצה
         * @param professional בעל המקצוע להצגה
         * @param listener מאזין ללחיצות
         */
        public void bind(User professional, OnProfessionalClickListener listener) {
            nameTextView.setText(professional.username);
            professionTextView.setText(professional.profession);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProfessionalClick(professional);
                }
            });
        }
    }
}
