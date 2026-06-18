package com.ubaidfauzan.dompetkeluarga.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter {

    private static final Locale LOCALE_ID = new Locale("id", "ID");

    /**
     * Format lengkap: 57000000 → "Rp 57.000.000"
     */
    public static String format(long amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(LOCALE_ID);
        formatter.setGroupingUsed(true);
        return "Rp " + formatter.format(amount);
    }

    /**
     * Format lengkap dari double: 57000000.0 → "Rp 57.000.000"
     */
    public static String format(double amount) {
        return format((long) amount);
    }

    // Contoh hasil:
    // 57000000  → "Rp 57.000.000"
    // 20156570  → "Rp 20.156.570"
    // 26000     → "Rp 26.000"
    // 6700000   → "Rp 6.700.000"
    // 500000    → "Rp 500.000"
    // 0         → "Rp 0"
}
