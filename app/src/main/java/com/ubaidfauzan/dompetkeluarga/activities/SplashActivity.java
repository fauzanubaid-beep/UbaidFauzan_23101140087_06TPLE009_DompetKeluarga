
package com.ubaidfauzan.dompetkeluarga.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.ubaidfauzan.dompetkeluarga.database.DatabaseHelper;
import com.ubaidfauzan.dompetkeluarga.models.UserProfile;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Handler().postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("DompetPrefs", MODE_PRIVATE);
            boolean isFirstRun = prefs.getBoolean("is_first_run", true);
            
            if (isFirstRun) {
                startActivity(new Intent(this, IntroActivity.class));
            } else {
                DatabaseHelper db = DatabaseHelper.getInstance(this);
                List<UserProfile> users = db.getAllUsers();
                if (users.isEmpty()) {
                    startActivity(new Intent(this, OnboardingActivity.class));
                } else {
                    startActivity(new Intent(this, LoginActivity.class));
                }
            }
            finish();
        }, 800);
    }
}
