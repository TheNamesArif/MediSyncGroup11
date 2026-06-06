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
import java.util.Map;

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

        // Derive an overall status summary from all intake times
        // Shows "ALL TAKEN" if every intake is taken, otherwise shows the first non-taken status
        String overallStatus = getOverallStatus(medicine);
        holder.tvStatus.setText(overallStatus);

        if ("ALL TAKEN".equals(overallStatus)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_card_green);
        } else if (overallStatus.contains("MISSED")) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_card_orange);
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_card_blue);
        }

        holder.btnView.setOnClickListener(v -> listener.onView(medicine));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(medicine));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(medicine));
    }

    private String getOverallStatus(Medicine medicine) {
        Map<String, String> intakeTimes = medicine.getIntakeTimes();
        if (intakeTimes == null || intakeTimes.isEmpty()) return "PENDING";

        boolean allTaken = true;
        boolean anyMissed = false;

        for (String status : intakeTimes.values()) {
            if (!"taken".equalsIgnoreCase(status)) allTaken = false;
            if ("missed".equalsIgnoreCase(status)) anyMissed = true;
        }

        if (allTaken) return "ALL TAKEN";
        if (anyMissed) return "MISSED";
        return "PENDING";
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