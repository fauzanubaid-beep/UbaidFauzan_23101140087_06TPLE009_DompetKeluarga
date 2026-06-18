
package com.ubaidfauzan.dompetkeluarga.models;

public class PosKeuangan {
    private int id;
    private String nama;
    private String deskripsi;
    private String iconName;
    private int prioritasRank;
    private double bobotPersen;
    private double targetNominal; // 0 = pakai persen, >0 = nominal tetap
    private String warnaPrimary;
    private int userId;

    public PosKeuangan() {}

    public PosKeuangan(String nama, String deskripsi, String iconName,
                       int prioritasRank, double bobotPersen,
                       double targetNominal, String warnaPrimary, int userId) {
        this.nama = nama;
        this.deskripsi = deskripsi;
        this.iconName = iconName;
        this.prioritasRank = prioritasRank;
        this.bobotPersen = bobotPersen;
        this.targetNominal = targetNominal;
        this.warnaPrimary = warnaPrimary;
        this.userId = userId;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }
    public int getPrioritasRank() { return prioritasRank; }
    public void setPrioritasRank(int prioritasRank) { this.prioritasRank = prioritasRank; }
    public double getBobotPersen() { return bobotPersen; }
    public void setBobotPersen(double bobotPersen) { this.bobotPersen = bobotPersen; }
    public double getTargetNominal() { return targetNominal; }
    public void setTargetNominal(double targetNominal) { this.targetNominal = targetNominal; }
    public String getWarnaPrimary() { return warnaPrimary; }
    public void setWarnaPrimary(String warnaPrimary) { this.warnaPrimary = warnaPrimary; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    // Hitung alokasi berdasarkan mode
    public double getAlokasi(double pemasukanBulanan) {
        return targetNominal > 0 ? targetNominal : (pemasukanBulanan * bobotPersen / 100.0);
    }
}
