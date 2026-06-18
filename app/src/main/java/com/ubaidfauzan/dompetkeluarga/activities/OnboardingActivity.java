
package com.ubaidfauzan.dompetkeluarga.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.ubaidfauzan.dompetkeluarga.R;
import com.ubaidfauzan.dompetkeluarga.database.DatabaseHelper;
import com.ubaidfauzan.dompetkeluarga.models.PosKeuangan;
import com.ubaidfauzan.dompetkeluarga.models.UserProfile;
import com.ubaidfauzan.dompetkeluarga.spk.SpkService;
import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private EditText etNama, etPemasukan, etSumber, etPin, etPinKonfir;
    private Spinner spinnerAnggota, spinnerDarurat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        etNama = findViewById(R.id.et_nama);
        etPemasukan = findViewById(R.id.et_pemasukan);
        etSumber = findViewById(R.id.et_sumber);
        etPin = findViewById(R.id.et_pin);
        etPinKonfir = findViewById(R.id.et_pin_konfir);
        spinnerAnggota = findViewById(R.id.spinner_anggota);
        spinnerDarurat = findViewById(R.id.spinner_darurat);

        String[] anggotaOptions = {"1 orang","2 orang","3 orang","4 orang","5 orang","6 orang"};
        spinnerAnggota.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, anggotaOptions));
        spinnerAnggota.setSelection(1);

        String[] daruratOptions = {"3x pengeluaran","6x pengeluaran","12x pengeluaran"};
        spinnerDarurat.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, daruratOptions));

        Button btnLanjut = findViewById(R.id.btn_lanjut);
        btnLanjut.setOnClickListener(v -> simpanProfil());
    }

    private void simpanProfil() {
        String nama = etNama.getText().toString().trim();
        String pemasukanStr = etPemasukan.getText().toString().trim();
        String pin = etPin.getText().toString().trim();
        String pinKonfir = etPinKonfir.getText().toString().trim();

        if (nama.isEmpty() || pemasukanStr.isEmpty() || pin.length() != 4) {
            Toast.makeText(this, "Lengkapi semua data & PIN 4 digit", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pin.equals(pinKonfir)) {
            Toast.makeText(this, "PIN tidak cocok", Toast.LENGTH_SHORT).show();
            return;
        }

        double pemasukan = Double.parseDouble(pemasukanStr);
        int anggota = spinnerAnggota.getSelectedItemPosition() + 1;
        int[] daruratValues = {3, 6, 12};
        int targetDarurat = daruratValues[spinnerDarurat.getSelectedItemPosition()];

        UserProfile user = new UserProfile(nama, anggota, pemasukan,
            etSumber.getText().toString().trim(), targetDarurat, pin);

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        long userId = db.insertUser(user);

        // Inisialisasi pos default
        inisialisasiPosDefault(db, (int) userId);

        SharedPreferences prefs = getSharedPreferences("DompetPrefs", MODE_PRIVATE);
        prefs.edit().putInt("logged_user_id", (int) userId).apply();

        Intent intent = new Intent(this, PrioritasPosActivity.class);
        intent.putExtra("user_id", (int) userId);
        intent.putExtra("is_onboarding", true);
        startActivity(intent);
        finish();
    }

    private void inisialisasiPosDefault(DatabaseHelper db, int userId) {
        String[][] defaultPos = {
            {"Dana Darurat","Buffer keamanan keluarga","shield","#1D9E75","1"},
            {"Tabungan","Rekening terpisah","savings","#0F6E56","2"},
            {"Keb. Pokok","Makan, listrik, transportasi","home","#D85A30","3"},
            {"Pendidikan","Dana anak & pengembangan diri","school","#7F77DD","4"},
            {"Investasi","Reksa dana, emas, saham","trending_up","#378ADD","5"},
            {"Hiburan","Rekreasi & lifestyle","celebration","#D4537E","6"},
            {"Sosial & Sedekah","Zakat, infaq, sumbangan","volunteer_activism","#EF9F27","7"},
        };

        List<PosKeuangan> posList = new ArrayList<>();
        double sumResip = 0;
        for (String[] p : defaultPos) sumResip += 1.0 / Integer.parseInt(p[4]);

        for (String[] p : defaultPos) {
            int rank = Integer.parseInt(p[4]);
            double bobot = (1.0 / rank) / sumResip * 100.0;
            PosKeuangan pos = new PosKeuangan(p[0], p[1], p[2], rank,
                Math.round(bobot * 100.0) / 100.0, 0, p[3], userId);
            db.insertPos(pos);
        }
    }
}
