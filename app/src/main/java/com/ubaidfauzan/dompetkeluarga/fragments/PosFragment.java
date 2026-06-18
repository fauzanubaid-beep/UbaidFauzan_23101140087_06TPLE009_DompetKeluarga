
package com.ubaidfauzan.dompetkeluarga.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ubaidfauzan.dompetkeluarga.R;
import com.ubaidfauzan.dompetkeluarga.activities.KelolaPosActivity;
import com.ubaidfauzan.dompetkeluarga.activities.PrioritasPosActivity;
import com.ubaidfauzan.dompetkeluarga.adapters.PosSummaryAdapter;
import com.ubaidfauzan.dompetkeluarga.database.DatabaseHelper;
import com.ubaidfauzan.dompetkeluarga.models.*;
import com.ubaidfauzan.dompetkeluarga.spk.SpkService;
import android.widget.TextView;
import java.util.*;
import com.ubaidfauzan.dompetkeluarga.utils.CurrencyFormatter;

public class PosFragment extends Fragment {

    private int userId;
    private RecyclerView rvPos;
    private TextView tvKonteksPos, tvEmptySubtitle;
    private View emptyStateView;

    public PosFragment(int userId) { this.userId = userId; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pos, container, false);
        rvPos = view.findViewById(R.id.rv_pos);
        rvPos.setLayoutManager(new LinearLayoutManager(getContext()));
        tvKonteksPos = view.findViewById(R.id.tv_konteks_pos);
        emptyStateView = view.findViewById(R.id.empty_state_pos);
        tvEmptySubtitle = emptyStateView.findViewById(R.id.tv_empty_subtitle);

        view.findViewById(R.id.btn_kelola).setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            Intent i = new Intent(getContext(), KelolaPosActivity.class);
            i.putExtra("user_id", userId);
            startActivity(i);
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
        List<Transaksi> trxList = db.getTransaksiBulan(userId, now.get(Calendar.MONTH)+1, now.get(Calendar.YEAR));
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
        
        double totalAlokasi = 0;
        for (PosSummary s : summaries) {
            totalAlokasi += s.getAlokasi();
        }
        
        tvKonteksPos.setText(posList.size() + " pos aktif · Total alokasi " + CurrencyFormatter.format(totalAlokasi));
        
        if (posList.isEmpty()) {
            emptyStateView.setVisibility(View.VISIBLE);
            tvEmptySubtitle.setText("Belum ada pos keuangan. Buat pos pertamamu dan mulai atur keuangan keluarga! \uD83D\uDCA1"); // 💡
            rvPos.setVisibility(View.GONE);
        } else {
            emptyStateView.setVisibility(View.GONE);
            rvPos.setVisibility(View.VISIBLE);
        }
        
        rvPos.setAdapter(new PosSummaryAdapter(summaries, getContext()));
    }
}
