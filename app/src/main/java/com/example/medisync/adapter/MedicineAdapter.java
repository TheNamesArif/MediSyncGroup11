package com.example.medisync.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medisync.R;
import com.example.medisync.model.Medicine;

import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.ViewHolder> {

    private List<Medicine> medicineList;

    public MedicineAdapter(List<Medicine> medicineList) {
        this.medicineList = medicineList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine_dashboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medicine medicine = medicineList.get(position);
        
        // 1. Set Patient Name (Show if it's for the Doctor)
        if (medicine.getPatientName() != null && !medicine.getPatientName().equals("You")) {
            holder.tvPatientName.setVisibility(View.VISIBLE);
            holder.tvPatientName.setText("Patient: " + medicine.getPatientName());
        } else {
            holder.tvPatientName.setVisibility(View.GONE);
        }

        // 2. Basic Info
        holder.tvMedName.setText(medicine.getName());
        String amountText = medicine.getAmount() + " " + (medicine.getUnit() != null ? medicine.getUnit() : "");
        holder.tvMedAmount.setText(amountText);
        holder.tvMedInstruction.setText(medicine.getInstruction());
        
        // 3. Intake Times
        if (medicine.getIntakeTimes() != null && !medicine.getIntakeTimes().isEmpty()) {
            StringBuilder timesBuilder = new StringBuilder("Times: ");
            for (int i = 0; i < medicine.getIntakeTimes().size(); i++) {
                timesBuilder.append(medicine.getIntakeTimes().get(i));
                if (i < medicine.getIntakeTimes().size() - 1) timesBuilder.append(", ");
            }
            holder.tvIntakeTimes.setText(timesBuilder.toString());
        } else {
            holder.tvIntakeTimes.setText("No Intake Times Set");
        }

        // 4. Status Logic
        String status = medicine.getStatus() != null ? medicine.getStatus().toUpperCase() : "PENDING";
        holder.tvStatus.setText(status);

        if ("TAKEN".equals(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_card_green);
        } else if ("MISSED".equals(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_card_orange); 
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_card_blue);
        }
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedName, tvMedAmount, tvMedInstruction, tvStatus, tvPatientName, tvIntakeTimes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvMedName = itemView.findViewById(R.id.tvMedName);
            tvMedAmount = itemView.findViewById(R.id.tvMedAmount);
            tvMedInstruction = itemView.findViewById(R.id.tvMedInstruction);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvIntakeTimes = itemView.findViewById(R.id.tvIntakeTimes);
        }
    }
}
