
package com.ubaidfauzan.dompetkeluarga.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.Fragment;
import com.ubaidfauzan.dompetkeluarga.R;
import com.ubaidfauzan.dompetkeluarga.database.DatabaseHelper;
import com.ubaidfauzan.dompetkeluarga.models.*;
import com.ubaidfauzan.dompetkeluarga.spk.SpkService;
import com.ubaidfauzan.dompetkeluarga.service.PdfService;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.*;
import com.ubaidfauzan.dompetkeluarga.utils.CurrencyFormatter;

public class LaporanFragment extends Fragment {

    private int userId;
    private int bulan, tahun;
    private TextView tvBulanTahun, tvPemasukan, tvKeluar, tvSisa;
    private TextView tvInsightSurplus, tvInsightPengeluaran, tvInsightAlokasi;
    private TextView tvSurplusLabel;
    private LinearLayout llSurplusCard;
    private PieChart pieChart;
    private LinearLayout customBarChartContainer;

    public LaporanFragment(int userId) {
        this.userId = userId;
        Calendar now = Calendar.getInstance();
        this.bulan = now.get(Calendar.MONTH) + 1;
        this.tahun = now.get(Calendar.YEAR);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_laporan, container, false);

        tvBulanTahun = view.findViewById(R.id.tv_bulan_tahun);
        tvPemasukan = view.findViewById(R.id.tv_total_pemasukan);
        tvKeluar = view.findViewById(R.id.tv_total_keluar);
        tvSisa = view.findViewById(R.id.tv_total_sisa);
        tvInsightSurplus = view.findViewById(R.id.tv_insight_surplus);
        tvInsightPengeluaran = view.findViewById(R.id.tv_insight_pengeluaran);
        tvInsightAlokasi = view.findViewById(R.id.tv_insight_alokasi);
        pieChart = view.findViewById(R.id.pie_chart);
        customBarChartContainer = view.findViewById(R.id.custom_bar_chart_container);
        llSurplusCard = view.findViewById(R.id.ll_surplus_card);
        tvSurplusLabel = view.findViewById(R.id.tv_surplus_label);

        view.findViewById(R.id.btn_prev).setOnClickListener(v -> {
            if (bulan == 1) { bulan = 12; tahun--; } else bulan--;
            refresh();
        });
        view.findViewById(R.id.btn_next).setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            if (!(bulan == now.get(Calendar.MONTH)+1 && tahun == now.get(Calendar.YEAR))) {
                if (bulan == 12) { bulan = 1; tahun++; } else bulan++;
                refresh();
            }
        });
        view.findViewById(R.id.btn_export_pdf).setOnClickListener(v -> exportPdf());

        refresh();
        return view;
    }

    private void refresh() {
        if (getContext() == null) return;
        DatabaseHelper db = DatabaseHelper.getInstance(getContext());
        UserProfile user = db.getUserById(userId);
        if (user == null) return;

        List<Transaksi> trxList = db.getTransaksiBulan(userId, bulan, tahun);
        List<PosKeuangan> posList = db.getPosUser(userId);
        List<PosSummary> summaries = new ArrayList<>();

        for (PosKeuangan pos : posList) {
            double alokasi = pos.getAlokasi(user.getPemasukanBulanan());
            double terpakai = 0;
            for (Transaksi t : trxList) {
                if (t.getPosId() == pos.getId() && t.getJenis().equals("pengeluaran"))
                    terpakai += t.getNominal();
            }
            summaries.add(new PosSummary(pos, alokasi, terpakai));
        }

        String[] namaBulan = {"","Januari","Februari","Maret","April","Mei","Juni",
            "Juli","Agustus","September","Oktober","November","Desember"};
        tvBulanTahun.setText(namaBulan[bulan] + " " + tahun);

        double totalKeluar = 0;
        for (PosSummary s : summaries) totalKeluar += s.getTerpakai();
        double totalSisa = user.getPemasukanBulanan() - totalKeluar;

        tvPemasukan.setText(CurrencyFormatter.format(user.getPemasukanBulanan()));
        tvKeluar.setText(CurrencyFormatter.format(totalKeluar));
        tvSisa.setText((totalSisa >= 0 ? "" : "-") + CurrencyFormatter.format(Math.abs(totalSisa)));

        // Dynamic surplus/deficit background
        if (totalSisa >= 0) {
            llSurplusCard.setBackgroundResource(R.drawable.bg_green_card);
            tvSurplusLabel.setText("Surplus");
        } else {
            llSurplusCard.setBackgroundResource(R.drawable.bg_red_card);
            tvSurplusLabel.setText("Defisit");
        }
        
        // Insight Surplus
        double surplusPersen = (totalSisa / user.getPemasukanBulanan()) * 100;
        if (surplusPersen > 80) {
            tvInsightSurplus.setText("Bulan ini sangat hemat! Surplus " + CurrencyFormatter.format(totalSisa) + " — luar biasa \uD83C\uDF89"); // 🎉
        } else if (surplusPersen >= 50) {
            tvInsightSurplus.setText("Keuangan sehat. Surplus bulan ini " + CurrencyFormatter.format(totalSisa) + ".");
        } else if (surplusPersen >= 20) {
            tvInsightSurplus.setText("Cukup baik, tapi masih bisa lebih dihemat.");
        } else {
            tvInsightSurplus.setText("Pengeluaran bulan ini cukup besar. Perlu dievaluasi.");
        }

        updatePieChart(summaries);
        updateBarChart(summaries);
        updateInsights(summaries);
    }
    
    private void updateInsights(List<PosSummary> summaries) {
        if (summaries.isEmpty()) {
            tvInsightPengeluaran.setVisibility(View.GONE);
            tvInsightAlokasi.setVisibility(View.GONE);
            return;
        }
        tvInsightPengeluaran.setVisibility(View.VISIBLE);
        tvInsightAlokasi.setVisibility(View.VISIBLE);

        PosSummary maxPengeluaran = null;
        PosSummary minPersen = null;
        PosSummary maxPersen = null;

        for (PosSummary s : summaries) {
            if (maxPengeluaran == null || s.getTerpakai() > maxPengeluaran.getTerpakai()) {
                maxPengeluaran = s;
            }
            if (minPersen == null || s.getPersenTerpakai() < minPersen.getPersenTerpakai()) {
                minPersen = s;
            }
            if (maxPersen == null || s.getPersenTerpakai() > maxPersen.getPersenTerpakai()) {
                maxPersen = s;
            }
        }

        if (maxPengeluaran != null && maxPengeluaran.getTerpakai() > 0) {
            tvInsightPengeluaran.setText("Pengeluaran terbesar: " + maxPengeluaran.getPos().getNama() + " " + CurrencyFormatter.format(maxPengeluaran.getTerpakai()) + " — pertimbangkan untuk dikurangi bulan depan.");
        } else {
            tvInsightPengeluaran.setText("Belum ada pengeluaran bulan ini.");
        }

        if (minPersen != null && maxPersen != null && minPersen != maxPersen && maxPersen.getTerpakai() > 0) {
            tvInsightAlokasi.setText("Pos " + minPersen.getPos().getNama() + " paling irit bulan ini. Pos " + maxPersen.getPos().getNama() + " paling banyak terpakai.");
        } else if (maxPersen != null && maxPersen.getTerpakai() > 0) {
            tvInsightAlokasi.setText("Pos " + maxPersen.getPos().getNama() + " paling banyak terpakai.");
        } else {
            tvInsightAlokasi.setText("Belum ada alokasi yang terpakai.");
        }
    }

    private void updatePieChart(List<PosSummary> summaries) {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        
        List<PosSummary> activeSummaries = new ArrayList<>();
        for (PosSummary s : summaries) {
            if (s.getTerpakai() > 0) activeSummaries.add(s);
        }
        Collections.sort(activeSummaries, new Comparator<PosSummary>() {
            @Override
            public int compare(PosSummary o1, PosSummary o2) {
                return Double.compare(o2.getTerpakai(), o1.getTerpakai());
            }
        });

        for (PosSummary s : activeSummaries) {
            entries.add(new PieEntry((float) s.getTerpakai(), s.getPos().getNama()));
            try {
                colors.add(Color.parseColor(s.getPos().getWarnaPrimary()));
            } catch (Exception e) {
                colors.add(Color.parseColor("#1D9E75"));
            }
        }
        if (entries.isEmpty()) { pieChart.setVisibility(View.GONE); return; }
        pieChart.setVisibility(View.VISIBLE);
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setDrawValues(false);
        pieChart.setData(new PieData(dataSet));
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setEntryLabelColor(Color.parseColor("#1A1A2E"));
        pieChart.setEntryLabelTextSize(11f);
        pieChart.invalidate();
    }

    private class CustomBarView extends View {
        private float terpakaiPercent;
        private android.graphics.Paint bgPaint;
        private android.graphics.Paint fgPaint;
        private android.graphics.RectF rectF;

        public CustomBarView(android.content.Context context, float terpakaiPercent, int barColor) {
            super(context);
            this.terpakaiPercent = terpakaiPercent;
            bgPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            bgPaint.setColor(Color.parseColor("#E8E8EE")); // Light gray (alokasi/kosong)
            fgPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            fgPaint.setColor(barColor); // Per-pos color (terpakai)
            rectF = new android.graphics.RectF();
        }

        @Override
        protected void onDraw(android.graphics.Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            float radius = w / 2f;
            
            // Draw background bar
            rectF.set(0, 0, w, h);
            canvas.drawRoundRect(rectF, radius, radius, bgPaint);
            
            // Draw foreground bar
            float fgHeight = h * terpakaiPercent;
            if (fgHeight > 0) {
                rectF.set(0, h - fgHeight, w, h);
                canvas.drawRoundRect(rectF, radius, radius, fgPaint);
            }
        }
    }

    private void updateBarChart(List<PosSummary> summaries) {
        customBarChartContainer.removeAllViews();
        if (summaries.isEmpty()) return;

        double maxVal = 0;
        for (PosSummary s : summaries) {
            double h = Math.max(s.getAlokasi(), s.getTerpakai());
            if (h > maxVal) maxVal = h;
        }
        if (maxVal == 0) maxVal = 1;

        List<PosSummary> sortedSummaries = new ArrayList<>(summaries);
        Collections.sort(sortedSummaries, new Comparator<PosSummary>() {
            @Override
            public int compare(PosSummary o1, PosSummary o2) {
                return Integer.compare(o1.getPos().getPrioritasRank(), o2.getPos().getPrioritasRank());
            }
        });

        int barWidth = (int) (18 * getResources().getDisplayMetrics().density);
        int marginHorizontal = (int) (10 * getResources().getDisplayMetrics().density);
        int dp8 = (int) (8 * getResources().getDisplayMetrics().density);

        for (PosSummary s : sortedSummaries) {
            double barMax = Math.max(s.getAlokasi(), s.getTerpakai());
            if (barMax == 0) barMax = 0.01;
            float terpakaiPercent = (float) (s.getTerpakai() / barMax);
            if (terpakaiPercent > 1f) terpakaiPercent = 1f;

            // Parse per-pos color
            int posColor;
            try {
                posColor = Color.parseColor(s.getPos().getWarnaPrimary());
            } catch (Exception e) {
                posColor = Color.parseColor("#1D9E75");
            }

            LinearLayout itemContainer = new LinearLayout(getContext());
            itemContainer.setOrientation(LinearLayout.VERTICAL);
            itemContainer.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            itemParams.setMargins(marginHorizontal, 0, marginHorizontal, 0);
            itemContainer.setLayoutParams(itemParams);

            LinearLayout barWrapper = new LinearLayout(getContext());
            barWrapper.setOrientation(LinearLayout.VERTICAL);
            barWrapper.setWeightSum((float) maxVal);
            barWrapper.setGravity(Gravity.BOTTOM);
            LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(barWidth, 0);
            wrapperParams.weight = 1;
            wrapperParams.bottomMargin = dp8;
            barWrapper.setLayoutParams(wrapperParams);

            CustomBarView customBar = new CustomBarView(getContext(), terpakaiPercent, posColor);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(barWidth, 0);
            barParams.weight = (float) barMax;
            customBar.setLayoutParams(barParams);
            
            barWrapper.addView(customBar);

            TextView tvRank = new TextView(getContext());
            tvRank.setText(String.valueOf(s.getPos().getPrioritasRank()));
            tvRank.setTextColor(Color.parseColor("#4A4A6A"));
            tvRank.setTextSize(12f);
            tvRank.setGravity(Gravity.CENTER);

            itemContainer.addView(barWrapper);
            itemContainer.addView(tvRank);

            customBarChartContainer.addView(itemContainer);
        }
    }

    private void exportPdf() {
        if (getContext() == null) return;
        DatabaseHelper db = DatabaseHelper.getInstance(getContext());
        UserProfile user = db.getUserById(userId);
        List<Transaksi> trxList = db.getTransaksiBulan(userId, bulan, tahun);
        List<PosKeuangan> posList = db.getPosUser(userId);
        List<PosSummary> summaries = new ArrayList<>();
        for (PosKeuangan pos : posList) {
            double alokasi = pos.getAlokasi(user.getPemasukanBulanan());
            double terpakai = 0;
            for (Transaksi t : trxList) {
                if (t.getPosId() == pos.getId() && t.getJenis().equals("pengeluaran"))
                    terpakai += t.getNominal();
            }
            summaries.add(new PosSummary(pos, alokasi, terpakai));
        }
        try {
            String path = PdfService.generateLaporan(getContext(), user, summaries, trxList, bulan, tahun);
            Toast.makeText(getContext(), "PDF tersimpan: " + path, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Gagal export: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
