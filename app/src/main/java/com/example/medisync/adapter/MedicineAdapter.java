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
        holder.tvMedName.setText(medicine.getName());
        
        // Display Amount and Unit (e.g. 2 Pills or 5.0 ML)
        String amountText = medicine.getAmount() + " " + (medicine.getUnit() != null ? medicine.getUnit() : "");
        holder.tvMedAmount.setText(amountText);
        
        holder.tvMedInstruction.setText(medicine.getInstruction());
        
        // Display all intake times
        if (medicine.getIntakeTimes() != null && !medicine.getIntakeTimes().isEmpty()) {
            StringBuilder times = new StringBuilder("Times: ");
            for (int i = 0; i < medicine.getIntakeTimes().size(); i++) {
                times.append(medicine.getIntakeTimes().get(i));
                if (i < medicine.getIntakeTimes().size() - 1) times.append(", ");
            }
            holder.tvStatus.setText(times.toString());
        } else {
            holder.tvStatus.setText("No Times Set");
        }
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedName, tvMedAmount, tvMedInstruction, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedName = itemView.findViewById(R.id.tvMedName);
            tvMedAmount = itemView.findViewById(R.id.tvMedAmount);
            tvMedInstruction = itemView.findViewById(R.id.tvMedInstruction);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
