
package com.ubaidfauzan.dompetkeluarga.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.ubaidfauzan.dompetkeluarga.R;
import com.ubaidfauzan.dompetkeluarga.database.DatabaseHelper;
import com.ubaidfauzan.dompetkeluarga.models.*;
import com.ubaidfauzan.dompetkeluarga.spk.SpkService;
import java.text.SimpleDateFormat;
import java.util.*;
import com.ubaidfauzan.dompetkeluarga.utils.CurrencyFormatter;

public class TambahPemasukanActivity extends AppCompatActivity {

    private EditText etNominal, etSumber;
    private LinearLayout llPreview;
    private int userId;
    private List<PosKeuangan> posList;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_pemasukan);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tambah Pemasukan");
        }

        userId = getIntent().getIntExtra("user_id", -1);
        db = DatabaseHelper.getInstance(this);
        posList = db.getPosUser(userId);

        etNominal = findViewById(R.id.et_nominal);
        etSumber = findViewById(R.id.et_sumber);
        llPreview = findViewById(R.id.ll_preview);

        etNominal.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { updatePreview(); }
            public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.btn_simpan).setOnClickListener(v -> simpan());
    }

    private void updatePreview() {
        llPreview.removeAllViews();
        String nominalStr = etNominal.getText().toString().trim();
        if (nominalStr.isEmpty()) return;
        double nominal = Double.parseDouble(nominalStr);
        Map<Integer, Double> dist = SpkService.distribusiPemasukanTambahan(posList, nominal);

        for (PosKeuangan pos : posList) {
            Double jumlah = dist.get(pos.getId());
            if (jumlah == null) continue;
            TextView tv = new TextView(this);
            tv.setText("• " + pos.getNama() + "  →  +" + CurrencyFormatter.format(jumlah));
            tv.setTextSize(12);
            tv.setPadding(0, 4, 0, 4);
            llPreview.addView(tv);
        }
    }

    private void simpan() {
        String nominalStr = etNominal.getText().toString().trim();
        String sumber = etSumber.getText().toString().trim();
        if (nominalStr.isEmpty() || sumber.isEmpty()) {
            Toast.makeText(this, "Isi nominal dan sumber", Toast.LENGTH_SHORT).show();
            return;
        }
        double nominal = Double.parseDouble(nominalStr);
        Map<Integer, Double> dist = SpkService.distribusiPemasukanTambahan(posList, nominal);
        String tanggal = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        for (PosKeuangan pos : posList) {
            Double jumlah = dist.get(pos.getId());
            if (jumlah == null) continue;
            Transaksi t = new Transaksi(pos.getId(), pos.getNama(), jumlah,
                "pemasukan", tanggal, "Distribusi dari: " + sumber, userId);
            db.insertTransaksi(t);
        }

        // Update pemasukan bulanan user
        UserProfile user = db.getUserById(userId);
        user.setPemasukanBulanan(user.getPemasukanBulanan() + nominal);
        db.updateUser(user);

        Toast.makeText(this, "Pemasukan tersimpan & didistribusikan!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
