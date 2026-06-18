
package com.ubaidfauzan.dompetkeluarga.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.*;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.ubaidfauzan.dompetkeluarga.R;
import com.ubaidfauzan.dompetkeluarga.models.PosKeuangan;
import java.util.List;
import com.ubaidfauzan.dompetkeluarga.utils.CurrencyFormatter;

public class KelolaPosAdapter extends RecyclerView.Adapter<KelolaPosAdapter.ViewHolder> {

    public interface OnEditListener { void onEdit(PosKeuangan pos); }

    private List<PosKeuangan> posList;
    private Context context;
    private OnEditListener listener;

    public KelolaPosAdapter(List<PosKeuangan> posList, Context context, OnEditListener listener) {
        this.posList = posList;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_prioritas_pos, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PosKeuangan pos = posList.get(position);
        holder.tvRank.setText("#" + pos.getPrioritasRank());
        holder.tvNama.setText(pos.getNama());
        String alokasi = pos.getTargetNominal() > 0
            ? CurrencyFormatter.format(pos.getTargetNominal()) + " (nominal)"
            : pos.getBobotPersen() + "% persentase";
        holder.tvBobot.setText(alokasi);
        holder.itemView.setOnClickListener(v -> listener.onEdit(pos));
    }

    @Override
    public int getItemCount() { return posList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvNama, tvBobot;
        ViewHolder(View v) {
            super(v);
            tvRank = v.findViewById(R.id.tv_rank);
            tvNama = v.findViewById(R.id.tv_nama);
            tvBobot = v.findViewById(R.id.tv_bobot);
        }
    }
}
