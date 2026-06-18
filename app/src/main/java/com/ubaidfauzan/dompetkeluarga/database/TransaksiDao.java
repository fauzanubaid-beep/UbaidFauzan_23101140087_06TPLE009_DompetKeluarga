
package com.ubaidfauzan.dompetkeluarga.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ubaidfauzan.dompetkeluarga.models.Transaksi;
import java.util.ArrayList;
import java.util.List;

public class TransaksiDao {
    private final DatabaseHelper dbHelper;

    public TransaksiDao(DatabaseHelper dbHelper) { this.dbHelper = dbHelper; }

    public long insert(Transaksi t) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("pos_id", t.getPosId());
        cv.put("nama_pos", t.getNamaPos());
        cv.put("nominal", t.getNominal());
        cv.put("jenis", t.getJenis());
        cv.put("tanggal", t.getTanggal());
        cv.put("catatan", t.getCatatan());
        cv.put("user_id", t.getUserId());
        return db.insert("transaksi", null, cv);
    }

    public List<Transaksi> getByBulan(int userId, int bulan, int tahun) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String mulai = String.format("%d-%02d-01", tahun, bulan);
        String akhir = String.format("%d-%02d-31", tahun, bulan);
        Cursor c = db.query("transaksi", null,
            "user_id=? AND tanggal BETWEEN ? AND ?",
            new String[]{String.valueOf(userId), mulai, akhir},
            null, null, "tanggal DESC");
        List<Transaksi> list = new ArrayList<>();
        while (c.moveToNext()) list.add(fromCursor(c));
        c.close();
        return list;
    }

    public List<Transaksi> getByPos(int posId, int bulan, int tahun) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String mulai = String.format("%d-%02d-01", tahun, bulan);
        String akhir = String.format("%d-%02d-31", tahun, bulan);
        Cursor c = db.query("transaksi", null,
            "pos_id=? AND tanggal BETWEEN ? AND ?",
            new String[]{String.valueOf(posId), mulai, akhir},
            null, null, "tanggal DESC");
        List<Transaksi> list = new ArrayList<>();
        while (c.moveToNext()) list.add(fromCursor(c));
        c.close();
        return list;
    }

    private Transaksi fromCursor(Cursor c) {
        Transaksi t = new Transaksi();
        t.setId(c.getInt(c.getColumnIndexOrThrow("id")));
        t.setPosId(c.getInt(c.getColumnIndexOrThrow("pos_id")));
        t.setNamaPos(c.getString(c.getColumnIndexOrThrow("nama_pos")));
        t.setNominal(c.getDouble(c.getColumnIndexOrThrow("nominal")));
        t.setJenis(c.getString(c.getColumnIndexOrThrow("jenis")));
        t.setTanggal(c.getString(c.getColumnIndexOrThrow("tanggal")));
        t.setCatatan(c.getString(c.getColumnIndexOrThrow("catatan")));
        t.setUserId(c.getInt(c.getColumnIndexOrThrow("user_id")));
        return t;
    }
}
