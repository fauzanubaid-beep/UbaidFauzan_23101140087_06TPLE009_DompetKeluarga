
package com.ubaidfauzan.dompetkeluarga.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ubaidfauzan.dompetkeluarga.R;
import com.ubaidfauzan.dompetkeluarga.adapters.PrioritasPosAdapter;
import com.ubaidfauzan.dompetkeluarga.database.DatabaseHelper;
import com.ubaidfauzan.dompetkeluarga.models.PosKeuangan;
import com.ubaidfauzan.dompetkeluarga.spk.SpkService;
import java.util.Collections;
import java.util.List;

public class PrioritasPosActivity extends AppCompatActivity {

    private int userId;
    private boolean isOnboarding;
    private List<PosKeuangan> posList;
    private PrioritasPosAdapter adapter;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prioritas_pos);

        userId = getIntent().getIntExtra("user_id", -1);
        isOnboarding = getIntent().getBooleanExtra("is_onboarding", false);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Urutan Prioritas Pos");
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isOnboarding);
        }

        db = DatabaseHelper.getInstance(this);
        posList = db.getPosUser(userId);

        RecyclerView rv = findViewById(R.id.rv_prioritas);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PrioritasPosAdapter(posList, this);
        rv.setAdapter(adapter);

        // Drag & drop untuk reorder
        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder from, RecyclerView.ViewHolder to) {
                int fromPos = from.getAdapterPosition();
                int toPos = to.getAdapterPosition();
                Collections.swap(posList, fromPos, toPos);
                // Update rank
                for (int i = 0; i < posList.size(); i++) posList.get(i).setPrioritasRank(i + 1);
                adapter.notifyItemMoved(fromPos, toPos);
                adapter.notifyDataSetChanged();
                return true;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {}
        };
        new ItemTouchHelper(callback).attachToRecyclerView(rv);

        Button btnSimpan = findViewById(R.id.btn_simpan);
        btnSimpan.setText(isOnboarding ? "Konfirmasi & Mulai" : "Simpan Perubahan");
        btnSimpan.setOnClickListener(v -> simpan());
    }

    private void simpan() {
        // Hitung ulang bobot dengan Rank Reciprocal
        SpkService.hitungBobotRankReciprocal(posList);
        db.updateBanyakPos(posList);
        Toast.makeText(this, "Prioritas disimpan!", Toast.LENGTH_SHORT).show();

        if (isOnboarding) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
            finish();
        } else {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
