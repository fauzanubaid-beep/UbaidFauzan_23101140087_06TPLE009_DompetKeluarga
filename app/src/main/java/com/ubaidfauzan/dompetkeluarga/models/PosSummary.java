
package com.ubaidfauzan.dompetkeluarga.models;

public class PosSummary {
    private PosKeuangan pos;
    private double alokasi;
    private double terpakai;
    private double sisa;

    public PosSummary(PosKeuangan pos, double alokasi, double terpakai) {
        this.pos = pos;
        this.alokasi = alokasi;
        this.terpakai = terpakai;
        this.sisa = alokasi - terpakai;
    }

    public PosKeuangan getPos() { return pos; }
    public double getAlokasi() { return alokasi; }
    public double getTerpakai() { return terpakai; }
    public double getSisa() { return sisa; }

    public float getPersenTerpakai() {
        if (alokasi <= 0) return 0f;
        return (float) Math.min(terpakai / alokasi, 2.0);
    }

    public boolean isMinus() { return sisa < 0; }
    public boolean isHampirHabis() { return getPersenTerpakai() >= 0.8f && !isMinus(); }
}
