package com.ubaidfauzan.dompetkeluarga.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ubaidfauzan.dompetkeluarga.R;
import com.ubaidfauzan.dompetkeluarga.adapters.TransaksiPosAdapter;
import com.ubaidfauzan.dompetkeluarga.database.DatabaseHelper;
import com.ubaidfauzan.dompetkeluarga.models.PosKeuangan;
import com.ubaidfauzan.dompetkeluarga.models.PosSummary;
import com.ubaidfauzan.dompetkeluarga.models.Transaksi;
import com.ubaidfauzan.dompetkeluarga.models.UserProfile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TransaksiFragment extends Fragment {

    private int userId;
    private RecyclerView rvTransaksi;
    private TransaksiPosAdapter adapter;
    private android.widget.TextView tvKonteksTransaksi, tvMotivasi, tvEmptySubtitle;
    private View emptyStateView;

    public TransaksiFragment(int userId) {
        this.userId = userId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaksi, container, false);

        rvTransaksi = view.findViewById(R.id.rv_transaksi);
        rvTransaksi.setLayoutManager(new LinearLayoutManager(getContext()));
        
        tvKonteksTransaksi = view.findViewById(R.id.tv_konteks_transaksi);
        tvMotivasi = view.findViewById(R.id.tv_motivasi);
        emptyStateView = view.findViewById(R.id.empty_state_transaksi);
        tvEmptySubtitle = emptyStateView.findViewById(R.id.tv_empty_subtitle);

        refresh();
        return view;
    }

    public void refresh() {
        if (getContext() == null) return;

        DatabaseHelper db = DatabaseHelper.getInstance(getContext());
        UserProfile user = db.getUserById(userId);
        if (user == null) return;

        Calendar now = Calendar.getInstance();
        List<PosKeuangan> posList = db.getPosUser(userId);
        List<Transaksi> trxList = db.getTransaksiBulan(userId,
                now.get(Calendar.MONTH) + 1, now.get(Calendar.YEAR));

        // Bangun PosSummary untuk SEMUA pos (termasuk yang terpakai = 0)
        List<PosSummary> summaries = new ArrayList<>();
        for (PosKeuangan pos : posList) {
            double alokasi = pos.getAlokasi(user.getPemasukanBulanan());
            double terpakai = 0;
            for (Transaksi t : trxList) {
                if (t.getPosId() == pos.getId() && t.getJenis().equals("pengeluaran")) {
                    terpakai += t.getNominal();
                }
            }
            summaries.add(new PosSummary(pos, alokasi, terpakai));
        }

        if (adapter == null) {
            adapter = new TransaksiPosAdapter(summaries, getContext(), userId);
            rvTransaksi.setAdapter(adapter);
        } else {
            adapter.updateData(summaries);
        }
        
        String[] namaBulan = {"","Januari","Februari","Maret","April","Mei","Juni",
            "Juli","Agustus","September","Oktober","November","Desember"};
        int month = now.get(Calendar.MONTH) + 1;
        int year = now.get(Calendar.YEAR);
        
        tvKonteksTransaksi.setText(namaBulan[month] + " " + year + " · " + trxList.size() + " transaksi tercatat");
        
        if (trxList.isEmpty()) {
            emptyStateView.setVisibility(View.VISIBLE);
            tvEmptySubtitle.setText("Belum ada transaksi bulan ini. Catat pengeluaran pertamamu sekarang! \uD83D\uDCDD"); // 📝
            rvTransaksi.setVisibility(View.GONE);
            tvMotivasi.setVisibility(View.GONE);
        } else {
            emptyStateView.setVisibility(View.GONE);
            rvTransaksi.setVisibility(View.VISIBLE);
            tvMotivasi.setVisibility(View.VISIBLE);
        }
    }
}
