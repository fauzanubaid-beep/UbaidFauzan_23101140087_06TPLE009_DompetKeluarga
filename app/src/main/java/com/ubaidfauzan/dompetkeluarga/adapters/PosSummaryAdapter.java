
package com.ubaidfauzan.dompetkeluarga.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.ubaidfauzan.dompetkeluarga.R;
import com.ubaidfauzan.dompetkeluarga.models.PosSummary;
import com.ubaidfauzan.dompetkeluarga.spk.SpkService;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.List;
import com.ubaidfauzan.dompetkeluarga.utils.CurrencyFormatter;

public class PosSummaryAdapter extends RecyclerView.Adapter<PosSummaryAdapter.ViewHolder> {

    private List<PosSummary> summaries;
    private Context context;

    public PosSummaryAdapter(List<PosSummary> summaries, Context context) {
        this.summaries = summaries;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_pos_summary, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PosSummary s = summaries.get(position);
        holder.tvNama.setText(s.getPos().getNama());
        holder.tvAlokasi.setText("Alokasi " + CurrencyFormatter.format(s.getAlokasi()));
        holder.tvTerpakai.setText("Terpakai " + CurrencyFormatter.format(s.getTerpakai()));

        try {
            int warnaInt = Color.parseColor(s.getPos().getWarnaPrimary());
            int iconBgColor = androidx.core.graphics.ColorUtils.setAlphaComponent(warnaInt, 38); // ~15% opacity
            int softTrackColor = androidx.core.graphics.ColorUtils.setAlphaComponent(warnaInt, 40); // ~15% opacity for track

            // Card: white background with border (handled by XML)
            com.google.android.material.card.MaterialCardView cardView = (com.google.android.material.card.MaterialCardView) holder.itemView;
            cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.color_card_bg));
            cardView.setStrokeWidth((int) (context.getResources().getDisplayMetrics().density)); // 1dp
            cardView.setStrokeColor(ContextCompat.getColor(context, R.color.color_card_border));

            // Icon background: 15% opacity of category color
            holder.ivCatBg.setImageTintList(ColorStateList.valueOf(iconBgColor));

            // Icon color: category color (not white)
            holder.ivCatIcon.setImageTintList(ColorStateList.valueOf(warnaInt));

            // Progress bar
            holder.progressBar.setIndicatorColor(warnaInt);
            holder.progressBar.setTrackColor(softTrackColor);
            
            // Badge text color
            holder.tvBobot.setTextColor(ContextCompat.getColor(context, R.color.color_heading));
        } catch (Exception ignored) {}

        // Set Icon based on category
        String namaPos = s.getPos().getNama().toLowerCase();
        int iconResId = R.drawable.ic_cat_keb_pokok; // default
        if (namaPos.contains("darurat")) iconResId = R.drawable.ic_cat_dana_darurat;
        else if (namaPos.contains("tabungan")) iconResId = R.drawable.ic_cat_tabungan;
        else if (namaPos.contains("pendidikan")) iconResId = R.drawable.ic_cat_pendidikan;
        else if (namaPos.contains("investasi")) iconResId = R.drawable.ic_cat_investasi;
        else if (namaPos.contains("hiburan")) iconResId = R.drawable.ic_cat_hiburan;
        else if (namaPos.contains("sosial") || namaPos.contains("zakat")) iconResId = R.drawable.ic_cat_sosial;
        holder.ivCatIcon.setImageResource(iconResId);

        if (s.isMinus()) {
            holder.tvSisa.setText("−" + CurrencyFormatter.format(Math.abs(s.getSisa())));
            int redColor = ContextCompat.getColor(context, R.color.pos_sisa_negative);
            holder.tvSisa.setTextColor(redColor);
            holder.progressBar.setIndicatorColor(redColor);
            holder.progressBar.setTrackColor(androidx.core.graphics.ColorUtils.setAlphaComponent(redColor, 40));
        } else {
            holder.tvSisa.setText(CurrencyFormatter.format(s.getSisa()));
            try {
                int warnaInt = Color.parseColor(s.getPos().getWarnaPrimary());
                holder.tvSisa.setTextColor(warnaInt);
            } catch (Exception e) {
                holder.tvSisa.setTextColor(ContextCompat.getColor(context, R.color.pos_sisa_positive));
            }
        }

        holder.progressBar.setProgress((int)(s.getPersenTerpakai() * 100));
        holder.tvBobot.setText(s.getPos().getTargetNominal() > 0
            ? "Nominal" : s.getPos().getBobotPersen() + "%");
            
        double persenTerpakai = s.getPersenTerpakai() * 100;
        if (s.getTerpakai() == 0) {
            holder.tvInsightPos.setText("Belum ada pengeluaran hari ini \uD83D\uDCAA"); // 💪
        } else if (persenTerpakai < 10) {
            holder.tvInsightPos.setText("Masih sangat aman, pertahankan!");
        } else if (persenTerpakai <= 50) {
            holder.tvInsightPos.setText("Pemakaian normal, tetap terjaga.");
        } else if (persenTerpakai <= 80) {
            holder.tvInsightPos.setText("Sudah separuh terpakai, mulai hati-hati.");
        } else if (persenTerpakai <= 100) {
            holder.tvInsightPos.setText("⚠ Hampir habis! Kurangi pengeluaran pos ini.");
        } else {
            holder.tvInsightPos.setText("\uD83D\uDD34 Alokasi terlampaui! Perlu ditambah."); // 🔴
        }
    }

    @Override
    public int getItemCount() { return summaries.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvAlokasi, tvTerpakai, tvSisa, tvBobot, tvInsightPos;
        LinearProgressIndicator progressBar;
        ImageView ivCatBg, ivCatIcon;

        ViewHolder(View v) {
            super(v);
            tvNama = v.findViewById(R.id.tv_nama_pos);
            tvAlokasi = v.findViewById(R.id.tv_alokasi);
            tvTerpakai = v.findViewById(R.id.tv_terpakai);
            tvSisa = v.findViewById(R.id.tv_sisa);
            tvBobot = v.findViewById(R.id.tv_bobot);
            tvInsightPos = v.findViewById(R.id.tv_insight_pos);
            progressBar = v.findViewById(R.id.progress_bar);
            ivCatBg = v.findViewById(R.id.iv_cat_bg);
            ivCatIcon = v.findViewById(R.id.iv_cat_icon);
        }
    }
}
