package com.example.medisync.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medisync.R;
import com.example.medisync.model.Medicine;
import com.example.medisync.model.MedicineIntake;

import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.ViewHolder> {

    private List<MedicineIntake> intakeList;
    private OnIntakeClickListener listener;

    public interface OnIntakeClickListener {
        void onIntakeClick(MedicineIntake intake);
    }

    public MedicineAdapter(List<MedicineIntake> intakeList, OnIntakeClickListener listener) {
        this.intakeList = intakeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine_dashboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicineIntake intake = intakeList.get(position);
        Medicine medicine = intake.getMedicine();
        
        // 1. Set Patient Name (Show if it's for the Doctor)
        if (medicine.getPatientName() != null && !medicine.getPatientName().equals("You")) {
            holder.tvPatientName.setVisibility(View.VISIBLE);
            holder.tvPatientName.setText("Patient: " + medicine.getPatientName());
        } else {
            holder.tvPatientName.setVisibility(View.GONE);
        }

        // 2. Set Intake Time as Primary
        holder.tvIntakeTimePrimary.setText(intake.getIntakeTime());

        // 3. Basic Info (Medicine Name is now secondary)
        holder.tvMedName.setText(medicine.getName());
        String amountText = medicine.getAmount() + " " + (medicine.getUnit() != null ? medicine.getUnit() : "");
        holder.tvMedAmount.setText(amountText);
        holder.tvMedInstruction.setText(medicine.getInstruction());

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

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onIntakeClick(intake);
            }
        });
    }

    @Override
    public int getItemCount() {
        return intakeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedName, tvMedAmount, tvMedInstruction, tvStatus, tvPatientName, tvIntakeTimePrimary;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvIntakeTimePrimary = itemView.findViewById(R.id.tvIntakeTimePrimary);
            tvMedName = itemView.findViewById(R.id.tvMedName);
            tvMedAmount = itemView.findViewById(R.id.tvMedAmount);
            tvMedInstruction = itemView.findViewById(R.id.tvMedInstruction);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
