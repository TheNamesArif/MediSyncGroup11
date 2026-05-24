package com.example.medisync.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medisync.R;
import com.example.medisync.model.Medicine;

import java.util.List;

public class MedicineHistoryAdapter extends RecyclerView.Adapter<MedicineHistoryAdapter.ViewHolder> {

    private List<Medicine> medicineList;
    private OnMedicineActionListener listener;

    public interface OnMedicineActionListener {
        void onView(Medicine medicine);
        void onEdit(Medicine medicine);
        void onDelete(Medicine medicine);
    }

    public MedicineHistoryAdapter(List<Medicine> medicineList, OnMedicineActionListener listener) {
        this.medicineList = medicineList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medicine medicine = medicineList.get(position);
        
        holder.tvMedName.setText(medicine.getName());
        String amountText = medicine.getAmount() + " " + (medicine.getUnit() != null ? medicine.getUnit() : "");
        holder.tvMedAmount.setText(amountText);
        
        String status = medicine.getStatus() != null ? medicine.getStatus().toUpperCase() : "PENDING";
        holder.tvStatus.setText(status);

        if ("TAKEN".equals(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_card_green);
        } else if ("MISSED".equals(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_card_orange); 
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_card_blue);
        }

        holder.btnView.setOnClickListener(v -> listener.onView(medicine));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(medicine));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(medicine));
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedName, tvMedAmount, tvStatus;
        Button btnView, btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedName = itemView.findViewById(R.id.tvMedName);
            tvMedAmount = itemView.findViewById(R.id.tvMedAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnView = itemView.findViewById(R.id.btnView);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
