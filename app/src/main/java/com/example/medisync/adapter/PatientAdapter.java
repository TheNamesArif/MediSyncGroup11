package com.example.medisync.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medisync.R;
import com.example.medisync.model.User;

import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    private List<User> patientList;
    private OnPatientClickListener listener;

    public interface OnPatientClickListener {
        void onEditClick(User user);
        void onDeleteClick(User user);
    }

    public PatientAdapter(List<User> patientList, OnPatientClickListener listener) {
        this.patientList = patientList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        User user = patientList.get(position);
        holder.tvPatientName.setText(user.getFullName());
        holder.tvPatientEmail.setText(user.getEmail());
        holder.tvPatientDetails.setText("Age: " + user.getAge() + " | Gender: " + user.getGender());

        holder.btnEditPatient.setOnClickListener(v -> listener.onEditClick(user));
        holder.btnDeletePatient.setOnClickListener(v -> listener.onDeleteClick(user));
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    public static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName, tvPatientEmail, tvPatientDetails;
        ImageButton btnEditPatient, btnDeletePatient;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvPatientEmail = itemView.findViewById(R.id.tvPatientEmail);
            tvPatientDetails = itemView.findViewById(R.id.tvPatientDetails);
            btnEditPatient = itemView.findViewById(R.id.btnEditPatient);
            btnDeletePatient = itemView.findViewById(R.id.btnDeletePatient);
        }
    }
}
