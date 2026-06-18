
package com.ubaidfauzan.dompetkeluarga.models;

public class UserProfile {
    private int id;
    private String nama;
    private int jumlahAnggota;
    private double pemasukanBulanan;
    private String sumberPemasukan;
    private int targetDanaDarurat; // 3, 6, atau 12
    private String pin;

    public UserProfile() {}

    public UserProfile(String nama, int jumlahAnggota, double pemasukanBulanan,
                       String sumberPemasukan, int targetDanaDarurat, String pin) {
        this.nama = nama;
        this.jumlahAnggota = jumlahAnggota;
        this.pemasukanBulanan = pemasukanBulanan;
        this.sumberPemasukan = sumberPemasukan;
        this.targetDanaDarurat = targetDanaDarurat;
        this.pin = pin;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public int getJumlahAnggota() { return jumlahAnggota; }
    public void setJumlahAnggota(int jumlahAnggota) { this.jumlahAnggota = jumlahAnggota; }
    public double getPemasukanBulanan() { return pemasukanBulanan; }
    public void setPemasukanBulanan(double pemasukanBulanan) { this.pemasukanBulanan = pemasukanBulanan; }
    public String getSumberPemasukan() { return sumberPemasukan; }
    public void setSumberPemasukan(String sumberPemasukan) { this.sumberPemasukan = sumberPemasukan; }
    public int getTargetDanaDarurat() { return targetDanaDarurat; }
    public void setTargetDanaDarurat(int targetDanaDarurat) { this.targetDanaDarurat = targetDanaDarurat; }
    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
}
