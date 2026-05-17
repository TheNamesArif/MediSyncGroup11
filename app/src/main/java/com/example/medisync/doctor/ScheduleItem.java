package com.example.medisync.doctor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScheduleItem {
    private String id;
    private List<Map<String, Object>> medicines;
    private Date createdAt;

    public ScheduleItem(String id, List<Map<String, Object>> medicines, Date createdAt) {
        this.id = id;
        this.medicines = medicines;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Map<String, Object>> getMedicines() {
        return medicines;
    }

    public void setMedicines(List<Map<String, Object>> medicines) {
        this.medicines = medicines;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getFormattedDate() {
        if (createdAt == null) {
            return "No date";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        return sdf.format(createdAt);
    }

    public String getMedicinesSummary() {
        if (medicines == null || medicines.isEmpty()) {
            return "No medicines";
        }
        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < medicines.size(); i++) {
            Map<String, Object> med = medicines.get(i);
            summary.append(i + 1).append(". ").append(med.get("name"));
            if (i < medicines.size() - 1) {
                summary.append("\n");
            }
        }
        return summary.toString();
    }
}
