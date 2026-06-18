
package com.ubaidfauzan.dompetkeluarga.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.ubaidfauzan.dompetkeluarga.R;
import com.ubaidfauzan.dompetkeluarga.database.DatabaseHelper;
import com.ubaidfauzan.dompetkeluarga.models.UserProfile;

public class LoginActivity extends AppCompatActivity {

    private StringBuilder pinInput = new StringBuilder();
    private TextView tvError;
    private View[] pinDots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tvError = findViewById(R.id.tv_error);

        pinDots = new View[]{
            findViewById(R.id.dot_1),
            findViewById(R.id.dot_2),
            findViewById(R.id.dot_3),
            findViewById(R.id.dot_4)
        };

        int[] angkaIds = {R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
                          R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9};

        for (int i = 0; i < angkaIds.length; i++) {
            final String angka = String.valueOf(i);
            Button btn = findViewById(angkaIds[i]);
            btn.setOnClickListener(v -> tekanAngka(angka));
        }

        findViewById(R.id.btn_hapus).setOnClickListener(v -> hapusAngka());
    }

    private void tekanAngka(String angka) {
        if (pinInput.length() < 4) {
            pinInput.append(angka);
            updatePinDisplay();
            tvError.setVisibility(View.GONE);
            if (pinInput.length() == 4) verifikasiPin();
        }
    }

    private void hapusAngka() {
        if (pinInput.length() > 0) {
            pinInput.deleteCharAt(pinInput.length() - 1);
            updatePinDisplay();
        }
    }

    private void updatePinDisplay() {
        for (int i = 0; i < 4; i++) {
            if (i < pinInput.length()) {
                pinDots[i].setBackgroundResource(R.drawable.bg_pin_dot_filled);
            } else {
                pinDots[i].setBackgroundResource(R.drawable.bg_pin_dot_empty);
            }
        }
    }

    private void verifikasiPin() {
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        UserProfile user = db.getUserByPin(pinInput.toString());
        if (user == null) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText("PIN salah. Coba lagi.");
            pinInput.setLength(0);
            updatePinDisplay();
        } else {
            SharedPreferences prefs = getSharedPreferences("DompetPrefs", MODE_PRIVATE);
            prefs.edit().putInt("logged_user_id", user.getId()).apply();
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("user_id", user.getId());
            startActivity(intent);
            finish();
        }
    }
}
