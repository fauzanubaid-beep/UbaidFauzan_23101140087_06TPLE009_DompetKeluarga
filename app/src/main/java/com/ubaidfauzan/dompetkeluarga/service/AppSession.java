
package com.ubaidfauzan.dompetkeluarga.service;

import android.content.Context;
import android.content.SharedPreferences;
import com.ubaidfauzan.dompetkeluarga.database.DatabaseHelper;
import com.ubaidfauzan.dompetkeluarga.database.PosDao;
import com.ubaidfauzan.dompetkeluarga.database.TransaksiDao;
import com.ubaidfauzan.dompetkeluarga.database.UserDao;
import com.ubaidfauzan.dompetkeluarga.models.*;
import com.ubaidfauzan.dompetkeluarga.spk.SpkService;
import com.ubaidfauzan.dompetkeluarga.spk.HasilEvaluasi;
import java.util.*;

// Singleton untuk menyimpan state sesi user (pengganti Provider Flutter)
public class AppSession {
    private static AppSession instance;
    private UserProfile currentUser;
    private List<PosKeuangan> posList = new ArrayList<>();
    private List<Transaksi> transaksiList = new ArrayList<>();
    private List<PosSummary> summaries = new ArrayList<>();
    private HasilEvaluasi evaluasi;

    private DatabaseHelper dbHelper;
    private UserDao userDao;
    private PosDao posDao;
    private TransaksiDao transaksiDao;

    private AppSession() {}

    public static AppSession getInstance() {
        if (instance == null) instance = new AppSession();
        return instance;
    }

    public void init(Context ctx) {
        dbHelper = DatabaseHelper.getInstance(ctx);
        userDao = new UserDao(dbHelper);
        posDao = new PosDao(dbHelper);
        transaksiDao = new TransaksiDao(dbHelper);
    }

    public void setUser(UserProfile user) {
        this.currentUser = user;
        refresh();
    }

    public void refresh() {
        if (currentUser == null) return;
        Calendar now = Calendar.getInstance();
        posList = posDao.getByUser(currentUser.getId());
        transaksiList = transaksiDao.getByBulan(currentUser.getId(), now.get(Calendar.MONTH) + 1, now.get(Calendar.YEAR));
        hitungSummaries(transaksiList);
        runEvaluasi();
    }

    private void hitungSummaries(List<Transaksi> trxList) {
        summaries = new ArrayList<>();
        for (PosKeuangan pos : posList) {
            double alokasi = pos.getTargetNominal() > 0 ? pos.getTargetNominal()
                : currentUser.getPemasukanBulanan() * pos.getBobotPersen() / 100;
            double terpakai = 0;
            for (Transaksi t : trxList)
                if (t.getPosId() == pos.getId() && t.getJenis().equals("pengeluaran"))
                    terpakai += t.getNominal();
            summaries.add(new PosSummary(pos, alokasi, terpakai));
        }
    }

    private void runEvaluasi() {
        if (currentUser == null) return;
        double cicilan = 0;
        for (Transaksi t : transaksiList)
            if (t.getNamaPos().toLowerCase().contains("cicilan"))
                cicilan += t.getNominal();
        evaluasi = SpkService.evaluasiKeuangan(summaries, currentUser.getPemasukanBulanan(), cicilan, currentUser);
    }

    public List<PosSummary> getSummariesBulan(int bulan, int tahun) {
        if (currentUser == null) return new ArrayList<>();
        List<Transaksi> trx = transaksiDao.getByBulan(currentUser.getId(), bulan, tahun);
        List<PosSummary> result = new ArrayList<>();
        for (PosKeuangan pos : posList) {
            double alokasi = pos.getTargetNominal() > 0 ? pos.getTargetNominal()
                : currentUser.getPemasukanBulanan() * pos.getBobotPersen() / 100;
            double terpakai = 0;
            for (Transaksi t : trx)
                if (t.getPosId() == pos.getId() && t.getJenis().equals("pengeluaran"))
                    terpakai += t.getNominal();
            result.add(new PosSummary(pos, alokasi, terpakai));
        }
        return result;
    }

    public void tambahTransaksi(Transaksi t) { transaksiDao.insert(t); refresh(); }

    public void simpanPrioritasBaru(List<PosKeuangan> urutan) {
        List<PosKeuangan> dg = SpkService.hitungBobotRankReciprocal(urutan);
        posDao.updateBatch(dg);
        refresh();
    }

    public void tambahPemasukanTambahan(double nominal, String sumber) {
        Map<Integer, Double> distribusi = SpkService.distribusiPemasukanTambahan(posList, nominal);
        Calendar now = Calendar.getInstance();
        String tanggal = String.format("%d-%02d-%02d",
            now.get(Calendar.YEAR), now.get(Calendar.MONTH)+1, now.get(Calendar.DAY_OF_MONTH));
        for (PosKeuangan pos : posList) {
            Double jumlah = distribusi.get(pos.getId());
            if (jumlah != null && jumlah > 0) {
                transaksiDao.insert(new Transaksi(pos.getId(), pos.getNama(), jumlah,
                    "pemasukan", tanggal, "Distribusi dari: " + sumber, currentUser.getId()));
            }
        }
        currentUser.setPemasukanBulanan(currentUser.getPemasukanBulanan() + nominal);
        userDao.update(currentUser);
        refresh();
    }

    public void tambahPos(PosKeuangan pos) {
        posDao.insert(pos);
        List<PosKeuangan> updated = posDao.getByUser(pos.getUserId());
        List<PosKeuangan> dg = SpkService.hitungBobotRankReciprocal(updated);
        posDao.updateBatch(dg);
        refresh();
    }

    public void updatePos(PosKeuangan pos) { posDao.update(pos); refresh(); }

    public void hapusPos(int posId) {
        posDao.delete(posId);
        if (currentUser != null) {
            List<PosKeuangan> remaining = posDao.getByUser(currentUser.getId());
            for (int i = 0; i < remaining.size(); i++) remaining.get(i).setPrioritasRank(i + 1);
            List<PosKeuangan> dg = SpkService.hitungBobotRankReciprocal(remaining);
            posDao.updateBatch(dg);
        }
        refresh();
    }

    public void inisialisasiPosDefault(int userId) {
        Object[][] defaults = {
            {"Dana Darurat","Buffer keamanan keluarga","shield","#1D9E75",1},
            {"Tabungan","Rekening terpisah","savings","#0F6E56",2},
            {"Keb. Pokok","Makan, listrik, transportasi","home","#D85A30",3},
            {"Pendidikan","Dana anak & pengembangan diri","school","#7F77DD",4},
            {"Investasi","Reksa dana, emas, saham","trending_up","#378ADD",5},
            {"Hiburan","Rekreasi & lifestyle","celebration","#D4537E",6},
            {"Sosial & Sedekah","Zakat, infaq, sumbangan","volunteer","#EF9F27",7},
        };
        double sumResip = 0;
        for (Object[] d : defaults) sumResip += 1.0 / (int) d[4];
        for (Object[] d : defaults) {
            double bobot = (1.0 / (int) d[4]) / sumResip * 100;
            posDao.insert(new PosKeuangan((String)d[0],(String)d[1],(String)d[2],
                (int)d[4], Math.round(bobot*100)/100.0, 0, (String)d[3], userId));
        }
    }

    // ── Login helpers ─────────────────────────────────────
    public static final String PREF_NAME = "dompet_prefs";
    public static final String KEY_USER_ID = "logged_user_id";

    public void saveLoginState(Context ctx, int userId) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_USER_ID, userId).apply();
    }

    public int getSavedUserId(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_USER_ID, -1);
    }

    public void logout(Context ctx) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply();
        currentUser = null; posList.clear(); transaksiList.clear(); summaries.clear();
    }

    // Getters
    public UserProfile getCurrentUser() { return currentUser; }
    public List<PosKeuangan> getPosList() { return posList; }
    public List<Transaksi> getTransaksiList() { return transaksiList; }
    public List<PosSummary> getSummaries() { return summaries; }
    public HasilEvaluasi getEvaluasi() { return evaluasi; }
    public UserDao getUserDao() { return userDao; }
    public PosDao getPosDao() { return posDao; }
    public TransaksiDao getTransaksiDao() { return transaksiDao; }
}
