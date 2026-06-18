package com.ubaidfauzan.dompetkeluarga.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ubaidfauzan.dompetkeluarga.R;
import com.ubaidfauzan.dompetkeluarga.activities.DetailTransaksiActivity;
import com.ubaidfauzan.dompetkeluarga.models.PosSummary;
import com.ubaidfauzan.dompetkeluarga.spk.SpkService;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.List;
import com.ubaidfauzan.dompetkeluarga.utils.CurrencyFormatter;

public class TransaksiPosAdapter extends RecyclerView.Adapter<TransaksiPosAdapter.ViewHolder> {

    private List<PosSummary> summaries;
    private Context context;
    private int userId;

    public TransaksiPosAdapter(List<PosSummary> summaries, Context context, int userId) {
        this.summaries = summaries;
        this.context = context;
        this.userId = userId;
    }

    public void updateData(List<PosSummary> newList) {
        this.summaries = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Reuse desain kartu dari item_pos_summary
        View v = LayoutInflater.from(context).inflate(R.layout.item_pos_summary, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PosSummary s = summaries.get(position);

        holder.tvNama.setText(s.getPos().getNama());
        holder.tvAlokasi.setText("Alokasi " + CurrencyFormatter.format(s.getAlokasi()));
        holder.tvTerpakai.setText("Terpakai " + CurrencyFormatter.format(s.getTerpakai()));
        holder.tvBobot.setText((int) s.getPos().getBobotPersen() + "%");

        try {
            int warnaInt = Color.parseColor(s.getPos().getWarnaPrimary());
            holder.ivCatBg.setImageTintList(android.content.res.ColorStateList.valueOf(warnaInt));
            holder.progressBar.setIndicatorColor(warnaInt);
        } catch (Exception ignored) {}

        // Ikon kategori
        String namaPos = s.getPos().getNama().toLowerCase();
        int iconResId = R.drawable.ic_cat_keb_pokok;
        if (namaPos.contains("darurat"))    iconResId = R.drawable.ic_cat_dana_darurat;
        else if (namaPos.contains("tabungan")) iconResId = R.drawable.ic_cat_tabungan;
        else if (namaPos.contains("pendidikan")) iconResId = R.drawable.ic_cat_pendidikan;
        else if (namaPos.contains("investasi")) iconResId = R.drawable.ic_cat_investasi;
        else if (namaPos.contains("hiburan")) iconResId = R.drawable.ic_cat_hiburan;
        else if (namaPos.contains("sosial") || namaPos.contains("zakat")) iconResId = R.drawable.ic_cat_sosial;
        holder.ivCatIcon.setImageResource(iconResId);

        if (s.isMinus()) {
            holder.tvSisa.setText("−" + CurrencyFormatter.format(Math.abs(s.getSisa())));
            holder.tvSisa.setTextColor(Color.parseColor("#D85A30"));
            holder.progressBar.setIndicatorColor(Color.parseColor("#D85A30"));
        } else {
            holder.tvSisa.setText(CurrencyFormatter.format(s.getSisa()));
            holder.tvSisa.setTextColor(Color.parseColor("#1D9E75"));
        }

        holder.progressBar.setProgress((int)(s.getPersenTerpakai() * 100));

        // Klik → buka DetailTransaksiActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailTransaksiActivity.class);
            intent.putExtra("pos_id",   s.getPos().getId());
            intent.putExtra("nama_pos", s.getPos().getNama());
            intent.putExtra("alokasi",  s.getAlokasi());
            intent.putExtra("terpakai", s.getTerpakai());
            intent.putExtra("warna",    s.getPos().getWarnaPrimary());
            intent.putExtra("user_id",  userId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return summaries.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvAlokasi, tvTerpakai, tvSisa, tvBobot;
        LinearProgressIndicator progressBar;
        ImageView ivCatBg, ivCatIcon;

        ViewHolder(View v) {
            super(v);
            tvNama     = v.findViewById(R.id.tv_nama_pos);
            tvAlokasi  = v.findViewById(R.id.tv_alokasi);
            tvTerpakai = v.findViewById(R.id.tv_terpakai);
            tvSisa     = v.findViewById(R.id.tv_sisa);
            tvBobot    = v.findViewById(R.id.tv_bobot);
            progressBar = v.findViewById(R.id.progress_bar);
            ivCatBg    = v.findViewById(R.id.iv_cat_bg);
            ivCatIcon  = v.findViewById(R.id.iv_cat_icon);
        }
    }
}
