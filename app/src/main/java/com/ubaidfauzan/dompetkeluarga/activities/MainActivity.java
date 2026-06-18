
package com.ubaidfauzan.dompetkeluarga.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.ubaidfauzan.dompetkeluarga.R;
import com.ubaidfauzan.dompetkeluarga.database.DatabaseHelper;
import com.ubaidfauzan.dompetkeluarga.fragments.BerandaFragment;
import com.ubaidfauzan.dompetkeluarga.fragments.PosFragment;
import com.ubaidfauzan.dompetkeluarga.fragments.LaporanFragment;
import com.ubaidfauzan.dompetkeluarga.models.UserProfile;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    private int userId;
    private UserProfile user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userId = getIntent().getIntExtra("user_id", -1);
        user = DatabaseHelper.getInstance(this).getUserById(userId);

        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        FloatingActionButton fab = findViewById(R.id.fab_tambah);

        loadFragment(new BerandaFragment(userId));
        fab.hide(); // Hide global FAB on Beranda because Beranda has its own stacked FABs

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_beranda) {
                loadFragment(new BerandaFragment(userId));
                fab.hide();
            } else if (id == R.id.nav_pos) {
                loadFragment(new PosFragment(userId));
                fab.hide();
            } else if (id == R.id.nav_laporan) {
                loadFragment(new LaporanFragment(userId));
                fab.hide();
            } else if (id == R.id.nav_transaksi) {
                loadFragment(new com.ubaidfauzan.dompetkeluarga.fragments.TransaksiFragment(userId));
                fab.show();
            }
            return true;
        });

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, TambahTransaksiActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, fragment).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh fragment aktif
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (current instanceof BerandaFragment) ((BerandaFragment) current).refresh();
        else if (current instanceof PosFragment) ((PosFragment) current).refresh();
        else if (current instanceof com.ubaidfauzan.dompetkeluarga.fragments.TransaksiFragment) ((com.ubaidfauzan.dompetkeluarga.fragments.TransaksiFragment) current).refresh();
    }
}
