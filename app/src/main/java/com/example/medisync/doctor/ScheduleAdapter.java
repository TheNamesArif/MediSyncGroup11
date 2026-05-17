package com.example.medisync.doctor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medisync.R;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private List<ScheduleItem> scheduleList;
    private OnViewListener onViewListener;
    private OnEditListener onEditListener;
    private OnDeleteListener onDeleteListener;

    public interface OnViewListener {
        void onView(ScheduleItem scheduleItem);
    }

    public interface OnEditListener {
        void onEdit(ScheduleItem scheduleItem);
    }

    public interface OnDeleteListener {
        void onDelete(String scheduleId);
    }

    public ScheduleAdapter(List<ScheduleItem> scheduleList, OnViewListener onViewListener, 
                          OnEditListener onEditListener, OnDeleteListener onDeleteListener) {
        this.scheduleList = scheduleList;
        this.onViewListener = onViewListener;
        this.onEditListener = onEditListener;
        this.onDeleteListener = onDeleteListener;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        ScheduleItem schedule = scheduleList.get(position);
        holder.tvDate.setText(schedule.getFormattedDate());
        holder.btnView.setOnClickListener(v -> onViewListener.onView(schedule));
        holder.btnEdit.setOnClickListener(v -> onEditListener.onEdit(schedule));
        holder.btnDelete.setOnClickListener(v -> onDeleteListener.onDelete(schedule.getId()));
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        Button btnView;
        Button btnEdit;
        Button btnDelete;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnView = itemView.findViewById(R.id.btnView);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
