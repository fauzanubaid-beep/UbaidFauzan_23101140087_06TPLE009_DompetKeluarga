
package com.ubaidfauzan.dompetkeluarga.fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.*;
import android.widget.*;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ubaidfauzan.dompetkeluarga.R;
import com.ubaidfauzan.dompetkeluarga.activities.TambahPemasukanActivity;
import com.ubaidfauzan.dompetkeluarga.adapters.PosSummaryAdapter;
import com.ubaidfauzan.dompetkeluarga.database.DatabaseHelper;
import com.ubaidfauzan.dompetkeluarga.models.*;
import com.ubaidfauzan.dompetkeluarga.spk.*;
import com.ubaidfauzan.dompetkeluarga.views.CircularScoreView;
import java.util.*;
import com.ubaidfauzan.dompetkeluarga.utils.CurrencyFormatter;

public class BerandaFragment extends Fragment {

    private int userId;
    private RecyclerView rvPos;
    private TextView tvNama, tvSapaan, tvPemasukan, tvPeriode, tvSkor, tvStatus, tvRekSPK;
    private LinearLayout llStatusBadge, llBannerAkhirBulan;
    private ImageView ivStatusIcon;
    private CircularScoreView circularScoreView;

    public BerandaFragment(int userId) { this.userId = userId; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_beranda, container, false);
        tvNama = view.findViewById(R.id.tv_nama);
        tvSapaan = view.findViewById(R.id.tv_sapaan);
        tvPemasukan = view.findViewById(R.id.tv_pemasukan);
        tvSkor = view.findViewById(R.id.tv_skor);
        tvStatus = view.findViewById(R.id.tv_status);
        tvRekSPK = view.findViewById(R.id.tv_rek_spk);
        llStatusBadge = view.findViewById(R.id.ll_status_badge);
        llBannerAkhirBulan = view.findViewById(R.id.ll_banner_akhir_bulan);
        ivStatusIcon = view.findViewById(R.id.iv_status_icon);
        circularScoreView = view.findViewById(R.id.circular_score_view);
        rvPos = view.findViewById(R.id.rv_pos);
        rvPos.setLayoutManager(new LinearLayoutManager(getContext()));
        
        tvPeriode = view.findViewById(R.id.tv_periode);
        
        com.google.android.material.floatingactionbutton.FloatingActionButton fabPemasukan = view.findViewById(R.id.fab_pemasukan);
        fabPemasukan.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            Intent intent = new Intent(getContext(), TambahPemasukanActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        com.google.android.material.floatingactionbutton.FloatingActionButton fabTransaksi = view.findViewById(R.id.fab_transaksi);
        fabTransaksi.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            Intent intent = new Intent(getContext(), com.ubaidfauzan.dompetkeluarga.activities.TambahTransaksiActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });
        
        refresh();
        return view;
    }

    public void refresh() {
        if (getContext() == null) return;
        DatabaseHelper db = DatabaseHelper.getInstance(getContext());
        UserProfile user = db.getUserById(userId);
        if (user == null) return;

        Calendar now = Calendar.getInstance();
        List<Transaksi> trxList = db.getTransaksiBulan(userId, now.get(Calendar.MONTH) + 1, now.get(Calendar.YEAR));
        List<PosKeuangan> posList = db.getPosUser(userId);

        // Hitung PosSummary
        List<PosSummary> summaries = new ArrayList<>();
        for (PosKeuangan pos : posList) {
            double alokasi = pos.getAlokasi(user.getPemasukanBulanan());
            double terpakai = 0;
            for (Transaksi t : trxList) {
                if (t.getPosId() == pos.getId() && t.getJenis().equals("pengeluaran"))
                    terpakai += t.getNominal();
            }
            if (terpakai > 0) {
                summaries.add(new PosSummary(pos, alokasi, terpakai));
            }
        }

        // SPK Evaluasi
        double totalCicilan = 0;
        for (Transaksi t : trxList) {
            if (t.getNamaPos().toLowerCase().contains("cicilan")) totalCicilan += t.getNominal();
        }
        HasilEvaluasi ev = SpkService.evaluasiKeuangan(summaries, user.getPemasukanBulanan(), totalCicilan, user);

        // SAW rekomendasi
        List<Map.Entry<PosKeuangan, Double>> sawRanking = SpkService.rankingRedistribusi(summaries);

        // Update UI
        String[] bulanNama = {"Jan","Feb","Mar","Apr","Mei","Jun","Jul","Ags","Sep","Okt","Nov","Des"};
        String namaPanggilan = user.getNama().split(" ")[0];
        String namaKeluarga = "Keluarga " + namaPanggilan;
        tvNama.setText(namaKeluarga);
        
        int hour = now.get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 11) {
            tvSapaan.setText("Selamat pagi, " + namaKeluarga + "! Semoga hari ini penuh berkah ☀️");
        } else if (hour >= 11 && hour < 15) {
            tvSapaan.setText("Selamat siang! Jangan lupa istirahat ya 😊");
        } else if (hour >= 15 && hour < 19) {
            tvSapaan.setText("Selamat sore, " + namaKeluarga + "! Yuk cek keuangan hari ini.");
        } else {
            tvSapaan.setText("Selamat malam! Sudah catat pengeluaran hari ini? 🌙");
        }
        
        int day = now.get(Calendar.DAY_OF_MONTH);
        llBannerAkhirBulan.setVisibility(day > 25 ? View.VISIBLE : View.GONE);
        
        // Nominal + periode
        tvPemasukan.setText(CurrencyFormatter.format(user.getPemasukanBulanan()));
        tvPeriode.setText(bulanNama[now.get(Calendar.MONTH)] + " " + now.get(Calendar.YEAR));

        int score = (int) ev.getSkor();
        tvSkor.setText(score + "%");
        circularScoreView.setProgress(score);
        tvStatus.setText(ev.getStatus().substring(0, 1).toUpperCase() + ev.getStatus().substring(1));
        
        // Status Colors — semua menggunakan resource warna
        String statusLower = ev.getStatus().toLowerCase();
        if (statusLower.contains("sehat")) {
            // Background hijau transparan menggunakan cat_dana_darurat
            int greenColor = ContextCompat.getColor(getContext(), R.color.cat_dana_darurat);
            int greenBg = androidx.core.graphics.ColorUtils.setAlphaComponent(greenColor, 38); // ~15%
            llStatusBadge.setBackgroundResource(R.drawable.bg_status_sehat);
            tvStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.color_white));
            ivStatusIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.color_white)));
            
            circularScoreView.setColors(greenColor, Color.parseColor("#0F6E56"));
            tvSkor.setTextColor(ContextCompat.getColor(getContext(), R.color.color_white));
        } else if (statusLower.contains("perhatian") || statusLower.contains("waspada")) {
            // Background kuning warning
            int yellowBg = ContextCompat.getColor(getContext(), R.color.color_yellow);
            int yellowBgAlpha = androidx.core.graphics.ColorUtils.setAlphaComponent(yellowBg, 51); // ~20%
            llStatusBadge.setBackgroundResource(R.drawable.bg_status_warning);
            tvStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.color_heading));
            ivStatusIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.color_yellow)));
            
            circularScoreView.setColors(yellowBg, ContextCompat.getColor(getContext(), R.color.color_yellow_dark));
            tvSkor.setTextColor(ContextCompat.getColor(getContext(), R.color.color_yellow_dark));
        } else {
            // Background merah kritis
            int redColor = ContextCompat.getColor(getContext(), R.color.color_red);
            int redBg = androidx.core.graphics.ColorUtils.setAlphaComponent(redColor, 38); // ~15%
            llStatusBadge.setBackgroundTintList(ColorStateList.valueOf(redBg));
            tvStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.color_red));
            ivStatusIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.color_red)));
            
            circularScoreView.setColors(redColor, Color.parseColor("#B32D2D"));
            tvSkor.setTextColor(ContextCompat.getColor(getContext(), R.color.color_white));
        }

        String prioritasName = sawRanking.isEmpty() ? "Tidak ada" : sawRanking.get(0).getKey().getNama();
        if (score >= 80) {
            tvRekSPK.setText("Luar biasa! Keuangan keluarga sangat terjaga bulan ini \uD83C\uDF89"); // 🎉
        } else if (score >= 60) {
            tvRekSPK.setText("Lumayan baik. Prioritaskan isi: " + prioritasName);
        } else if (score >= 40) {
            tvRekSPK.setText("Perlu perhatian. Yuk review pengeluaran minggu ini.");
        } else {
            tvRekSPK.setText("Saatnya evaluasi keuangan bersama keluarga.");
        }

        rvPos.setAdapter(new PosSummaryAdapter(summaries, getContext()));
    }
}
