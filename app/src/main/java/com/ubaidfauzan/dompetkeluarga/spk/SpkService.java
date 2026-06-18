
package com.ubaidfauzan.dompetkeluarga.spk;

import com.ubaidfauzan.dompetkeluarga.models.PosKeuangan;
import com.ubaidfauzan.dompetkeluarga.models.PosSummary;
import com.ubaidfauzan.dompetkeluarga.models.UserProfile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpkService {

    // ── METODE 1: Rank Reciprocal ─────────────────────────
    // Rumus: bobot_i = (1/rank_i) / sum(1/rank_j) * 100
    public static List<PosKeuangan> hitungBobotRankReciprocal(List<PosKeuangan> posList) {
        double sumResiprokal = 0;
        for (PosKeuangan pos : posList) {
            sumResiprokal += 1.0 / pos.getPrioritasRank();
        }
        for (PosKeuangan pos : posList) {
            double bobot = (1.0 / pos.getPrioritasRank()) / sumResiprokal * 100.0;
            pos.setBobotPersen(Math.round(bobot * 100.0) / 100.0);
        }
        Collections.sort(posList, Comparator.comparingInt(PosKeuangan::getPrioritasRank));
        return posList;
    }

    // ── METODE 2: SAW (Simple Additive Weighting) ─────────
    // Ranking pos mana yang paling perlu diisi dari sisa anggaran
    public static List<Map.Entry<PosKeuangan, Double>> rankingRedistribusi(
            List<PosSummary> summaries) {
        if (summaries.isEmpty()) return new ArrayList<>();

        final double BOBOT_JARAK = 0.40;
        final double BOBOT_URGENSI = 0.30;
        final double BOBOT_HISTORIS = 0.20;
        final double BOBOT_WAKTU = 0.10;

        Calendar now = Calendar.getInstance();
        int hariDalamBulan = now.getActualMaximum(Calendar.DAY_OF_MONTH);
        int hariSekarang = now.get(Calendar.DAY_OF_MONTH);
        int sisaHari = hariDalamBulan - hariSekarang;

        double maxRank = 1;
        for (PosSummary s : summaries) {
            maxRank = Math.max(maxRank, s.getPos().getPrioritasRank());
        }

        Map<PosKeuangan, Double> skorMap = new HashMap<>();
        for (PosSummary s : summaries) {
            double nilaiJarak = Math.max(0, Math.min(1, 1 - s.getPersenTerpakai()));
            double nilaiUrgensi = 1 - ((s.getPos().getPrioritasRank() - 1.0) / maxRank);
            double nilaiHistoris = s.getPersenTerpakai() < 0.5 ? 0.8 : (s.getPersenTerpakai() < 0.8 ? 0.4 : 0.1);
            double nilaiWaktu = sisaHari <= 3 ? 1.0 : sisaHari <= 7 ? 0.7 : 0.3;

            double skor = (nilaiJarak * BOBOT_JARAK) + (nilaiUrgensi * BOBOT_URGENSI)
                        + (nilaiHistoris * BOBOT_HISTORIS) + (nilaiWaktu * BOBOT_WAKTU);
            skorMap.put(s.getPos(), skor);
        }

        List<Map.Entry<PosKeuangan, Double>> hasil = new ArrayList<>(skorMap.entrySet());
        hasil.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return hasil;
    }

    // ── METODE 3: Rule-Based / Forward Chaining ───────────
    public static HasilEvaluasi evaluasiKeuangan(
            List<PosSummary> summaries,
            double totalPemasukan,
            double totalCicilan,
            UserProfile user) {

        double skor = 100.0;
        List<ReminderItem> reminders = new ArrayList<>();

        double totalPengeluaran = 0;
        for (PosSummary s : summaries) totalPengeluaran += s.getTerpakai();

        // RULE 1: Cicilan > 30% pemasukan
        if (totalPemasukan > 0 && totalCicilan / totalPemasukan > 0.30) {
            skor -= 20;
            int pct = (int) (totalCicilan / totalPemasukan * 100);
            reminders.add(new ReminderItem(
                "Cicilan melebihi batas aman",
                "Cicilan " + pct + "% dari pemasukan. Batas aman 30%.",
                ReminderItem.TIPE_BAHAYA));
        }

        // RULE 2: Evaluasi tiap pos
        for (PosSummary s : summaries) {
            if (s.isMinus()) {
                skor -= 10;
                reminders.add(new ReminderItem(
                    s.getPos().getNama() + " melebihi anggaran",
                    "Sudah minus " + formatRp(Math.abs(s.getSisa())),
                    ReminderItem.TIPE_BAHAYA));
            } else if (s.isHampirHabis()) {
                reminders.add(new ReminderItem(
                    s.getPos().getNama() + " hampir habis",
                    "Sisa " + formatRp(s.getSisa()) + " (" + (int)(s.getPersenTerpakai()*100) + "% terpakai)",
                    ReminderItem.TIPE_PERINGATAN));
            } else if (s.getTerpakai() == 0 && s.getAlokasi() > 0) {
                skor -= 5;
            }
        }

        // RULE 3: Dana darurat kurang
        double targetDarurat = totalPengeluaran * user.getTargetDanaDarurat();
        double danaDarurat = 0;
        for (PosSummary s : summaries) {
            if (s.getPos().getNama().toLowerCase().contains("darurat")) {
                danaDarurat += s.getTerpakai();
            }
        }
        if (danaDarurat < targetDarurat && targetDarurat > 0) {
            skor -= 15;
            reminders.add(new ReminderItem(
                "Dana darurat belum cukup",
                "Target " + user.getTargetDanaDarurat() + "x = " + formatRp(targetDarurat),
                ReminderItem.TIPE_INFO));
        }

        // RULE 4: Sisa pos idle di akhir bulan
        Calendar now = Calendar.getInstance();
        int sisaHari = now.getActualMaximum(Calendar.DAY_OF_MONTH) - now.get(Calendar.DAY_OF_MONTH);
        if (sisaHari <= 5) {
            double totalSisa = 0;
            int posIdle = 0;
            for (PosSummary s : summaries) {
                if (s.getSisa() > s.getAlokasi() * 0.20) {
                    totalSisa += s.getSisa();
                    posIdle++;
                }
            }
            if (posIdle > 0) {
                reminders.add(new ReminderItem(
                    "Ada sisa anggaran " + formatRp(totalSisa),
                    "Dari " + posIdle + " pos. Pertimbangkan alihkan ke investasi.",
                    ReminderItem.TIPE_SARAN));
            }
        }

        // Bonus: target tercapai
        for (PosSummary s : summaries) {
            if (s.getPersenTerpakai() >= 0.95 && s.getPersenTerpakai() <= 1.0) skor += 5;
        }

        skor = Math.max(0, Math.min(100, skor));
        String status = skor >= 70 ? "sehat" : skor >= 40 ? "perhatian" : "kritis";
        return new HasilEvaluasi(skor, status, reminders);
    }

    // ── Distribusi pemasukan tambahan ─────────────────────
    public static Map<Integer, Double> distribusiPemasukanTambahan(
            List<PosKeuangan> posList, double nominal) {
        Map<Integer, Double> hasil = new HashMap<>();
        double totalBobot = 0;
        for (PosKeuangan p : posList) totalBobot += p.getBobotPersen();
        for (PosKeuangan p : posList) {
            double porsi = (p.getBobotPersen() / totalBobot) * nominal;
            hasil.put(p.getId(), (double) Math.round(porsi));
        }
        return hasil;
    }

    public static String formatRp(double v) {
        java.text.NumberFormat formatter = java.text.NumberFormat.getNumberInstance(new java.util.Locale("id", "ID"));
        formatter.setGroupingUsed(true);
        formatter.setMaximumFractionDigits(0);
        return "Rp " + formatter.format(v);
    }
}
