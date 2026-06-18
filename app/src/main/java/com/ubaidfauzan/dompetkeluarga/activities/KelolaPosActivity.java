
package com.ubaidfauzan.dompetkeluarga.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ubaidfauzan.dompetkeluarga.R;
import com.ubaidfauzan.dompetkeluarga.adapters.KelolaPosAdapter;
import com.ubaidfauzan.dompetkeluarga.database.DatabaseHelper;
import com.ubaidfauzan.dompetkeluarga.models.PosKeuangan;
import com.ubaidfauzan.dompetkeluarga.models.UserProfile;
import com.ubaidfauzan.dompetkeluarga.spk.SpkService;
import com.ubaidfauzan.dompetkeluarga.utils.CurrencyFormatter;
import java.util.List;

public class KelolaPosActivity extends AppCompatActivity {

    private int userId;
    private List<PosKeuangan> posList;
    private KelolaPosAdapter adapter;
    private DatabaseHelper db;
    private android.widget.TextView tvPemasukanBulanan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kelola_pos);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Kelola Pos");
        }

        userId = getIntent().getIntExtra("user_id", -1);
        db = DatabaseHelper.getInstance(this);
        posList = db.getPosUser(userId);

        RecyclerView rv = findViewById(R.id.rv_pos);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new KelolaPosAdapter(posList, this, pos -> showDialogEditPos(pos));
        rv.setAdapter(adapter);

        // Swipe kiri untuk hapus
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView r, RecyclerView.ViewHolder v, RecyclerView.ViewHolder t) { return false; }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                PosKeuangan posHapus = posList.get(pos);
                new AlertDialog.Builder(KelolaPosActivity.this)
                    .setTitle("Hapus Pos?")
                    .setMessage("Pos \"" + posHapus.getNama() + "\" dan transaksinya akan dihapus permanen.")
                    .setPositiveButton("Hapus", (d, w) -> {
                        db.deletePos(posHapus.getId());
                        posList.remove(pos);
                        // Hitung ulang rank & bobot
                        for (int i = 0; i < posList.size(); i++) posList.get(i).setPrioritasRank(i + 1);
                        SpkService.hitungBobotRankReciprocal(posList);
                        db.updateBanyakPos(posList);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(KelolaPosActivity.this, "Pos dihapus", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Batal", (d, w) -> adapter.notifyItemChanged(pos))
                    .show();
            }
        }).attachToRecyclerView(rv);

        // FAB dan Prioritas
        findViewById(R.id.fab_tambah_pos).setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            tambahPosBaru();
        });

        findViewById(R.id.btn_prioritas).setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            Intent i = new Intent(this, PrioritasPosActivity.class);
            i.putExtra("user_id", userId);
            startActivity(i);
        });

        // Edit Pemasukan Bulanan
        tvPemasukanBulanan = findViewById(R.id.tv_pemasukan_bulanan);
        refreshPemasukan();
        findViewById(R.id.btn_edit_pemasukan).setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            showDialogEditPemasukan();
        });
    }

    private void refreshPemasukan() {
        UserProfile user = db.getUserById(userId);
        if (user != null) {
            tvPemasukanBulanan.setText(CurrencyFormatter.format(user.getPemasukanBulanan()));
        }
    }

    private void showDialogEditPemasukan() {
        UserProfile user = db.getUserById(userId);
        if (user == null) return;

        android.widget.EditText etPemasukan = new android.widget.EditText(this);
        etPemasukan.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etPemasukan.setText(String.valueOf((long) user.getPemasukanBulanan()));
        etPemasukan.setHint("Masukkan pemasukan bulanan");
        etPemasukan.setSelectAllOnFocus(true);

        int dp16 = (int) (16 * getResources().getDisplayMetrics().density);
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(dp16, dp16 / 2, dp16, 0);
        etPemasukan.setLayoutParams(params);
        container.addView(etPemasukan);

        new AlertDialog.Builder(this)
            .setTitle("Ubah Pemasukan Bulanan")
            .setMessage("Gaji bulan ini berubah? Update di sini agar alokasi pos menyesuaikan.")
            .setView(container)
            .setPositiveButton("Simpan", (d, w) -> {
                String input = etPemasukan.getText().toString().trim();
                if (!input.isEmpty()) {
                    try {
                        double pemasukan = Double.parseDouble(input);
                        user.setPemasukanBulanan(pemasukan);
                        db.updateUser(user);
                        refreshPemasukan();
                        Toast.makeText(this, "Pemasukan bulanan diperbarui", Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Nominal tidak valid", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    private void showDialogEditPos(PosKeuangan pos) {
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_pos, null);
        android.widget.EditText etNama = dialogView.findViewById(R.id.et_nama);
        android.widget.EditText etNilai = dialogView.findViewById(R.id.et_nilai);
        android.widget.RadioGroup rgMode = dialogView.findViewById(R.id.rg_mode);
        android.widget.RadioButton rbNominal = dialogView.findViewById(R.id.rb_nominal);
        android.widget.RadioButton rbPersen = dialogView.findViewById(R.id.rb_persen);
        android.widget.TextView tvLabelNilai = dialogView.findViewById(R.id.tv_label_nilai);
        android.widget.RadioGroup rgWarna = dialogView.findViewById(R.id.rg_warna);

        etNama.setText(pos.getNama());
        boolean isNominal = pos.getTargetNominal() > 0;
        if (isNominal) {
            rbNominal.setChecked(true);
            etNilai.setText(String.valueOf((int) pos.getTargetNominal()));
            tvLabelNilai.setText("Nominal per Bulan (Rp)");
        } else {
            rbPersen.setChecked(true);
            etNilai.setText(String.valueOf(pos.getBobotPersen()));
        }

        rgMode.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_nominal) {
                tvLabelNilai.setText("Nominal per Bulan (Rp)");
                etNilai.setHint("500000");
            } else {
                tvLabelNilai.setText("Persentase dari Pemasukan (%)");
                etNilai.setHint("10.0");
            }
        });

        new AlertDialog.Builder(this)
            .setTitle(pos.getId() == 0 ? "Tambah Pos Baru" : "Edit Pos")
            .setView(dialogView)
            .setPositiveButton("Simpan", (d, w) -> {
                String namaBaru = etNama.getText().toString().trim();
                double nilai = Double.parseDouble(etNilai.getText().toString().trim().isEmpty() ? "0" : etNilai.getText().toString().trim());
                boolean modeNominal = rbNominal.isChecked();

                // Ambil warna
                String warna = pos.getWarnaPrimary();
                int warnaChecked = rgWarna.getCheckedRadioButtonId();
                if (warnaChecked == R.id.rb_biru) warna = "#378ADD";
                else if (warnaChecked == R.id.rb_merah) warna = "#D85A30";
                else if (warnaChecked == R.id.rb_ungu) warna = "#7F77DD";
                else warna = "#1D9E75";

                if (pos.getId() == 0) {
                    // Tambah baru
                    PosKeuangan baru = new PosKeuangan(namaBaru, "", "savings",
                        posList.size() + 1, modeNominal ? 5.0 : nilai,
                        modeNominal ? nilai : 0, warna, userId);
                    long id = db.insertPos(baru);
                    baru.setId((int) id);
                    posList.add(baru);
                } else {
                    pos.setNama(namaBaru);
                    pos.setWarnaPrimary(warna);
                    if (modeNominal) { pos.setTargetNominal(nilai); }
                    else { pos.setBobotPersen(nilai); pos.setTargetNominal(0); }
                    db.updatePos(pos);
                }

                // Hitung ulang bobot
                SpkService.hitungBobotRankReciprocal(posList);
                db.updateBanyakPos(posList);
                adapter.notifyDataSetChanged();
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    public void tambahPosBaru() {
        PosKeuangan baru = new PosKeuangan("", "", "savings", posList.size() + 1, 5.0, 0, "#1D9E75", userId);
        showDialogEditPos(baru);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
