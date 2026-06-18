
package com.ubaidfauzan.dompetkeluarga.models;

import java.util.Date;

public class Transaksi {
    private int id;
    private int posId;
    private String namaPos;
    private double nominal;
    private String jenis; // "pengeluaran" | "pemasukan" | "alokasi"
    private String tanggal; // ISO string
    private String catatan;
    private int userId;

    public Transaksi() {}

    public Transaksi(int posId, String namaPos, double nominal,
                     String jenis, String tanggal, String catatan, int userId) {
        this.posId = posId;
        this.namaPos = namaPos;
        this.nominal = nominal;
        this.jenis = jenis;
        this.tanggal = tanggal;
        this.catatan = catatan;
        this.userId = userId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPosId() { return posId; }
    public void setPosId(int posId) { this.posId = posId; }
    public String getNamaPos() { return namaPos; }
    public void setNamaPos(String namaPos) { this.namaPos = namaPos; }
    public double getNominal() { return nominal; }
    public void setNominal(double nominal) { this.nominal = nominal; }
    public String getJenis() { return jenis; }
    public void setJenis(String jenis) { this.jenis = jenis; }
    public String getTanggal() { return tanggal; }
    public void setTanggal(String tanggal) { this.tanggal = tanggal; }
    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
