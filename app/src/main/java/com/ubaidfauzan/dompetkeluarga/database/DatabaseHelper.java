
package com.ubaidfauzan.dompetkeluarga.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ubaidfauzan.dompetkeluarga.models.PosKeuangan;
import com.ubaidfauzan.dompetkeluarga.models.Transaksi;
import com.ubaidfauzan.dompetkeluarga.models.UserProfile;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "dompet_keluarga.db";
    private static final int DB_VERSION = 1;
    private static DatabaseHelper instance;

    // Nama tabel
    public static final String TABLE_USERS = "users";
    public static final String TABLE_POS = "pos_keuangan";
    public static final String TABLE_TRANSAKSI = "transaksi";
    public static final String TABLE_LAPORAN = "laporan_bulanan";

    public static synchronized DatabaseHelper getInstance(Context ctx) {
        if (instance == null) instance = new DatabaseHelper(ctx.getApplicationContext());
        return instance;
    }

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "nama TEXT NOT NULL," +
            "jumlah_anggota INTEGER DEFAULT 2," +
            "pemasukan_bulanan REAL DEFAULT 0," +
            "sumber_pemasukan TEXT," +
            "target_dana_darurat INTEGER DEFAULT 3," +
            "pin TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_POS + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "nama TEXT NOT NULL," +
            "deskripsi TEXT," +
            "icon_name TEXT," +
            "prioritas_rank INTEGER NOT NULL," +
            "bobot_persen REAL NOT NULL," +
            "target_nominal REAL DEFAULT 0," +
            "warna_primary TEXT," +
            "user_id INTEGER NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_TRANSAKSI + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "pos_id INTEGER NOT NULL," +
            "nama_pos TEXT NOT NULL," +
            "nominal REAL NOT NULL," +
            "jenis TEXT NOT NULL," +
            "tanggal TEXT NOT NULL," +
            "catatan TEXT," +
            "user_id INTEGER NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_LAPORAN + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "bulan INTEGER NOT NULL," +
            "tahun INTEGER NOT NULL," +
            "total_pemasukan REAL DEFAULT 0," +
            "total_pengeluaran REAL DEFAULT 0," +
            "skor_kesehatan REAL DEFAULT 0," +
            "status_kesehatan TEXT," +
            "user_id INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSAKSI);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LAPORAN);
        onCreate(db);
    }

    // ── USERS ────────────────────────────────────────────

    public long insertUser(UserProfile user) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("nama", user.getNama());
        cv.put("jumlah_anggota", user.getJumlahAnggota());
        cv.put("pemasukan_bulanan", user.getPemasukanBulanan());
        cv.put("sumber_pemasukan", user.getSumberPemasukan());
        cv.put("target_dana_darurat", user.getTargetDanaDarurat());
        cv.put("pin", user.getPin());
        return db.insert(TABLE_USERS, null, cv);
    }

    public UserProfile getUserById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_USERS, null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        if (c.moveToFirst()) {
            UserProfile u = cursorToUser(c);
            c.close();
            return u;
        }
        c.close();
        return null;
    }

    public UserProfile getUserByPin(String pin) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_USERS, null, "pin=?", new String[]{pin}, null, null, null);
        if (c.moveToFirst()) {
            UserProfile u = cursorToUser(c);
            c.close();
            return u;
        }
        c.close();
        return null;
    }

    public List<UserProfile> getAllUsers() {
        List<UserProfile> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_USERS, null, null, null, null, null, null);
        while (c.moveToNext()) list.add(cursorToUser(c));
        c.close();
        return list;
    }

    public void updateUser(UserProfile user) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("nama", user.getNama());
        cv.put("pemasukan_bulanan", user.getPemasukanBulanan());
        cv.put("sumber_pemasukan", user.getSumberPemasukan());
        cv.put("target_dana_darurat", user.getTargetDanaDarurat());
        db.update(TABLE_USERS, cv, "id=?", new String[]{String.valueOf(user.getId())});
    }

    private UserProfile cursorToUser(Cursor c) {
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

    // ── POS KEUANGAN ─────────────────────────────────────

    public long insertPos(PosKeuangan pos) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = posToContentValues(pos);
        return db.insert(TABLE_POS, null, cv);
    }

    public List<PosKeuangan> getPosUser(int userId) {
        List<PosKeuangan> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_POS, null, "user_id=?",
            new String[]{String.valueOf(userId)}, null, null, "prioritas_rank ASC");
        while (c.moveToNext()) list.add(cursorToPos(c));
        c.close();
        return list;
    }

    public void updatePos(PosKeuangan pos) {
        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_POS, posToContentValues(pos), "id=?", new String[]{String.valueOf(pos.getId())});
    }

    public void updateBanyakPos(List<PosKeuangan> posList) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (PosKeuangan pos : posList) {
                db.update(TABLE_POS, posToContentValues(pos), "id=?", new String[]{String.valueOf(pos.getId())});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void deletePos(int posId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_POS, "id=?", new String[]{String.valueOf(posId)});
        db.delete(TABLE_TRANSAKSI, "pos_id=?", new String[]{String.valueOf(posId)});
    }

    private ContentValues posToContentValues(PosKeuangan pos) {
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

    private PosKeuangan cursorToPos(Cursor c) {
        PosKeuangan p = new PosKeuangan();
        p.setId(c.getInt(c.getColumnIndexOrThrow("id")));
        p.setNama(c.getString(c.getColumnIndexOrThrow("nama")));
        p.setDeskripsi(c.getString(c.getColumnIndexOrThrow("deskripsi")));
        p.setIconName(c.getString(c.getColumnIndexOrThrow("icon_name")));
        p.setPrioritasRank(c.getInt(c.getColumnIndexOrThrow("prioritas_rank")));
        p.setBobotPersen(c.getDouble(c.getColumnIndexOrThrow("bobot_persen")));
        p.setTargetNominal(c.getDouble(c.getColumnIndexOrThrow("target_nominal")));
        p.setWarnaPrimary(c.getString(c.getColumnIndexOrThrow("warna_primary")));
        p.setUserId(c.getInt(c.getColumnIndexOrThrow("user_id")));
        return p;
    }

    // ── TRANSAKSI ─────────────────────────────────────────

    public long insertTransaksi(Transaksi t) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("pos_id", t.getPosId());
        cv.put("nama_pos", t.getNamaPos());
        cv.put("nominal", t.getNominal());
        cv.put("jenis", t.getJenis());
        cv.put("tanggal", t.getTanggal());
        cv.put("catatan", t.getCatatan());
        cv.put("user_id", t.getUserId());
        return db.insert(TABLE_TRANSAKSI, null, cv);
    }

    public List<Transaksi> getTransaksiBulan(int userId, int bulan, int tahun) {
        List<Transaksi> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String mulai = String.format("%04d-%02d-01", tahun, bulan);
        String akhir = String.format("%04d-%02d-31", tahun, bulan);
        Cursor c = db.query(TABLE_TRANSAKSI, null,
            "user_id=? AND tanggal BETWEEN ? AND ?",
            new String[]{String.valueOf(userId), mulai, akhir},
            null, null, "tanggal DESC");
        while (c.moveToNext()) {
            Transaksi t = new Transaksi();
            t.setId(c.getInt(c.getColumnIndexOrThrow("id")));
            t.setPosId(c.getInt(c.getColumnIndexOrThrow("pos_id")));
            t.setNamaPos(c.getString(c.getColumnIndexOrThrow("nama_pos")));
            t.setNominal(c.getDouble(c.getColumnIndexOrThrow("nominal")));
            t.setJenis(c.getString(c.getColumnIndexOrThrow("jenis")));
            t.setTanggal(c.getString(c.getColumnIndexOrThrow("tanggal")));
            t.setCatatan(c.getString(c.getColumnIndexOrThrow("catatan")));
            t.setUserId(c.getInt(c.getColumnIndexOrThrow("user_id")));
            list.add(t);
        }
        c.close();
        return list;
    }

    public List<Transaksi> getTransaksiByPos(int userId, int posId, int bulan, int tahun) {
        List<Transaksi> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String mulai = String.format("%04d-%02d-01", tahun, bulan);
        String akhir = String.format("%04d-%02d-31", tahun, bulan);
        Cursor c = db.query(TABLE_TRANSAKSI, null,
            "user_id=? AND pos_id=? AND tanggal BETWEEN ? AND ?",
            new String[]{String.valueOf(userId), String.valueOf(posId), mulai, akhir},
            null, null, "tanggal DESC");
        while (c.moveToNext()) {
            Transaksi t = new Transaksi();
            t.setId(c.getInt(c.getColumnIndexOrThrow("id")));
            t.setPosId(c.getInt(c.getColumnIndexOrThrow("pos_id")));
            t.setNamaPos(c.getString(c.getColumnIndexOrThrow("nama_pos")));
            t.setNominal(c.getDouble(c.getColumnIndexOrThrow("nominal")));
            t.setJenis(c.getString(c.getColumnIndexOrThrow("jenis")));
            t.setTanggal(c.getString(c.getColumnIndexOrThrow("tanggal")));
            t.setCatatan(c.getString(c.getColumnIndexOrThrow("catatan")));
            t.setUserId(c.getInt(c.getColumnIndexOrThrow("user_id")));
            list.add(t);
        }
        c.close();
        return list;
    }
}
