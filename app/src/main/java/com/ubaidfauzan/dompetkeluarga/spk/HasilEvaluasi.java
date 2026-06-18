
package com.ubaidfauzan.dompetkeluarga.spk;

import java.util.List;

public class HasilEvaluasi {
    private double skor;
    private String status; // "sehat" | "perhatian" | "kritis"
    private List<ReminderItem> reminders;

    public HasilEvaluasi(double skor, String status, List<ReminderItem> reminders) {
        this.skor = skor;
        this.status = status;
        this.reminders = reminders;
    }

    public double getSkor() { return skor; }
    public String getStatus() { return status; }
    public List<ReminderItem> getReminders() { return reminders; }
}
