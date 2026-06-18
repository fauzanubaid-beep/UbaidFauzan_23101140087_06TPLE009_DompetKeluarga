package com.ubaidfauzan.dompetkeluarga.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ubaidfauzan.dompetkeluarga.R;
import com.ubaidfauzan.dompetkeluarga.adapters.TransaksiAdapter;
import com.ubaidfauzan.dompetkeluarga.database.DatabaseHelper;
import com.ubaidfauzan.dompetkeluarga.models.Transaksi;
import com.ubaidfauzan.dompetkeluarga.spk.SpkService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.Calendar;
import java.util.List;
import com.ubaidfauzan.dompetkeluarga.utils.CurrencyFormatter;

public class DetailTransaksiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_transaksi);

        // Ambil data dari Intent
        int posId        = getIntent().getIntExtra("pos_id", -1);
        String namaPos   = getIntent().getStringExtra("nama_pos");
        double alokasi   = getIntent().getDoubleExtra("alokasi", 0);
        double terpakai  = getIntent().getDoubleExtra("terpakai", 0);
        String warna     = getIntent().getStringExtra("warna");
        int userId       = getIntent().getIntExtra("user_id", -1);

        double sisa = alokasi - terpakai;
        float persen = alokasi > 0 ? (float)(terpakai / alokasi) : 0f;

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        TextView tvToolbarNama = findViewById(R.id.tv_toolbar_nama);
        TextView tvToolbarBulan = findViewById(R.id.tv_toolbar_bulan);
        tvToolbarNama.setText(namaPos != null ? namaPos : "Detail Transaksi");

        String[] bulanNama = {"Jan","Feb","Mar","Apr","Mei","Jun","Jul","Ags","Sep","Okt","Nov","Des"};
        Calendar now = Calendar.getInstance();
        tvToolbarBulan.setText(bulanNama[now.get(Calendar.MONTH)] + " " + now.get(Calendar.YEAR));
        toolbar.setNavigationOnClickListener(v -> finish());

        // Warna indikator pos
        int warnaInt = Color.parseColor("#2563EB"); // fallback biru
        if (warna != null && !warna.isEmpty()) {
            try { warnaInt = Color.parseColor(warna); } catch (Exception ignored) {}
        }

        // Summary views
        TextView tvAlokasi     = findViewById(R.id.tv_alokasi);
        TextView tvTerpakai    = findViewById(R.id.tv_terpakai);
        TextView tvSisa        = findViewById(R.id.tv_sisa);
        TextView tvJumlahTrx   = findViewById(R.id.tv_jumlah_trx);
        LinearProgressIndicator progress = findViewById(R.id.progress_pemakaian);

        tvAlokasi.setText(CurrencyFormatter.format(alokasi));
        tvTerpakai.setText(CurrencyFormatter.format(terpakai));
        progress.setIndicatorColor(warnaInt);
        progress.setProgress(Math.min((int)(persen * 100), 100));

        if (sisa < 0) {
            tvSisa.setText("−" + CurrencyFormatter.format(Math.abs(sisa)));
            tvSisa.setTextColor(Color.parseColor("#A31212"));
        } else {
            tvSisa.setText(CurrencyFormatter.format(sisa));
            tvSisa.setTextColor(Color.parseColor("#1D9E75"));
        }

        // Ambil transaksi dari database per pos bulan ini
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        List<Transaksi> list = db.getTransaksiByPos(userId, posId,
                now.get(Calendar.MONTH) + 1, now.get(Calendar.YEAR));

        tvJumlahTrx.setText(list.size() + " transaksi");

        // RecyclerView
        RecyclerView rv = findViewById(R.id.rv_detail_transaksi);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new TransaksiAdapter(list));
    }
}
