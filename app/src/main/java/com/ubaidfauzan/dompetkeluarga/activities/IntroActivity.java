package com.ubaidfauzan.dompetkeluarga.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.button.MaterialButton;
import com.ubaidfauzan.dompetkeluarga.R;
import com.ubaidfauzan.dompetkeluarga.database.DatabaseHelper;
import com.ubaidfauzan.dompetkeluarga.models.UserProfile;
import java.util.List;

public class IntroActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout layoutDots;
    private MaterialButton btnNext;
    private TextView[] dots;
    private IntroAdapter introAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        viewPager = findViewById(R.id.viewPager);
        layoutDots = findViewById(R.id.layoutDots);
        btnNext = findViewById(R.id.btn_next);

        introAdapter = new IntroAdapter();
        viewPager.setAdapter(introAdapter);

        addDotsIndicator(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                addDotsIndicator(position);
                if (position == introAdapter.getItemCount() - 1) {
                    btnNext.setText("Ke Form Login / Daftar");
                } else {
                    btnNext.setText("Selanjutnya");
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() + 1 < introAdapter.getItemCount()) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                SharedPreferences prefs = getSharedPreferences("DompetPrefs", MODE_PRIVATE);
                prefs.edit().putBoolean("is_first_run", false).apply();
                
                DatabaseHelper db = DatabaseHelper.getInstance(IntroActivity.this);
                List<UserProfile> users = db.getAllUsers();
                if (users.isEmpty()) {
                    startActivity(new Intent(IntroActivity.this, OnboardingActivity.class));
                } else {
                    startActivity(new Intent(IntroActivity.this, LoginActivity.class));
                }
                finish();
            }
        });
    }

    private void addDotsIndicator(int position) {
        dots = new TextView[introAdapter.getItemCount()];
        layoutDots.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                dots[i].setText(Html.fromHtml("&#8226;", Html.FROM_HTML_MODE_LEGACY));
            } else {
                dots[i].setText(Html.fromHtml("&#8226;"));
            }
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.color_muted));
            layoutDots.addView(dots[i]);
        }
        if (dots.length > 0) {
            dots[position].setTextColor(getResources().getColor(R.color.color_navy));
        }
    }

    private static class IntroAdapter extends RecyclerView.Adapter<IntroAdapter.IntroViewHolder> {

        private final int[] images = {
            R.drawable.img_intro_1,
            R.drawable.img_intro_2,
            R.drawable.img_intro_3
        };

        private final String[] titles = {
            "Catat Transaksi",
            "Alokasi Cerdas",
            "Capai Target Finansial"
        };

        private final String[] descriptions = {
            "Pantau setiap transaksi pengeluaran dan pemasukan keluarga dengan mudah dan rapi.",
            "Buat pos keuangan dan alokasikan dana sesuai prioritas kebutuhan keluarga Anda.",
            "Wujudkan tujuan finansial keluarga Anda dengan perencanaan yang matang dan konsisten."
        };

        @NonNull
        @Override
        public IntroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_intro, parent, false);
            return new IntroViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull IntroViewHolder holder, int position) {
            holder.image.setImageResource(images[position]);
            holder.title.setText(titles[position]);
            holder.description.setText(descriptions[position]);
        }

        @Override
        public int getItemCount() {
            return titles.length;
        }

        static class IntroViewHolder extends RecyclerView.ViewHolder {
            ImageView image;
            TextView title, description;

            IntroViewHolder(@NonNull View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.image_intro);
                title = itemView.findViewById(R.id.text_title);
                description = itemView.findViewById(R.id.text_description);
            }
        }
    }
}
