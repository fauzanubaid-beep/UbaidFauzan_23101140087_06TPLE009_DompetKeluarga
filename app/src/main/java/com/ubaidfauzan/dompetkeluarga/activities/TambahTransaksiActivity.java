
package com.ubaidfauzan.dompetkeluarga.activities;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.ubaidfauzan.dompetkeluarga.R;
import com.ubaidfauzan.dompetkeluarga.database.DatabaseHelper;
import com.ubaidfauzan.dompetkeluarga.models.PosKeuangan;
import com.ubaidfauzan.dompetkeluarga.models.Transaksi;
import java.text.SimpleDateFormat;
import java.util.*;

public class TambahTransaksiActivity extends AppCompatActivity {

    private EditText etNominal, etCatatan;
    private Spinner spinnerPos;
    private List<PosKeuangan> posList;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_transaksi);

        userId = getIntent().getIntExtra("user_id", -1);
        etNominal = findViewById(R.id.et_nominal);
        etCatatan = findViewById(R.id.et_catatan);
        spinnerPos = findViewById(R.id.spinner_pos);

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        posList = db.getPosUser(userId);

        String[] namaPos = new String[posList.size()];
        for (int i = 0; i < posList.size(); i++) namaPos[i] = posList.get(i).getNama();
        spinnerPos.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, namaPos));

        findViewById(R.id.btn_simpan).setOnClickListener(v -> simpanTransaksi());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Catat Pengeluaran");
        }
    }

    private void simpanTransaksi() {
        String nominalStr = etNominal.getText().toString().trim();
        if (nominalStr.isEmpty()) {
            Toast.makeText(this, "Masukkan nominal", Toast.LENGTH_SHORT).show();
            return;
        }
        double nominal = Double.parseDouble(nominalStr);
        PosKeuangan pos = posList.get(spinnerPos.getSelectedItemPosition());
        String tanggal = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Transaksi t = new Transaksi(pos.getId(), pos.getNama(), nominal,
            "pengeluaran", tanggal, etCatatan.getText().toString().trim(), userId);

        DatabaseHelper.getInstance(this).insertTransaksi(t);
        Toast.makeText(this, "Transaksi tersimpan!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
