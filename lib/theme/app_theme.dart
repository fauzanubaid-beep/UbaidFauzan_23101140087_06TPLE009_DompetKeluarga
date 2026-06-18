import 'package:flutter/material.dart';

class AppTheme {
  // ==========================================
  // COLOR TOKENS (WAJIB DIGUNAKAN)
  // ==========================================
  static const Color colorPrimary = Color(0xFF2563EB); // AppBar, header bg, btn outline, icon aktif
  static const Color colorYellow = Color(0xFFF5C518);  // Tombol CTA utama, FAB, badge
  static const Color colorRed = Color(0xFFE84343);     // Teks judul besar, badge status, aksen highlight
  static const Color colorBackground = Color(0xFFFAF0E6); // Scaffold background
  static const Color colorSurface = Color(0xFFFFFFFF); // Card, bottom sheet, form field
  static const Color colorTextPrimary = Color(0xFF1A1A1A); // Teks utama
  static const Color colorTextSecondary = Color(0xFF8B7355); // Label, subtitle, placeholder
  static const Color colorTextOnPrimary = Color(0xFFFFFFFF); // Teks di atas colorPrimary
  static const Color colorTextOnYellow = Color(0xFF5A3E00); // Teks di atas colorYellow

  static const Color borderColor = Color(0xFFE8DDD0);

  // ==========================================
  // THEME DATA UTAMA
  // ==========================================
  static ThemeData get lightTheme {
    return ThemeData(
      useMaterial3: true,
      primaryColor: colorPrimary,
      scaffoldBackgroundColor: colorBackground,
      colorScheme: const ColorScheme.light(
        primary: colorPrimary,
        secondary: colorYellow,
        surface: colorSurface,
        error: colorRed,
        onPrimary: colorTextOnPrimary,
        onSecondary: colorTextOnYellow,
        onSurface: colorTextPrimary,
        onError: colorSurface,
      ),

      // 1. AppBar: background colorPrimary, foregroundColor putih, elevation 0
      appBarTheme: const AppBarTheme(
        backgroundColor: colorPrimary,
        foregroundColor: colorTextOnPrimary,
        elevation: 0,
        centerTitle: true,
      ),

      // 2. ElevatedButton: background colorYellow, teks colorTextOnYellow, border radius 32
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: colorYellow,
          foregroundColor: colorTextOnYellow,
          elevation: 0,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(32),
          ),
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
          textStyle: const TextStyle(
            fontWeight: FontWeight.bold,
            fontSize: 16,
          ),
        ),
      ),

      // 6. Card: background colorSurface, border tipis #E8DDD0, borderRadius 16, no shadow
      cardTheme: CardTheme(
        color: colorSurface,
        elevation: 0,
        margin: EdgeInsets.zero,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
          side: const BorderSide(color: borderColor, width: 1),
        ),
      ),

      // 5. BottomNavigationBar: background putih, selectedItemColor colorPrimary, unselectedItemColor abu
      bottomNavigationBarTheme: const BottomNavigationBarThemeData(
        backgroundColor: colorSurface,
        selectedItemColor: colorPrimary,
        unselectedItemColor: Colors.grey,
        type: BottomNavigationBarType.fixed,
        elevation: 8,
      ),

      // 4. FAB (+ button): background colorYellow, foreground colorTextOnYellow
      floatingActionButtonTheme: const FloatingActionButtonThemeData(
        backgroundColor: colorYellow,
        foregroundColor: colorTextOnYellow,
        elevation: 4,
        shape: CircleBorder(),
      ),

      // InputDecorationTheme (Form field background dll)
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: colorSurface,
        labelStyle: const TextStyle(color: colorTextSecondary),
        hintStyle: const TextStyle(color: colorTextSecondary),
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: borderColor),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: borderColor),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: colorPrimary, width: 2),
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: colorRed, width: 1),
        ),
      ),

      // Text Theme
      textTheme: const TextTheme(
        displayLarge: TextStyle(color: colorTextPrimary, fontWeight: FontWeight.bold),
        displayMedium: TextStyle(color: colorTextPrimary, fontWeight: FontWeight.bold),
        displaySmall: TextStyle(color: colorTextPrimary, fontWeight: FontWeight.bold),
        headlineLarge: TextStyle(color: colorTextPrimary, fontWeight: FontWeight.bold),
        headlineMedium: TextStyle(color: colorTextPrimary, fontWeight: FontWeight.bold),
        headlineSmall: TextStyle(color: colorTextPrimary, fontWeight: FontWeight.bold),
        titleLarge: TextStyle(color: colorTextPrimary, fontWeight: FontWeight.w600),
        titleMedium: TextStyle(color: colorTextPrimary, fontWeight: FontWeight.w600),
        titleSmall: TextStyle(color: colorTextPrimary, fontWeight: FontWeight.w600),
        bodyLarge: TextStyle(color: colorTextPrimary),
        bodyMedium: TextStyle(color: colorTextPrimary),
        bodySmall: TextStyle(color: colorTextSecondary),
        labelLarge: TextStyle(color: colorTextPrimary, fontWeight: FontWeight.w500),
        labelMedium: TextStyle(color: colorTextSecondary, fontWeight: FontWeight.w500),
        labelSmall: TextStyle(color: colorTextSecondary, fontWeight: FontWeight.w500),
      ),
    );
  }

  // ==========================================
  // STYLES KHUSUS (SESUAI ATURAN TAMBAHAN)
  // ==========================================

  // 3. Headline besar / amount: color colorRed
  static const TextStyle headlineAmountStyle = TextStyle(
    color: colorRed,
    fontSize: 32,
    fontWeight: FontWeight.bold,
  );

  // 7. Icon pos/kategori: background colorPrimary, icon putih (TIDAK boleh hijau/orange lama)
  static BoxDecoration get categoryIconBackground => BoxDecoration(
    color: colorPrimary,
    borderRadius: BorderRadius.circular(12),
  );
  static const Color categoryIconColor = colorTextOnPrimary;

  // 8. Badge persentase: background colorYellow, teks colorTextOnYellow
  static BoxDecoration get percentageBadgeDecoration => BoxDecoration(
    color: colorYellow,
    borderRadius: BorderRadius.circular(8),
  );
  static const TextStyle percentageBadgeTextStyle = TextStyle(
    color: colorTextOnYellow,
    fontWeight: FontWeight.bold,
    fontSize: 12,
  );

  // 9. Status chip "Sehat": border dan teks colorPrimary (bukan hijau)
  static BoxDecoration get statusChipSehatDecoration => BoxDecoration(
    color: Colors.transparent,
    border: Border.all(color: colorPrimary, width: 1.5),
    borderRadius: BorderRadius.circular(16),
  );
  static const TextStyle statusChipSehatTextStyle = TextStyle(
    color: colorPrimary,
    fontWeight: FontWeight.w600,
    fontSize: 12,
  );

  // 10. Skor keuangan angka besar: color colorPrimary
  static const TextStyle scoreLargeStyle = TextStyle(
    color: colorPrimary,
    fontSize: 48,
    fontWeight: FontWeight.bold,
  );
}
