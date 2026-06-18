
package com.ubaidfauzan.dompetkeluarga.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ubaidfauzan.dompetkeluarga.models.PosKeuangan;
import java.util.ArrayList;
import java.util.List;

public class PosDao {
    private final DatabaseHelper dbHelper;

    public PosDao(DatabaseHelper dbHelper) { this.dbHelper = dbHelper; }

    public long insert(PosKeuangan pos) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = toContentValues(pos);
        return db.insert("pos_keuangan", null, cv);
    }

    public List<PosKeuangan> getByUser(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query("pos_keuangan", null, "user_id=?",
            new String[]{String.valueOf(userId)}, null, null, "prioritas_rank ASC");
        List<PosKeuangan> list = new ArrayList<>();
        while (c.moveToNext()) list.add(fromCursor(c));
        c.close();
        return list;
    }

    public void update(PosKeuangan pos) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.update("pos_keuangan", toContentValues(pos), "id=?",
            new String[]{String.valueOf(pos.getId())});
    }

    public void updateBatch(List<PosKeuangan> list) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (PosKeuangan pos : list)
                db.update("pos_keuangan", toContentValues(pos), "id=?",
                    new String[]{String.valueOf(pos.getId())});
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
    }

    public void delete(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("transaksi", "pos_id=?", new String[]{String.valueOf(id)});
        db.delete("pos_keuangan", "id=?", new String[]{String.valueOf(id)});
    }

    private ContentValues toContentValues(PosKeuangan pos) {
        ContentValues cv = new ContentValues();
        cv.put("nama", pos.getNama());
        cv.put("deskripsi", pos.getDeskripsi());
        cv.put("icon_name", pos.getIconName());
        cv.put("prioritas_rank", pos.getPrioritasRank());
        cv.put("bobot_persen", pos.getBobotPersen());
        cv.put("target_nominal", pos.getTargetNominal());
        cv.put("warna_primary", pos.getWarnaPrimary());
        cv.put("user_id", pos.getUserId());
        return cv;
    }

    private PosKeuangan fromCursor(Cursor c) {
        PosKeuangan pos = new PosKeuangan();
        pos.setId(c.getInt(c.getColumnIndexOrThrow("id")));
        pos.setNama(c.getString(c.getColumnIndexOrThrow("nama")));
        pos.setDeskripsi(c.getString(c.getColumnIndexOrThrow("deskripsi")));
        pos.setIconName(c.getString(c.getColumnIndexOrThrow("icon_name")));
        pos.setPrioritasRank(c.getInt(c.getColumnIndexOrThrow("prioritas_rank")));
        pos.setBobotPersen(c.getDouble(c.getColumnIndexOrThrow("bobot_persen")));
        pos.setTargetNominal(c.getDouble(c.getColumnIndexOrThrow("target_nominal")));
        pos.setWarnaPrimary(c.getString(c.getColumnIndexOrThrow("warna_primary")));
        pos.setUserId(c.getInt(c.getColumnIndexOrThrow("user_id")));
        return pos;
    }
}
