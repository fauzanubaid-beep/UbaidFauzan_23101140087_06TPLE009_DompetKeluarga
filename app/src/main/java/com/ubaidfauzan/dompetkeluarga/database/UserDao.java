
package com.ubaidfauzan.dompetkeluarga.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ubaidfauzan.dompetkeluarga.models.UserProfile;

public class UserDao {
    private final DatabaseHelper dbHelper;

    public UserDao(DatabaseHelper dbHelper) { this.dbHelper = dbHelper; }

    public long insert(UserProfile u) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("nama", u.getNama());
        cv.put("jumlah_anggota", u.getJumlahAnggota());
        cv.put("pemasukan_bulanan", u.getPemasukanBulanan());
        cv.put("sumber_pemasukan", u.getSumberPemasukan());
        cv.put("target_dana_darurat", u.getTargetDanaDarurat());
        cv.put("pin", u.getPin());
        return db.insert("users", null, cv);
    }

    public UserProfile getById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query("users", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        if (c.moveToFirst()) { UserProfile u = fromCursor(c); c.close(); return u; }
        c.close(); return null;
    }

    public UserProfile getByPin(String pin) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query("users", null, "pin=?", new String[]{pin}, null, null, null);
        if (c.moveToFirst()) { UserProfile u = fromCursor(c); c.close(); return u; }
        c.close(); return null;
    }

    public boolean hasUsers() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM users", null);
        c.moveToFirst();
        int count = c.getInt(0);
        c.close();
        return count > 0;
    }

    public void update(UserProfile u) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("nama", u.getNama());
        cv.put("pemasukan_bulanan", u.getPemasukanBulanan());
        cv.put("sumber_pemasukan", u.getSumberPemasukan());
        cv.put("target_dana_darurat", u.getTargetDanaDarurat());
        db.update("users", cv, "id=?", new String[]{String.valueOf(u.getId())});
    }

    private UserProfile fromCursor(Cursor c) {
        UserProfile u = new UserProfile();
        u.setId(c.getInt(c.getColumnIndexOrThrow("id")));
        u.setNama(c.getString(c.getColumnIndexOrThrow("nama")));
        u.setJumlahAnggota(c.getInt(c.getColumnIndexOrThrow("jumlah_anggota")));
        u.setPemasukanBulanan(c.getDouble(c.getColumnIndexOrThrow("pemasukan_bulanan")));
        u.setSumberPemasukan(c.getString(c.getColumnIndexOrThrow("sumber_pemasukan")));
        u.setTargetDanaDarurat(c.getInt(c.getColumnIndexOrThrow("target_dana_darurat")));
        u.setPin(c.getString(c.getColumnIndexOrThrow("pin")));
        return u;
    }
}
