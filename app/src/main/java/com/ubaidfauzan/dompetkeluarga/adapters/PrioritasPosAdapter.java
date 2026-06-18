
package com.ubaidfauzan.dompetkeluarga.adapters;

import android.content.Context;
import android.view.*;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.ubaidfauzan.dompetkeluarga.R;
import com.ubaidfauzan.dompetkeluarga.models.PosKeuangan;
import com.ubaidfauzan.dompetkeluarga.spk.SpkService;
import java.util.List;

public class PrioritasPosAdapter extends RecyclerView.Adapter<PrioritasPosAdapter.ViewHolder> {

    private List<PosKeuangan> posList;
    private Context context;

    public PrioritasPosAdapter(List<PosKeuangan> posList, Context context) {
        this.posList = posList;
        this.context = context;
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

        // Hitung bobot sementara untuk preview
        double sumResip = 0;
        for (PosKeuangan p : posList) sumResip += 1.0 / p.getPrioritasRank();
        double bobot = (1.0 / pos.getPrioritasRank()) / sumResip * 100.0;
        holder.tvBobot.setText(String.format("%.1f%%", bobot));
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
