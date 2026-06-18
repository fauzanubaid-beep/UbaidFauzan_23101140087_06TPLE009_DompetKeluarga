
package com.ubaidfauzan.dompetkeluarga.spk;

public class ReminderItem {
    public static final int TIPE_BAHAYA = 0;
    public static final int TIPE_PERINGATAN = 1;
    public static final int TIPE_INFO = 2;
    public static final int TIPE_SARAN = 3;

    private String judul;
    private String pesan;
    private int tipe;

    public ReminderItem(String judul, String pesan, int tipe) {
        this.judul = judul;
        this.pesan = pesan;
        this.tipe = tipe;
    }

    public String getJudul() { return judul; }
    public String getPesan() { return pesan; }
    public int getTipe() { return tipe; }
}
