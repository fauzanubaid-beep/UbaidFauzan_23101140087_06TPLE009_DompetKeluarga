package com.ubaidfauzan.dompetkeluarga.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ubaidfauzan.dompetkeluarga.R;
import com.ubaidfauzan.dompetkeluarga.models.Transaksi;
import com.ubaidfauzan.dompetkeluarga.spk.SpkService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ubaidfauzan.dompetkeluarga.utils.CurrencyFormatter;

public class TransaksiAdapter extends RecyclerView.Adapter<TransaksiAdapter.ViewHolder> {

    private List<Transaksi> list;

    // Map nama pos -> warna (hex string)
    private static final Map<String, String> POS_COLOR_MAP = new HashMap<>();
    static {
        POS_COLOR_MAP.put("dana darurat", "#1D9E75");
        POS_COLOR_MAP.put("tabungan", "#0F6E56");
        POS_COLOR_MAP.put("kebutuhan pokok", "#E84343");
        POS_COLOR_MAP.put("pendidikan", "#7F77DD");
        POS_COLOR_MAP.put("investasi", "#2563EB");
        POS_COLOR_MAP.put("hiburan", "#F5C518");
        POS_COLOR_MAP.put("sosial", "#EF9F27");
    }

    // Bulan pendek bahasa Indonesia
    private static final String[] BULAN = {
        "Jan","Feb","Mar","Apr","Mei","Jun",
        "Jul","Ags","Sep","Okt","Nov","Des"
    };

    public TransaksiAdapter(List<Transaksi> list) {
        this.list = list;
    }

    public void updateData(List<Transaksi> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaksi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaksi t = list.get(position);

        // Parse tanggal (format: "yyyy-MM-dd" or "dd MMM yyyy" etc.)
        parseTanggal(t.getTanggal(), holder.tvHari, holder.tvBulan);

        holder.tvNamaPos.setText(t.getNamaPos());
        holder.tvNominal.setText(CurrencyFormatter.format(t.getNominal()));

        // Catatan
        if (t.getCatatan() == null || t.getCatatan().trim().isEmpty()) {
            holder.tvCatatan.setVisibility(View.GONE);
        } else {
            holder.tvCatatan.setVisibility(View.VISIBLE);
            holder.tvCatatan.setText(t.getCatatan());
        }

        // Warna nominal berdasarkan jenis
        if (t.getJenis().equalsIgnoreCase("pemasukan")) {
            holder.tvNominal.setTextColor(Color.parseColor("#1D9E75")); // Hijau
        } else {
            holder.tvNominal.setTextColor(Color.parseColor("#E84343")); // Merah
        }

        // Warna dot berdasarkan nama pos
        int dotColor = getPosColor(t.getNamaPos());
        GradientDrawable dotBg = (GradientDrawable) holder.viewDot.getBackground().mutate();
        dotBg.setColor(dotColor);

        // Hide bottom divider for last item
        holder.viewDivider.setVisibility(
                position == getItemCount() - 1 ? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * Mendapatkan warna berdasarkan nama pos.
     * Menggunakan pencocokan partial (contains) seperti di PosSummaryAdapter.
     */
    private int getPosColor(String namaPos) {
        if (namaPos == null) return Color.parseColor("#2563EB"); // fallback biru

        String lower = namaPos.toLowerCase();
        for (Map.Entry<String, String> entry : POS_COLOR_MAP.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return Color.parseColor(entry.getValue());
            }
        }
        return Color.parseColor("#2563EB"); // fallback biru
    }

    /**
     * Parse tanggal string ke hari dan bulan.
     * Mendukung format: "yyyy-MM-dd", "dd MMM yyyy", "dd/MM/yyyy"
     */
    private void parseTanggal(String tanggal, TextView tvHari, TextView tvBulan) {
        if (tanggal == null || tanggal.isEmpty()) {
            tvHari.setText("--");
            tvBulan.setText("---");
            return;
        }

        try {
            // Try ISO format yyyy-MM-dd
            if (tanggal.contains("-") && tanggal.length() >= 10) {
                String[] parts = tanggal.split("-");
                if (parts.length >= 3 && parts[0].length() == 4) {
                    // yyyy-MM-dd
                    int bulanIdx = Integer.parseInt(parts[1]) - 1;
                    tvHari.setText(String.valueOf(Integer.parseInt(parts[2].substring(0, Math.min(2, parts[2].length())))));
                    tvBulan.setText(bulanIdx >= 0 && bulanIdx < 12 ? BULAN[bulanIdx] : "---");
                    return;
                }
            }

            // Try dd/MM/yyyy
            if (tanggal.contains("/")) {
                String[] parts = tanggal.split("/");
                if (parts.length >= 3) {
                    tvHari.setText(String.valueOf(Integer.parseInt(parts[0])));
                    int bulanIdx = Integer.parseInt(parts[1]) - 1;
                    tvBulan.setText(bulanIdx >= 0 && bulanIdx < 12 ? BULAN[bulanIdx] : "---");
                    return;
                }
            }

            // Fallback: show as-is
            tvHari.setText(tanggal.substring(0, Math.min(2, tanggal.length())));
            tvBulan.setText(tanggal.length() > 3 ? tanggal.substring(3, Math.min(6, tanggal.length())) : "");
        } catch (Exception e) {
            tvHari.setText("--");
            tvBulan.setText("---");
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHari, tvBulan, tvNamaPos, tvNominal, tvCatatan, tvTanggal, tvJenis;
        View viewDot, viewDivider;

        ViewHolder(View itemView) {
            super(itemView);
            tvHari = itemView.findViewById(R.id.tv_hari);
            tvBulan = itemView.findViewById(R.id.tv_bulan);
            tvNamaPos = itemView.findViewById(R.id.tv_nama_pos);
            tvNominal = itemView.findViewById(R.id.tv_nominal);
            tvCatatan = itemView.findViewById(R.id.tv_catatan);
            tvTanggal = itemView.findViewById(R.id.tv_tanggal);
            tvJenis = itemView.findViewById(R.id.tv_jenis);
            viewDot = itemView.findViewById(R.id.view_dot);
            viewDivider = itemView.findViewById(R.id.view_divider);
        }
    }
}
