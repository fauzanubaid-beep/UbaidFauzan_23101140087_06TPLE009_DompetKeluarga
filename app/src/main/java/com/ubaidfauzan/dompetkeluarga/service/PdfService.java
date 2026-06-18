
package com.ubaidfauzan.dompetkeluarga.service;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import com.ubaidfauzan.dompetkeluarga.models.*;
import com.ubaidfauzan.dompetkeluarga.spk.SpkService;
import java.io.*;
import java.util.List;
import com.ubaidfauzan.dompetkeluarga.utils.CurrencyFormatter;

// Menggunakan Android built-in PdfDocument (tidak perlu library eksternal)
public class PdfService {

    private static final String[] NAMA_BULAN = {"","Januari","Februari","Maret","April",
        "Mei","Juni","Juli","Agustus","September","Oktober","November","Desember"};

    public static String generateLaporan(Context ctx, UserProfile user,
            List<PosSummary> summaries, List<Transaksi> transaksiList, int bulan, int tahun) throws IOException {

        PdfDocument doc = new PdfDocument();
        int W = 595, H = 842; // A4 dalam points (72 dpi)

        // ── Halaman 1: Ringkasan ──────────────────────────
        PdfDocument.PageInfo pageInfo1 = new PdfDocument.PageInfo.Builder(W, H, 1).create();
        PdfDocument.Page page1 = doc.startPage(pageInfo1);
        android.graphics.Canvas canvas = page1.getCanvas();

        int y = 40;
        Paint pTitle = new Paint(); pTitle.setTextSize(16); pTitle.setFakeBoldText(true); pTitle.setColor(Color.WHITE);
        Paint pSub   = new Paint(); pSub.setTextSize(11);   pSub.setColor(Color.parseColor("#A8D5B5"));
        Paint pHead  = new Paint(); pHead.setTextSize(11);  pHead.setFakeBoldText(true); pHead.setColor(Color.WHITE);
        Paint pBody  = new Paint(); pBody.setTextSize(10);  pBody.setColor(Color.BLACK);
        Paint pGreen = new Paint(); pGreen.setTextSize(10); pGreen.setColor(Color.parseColor("#0F6E56")); pGreen.setFakeBoldText(true);
        Paint pRed   = new Paint(); pRed.setTextSize(10);   pRed.setColor(Color.RED); pRed.setFakeBoldText(true);
        Paint bgGreen= new Paint(); bgGreen.setColor(Color.parseColor("#1A3A2A"));
        Paint bgGray = new Paint(); bgGray.setColor(Color.parseColor("#F5F7F6"));
        Paint bgAlt  = new Paint(); bgAlt.setColor(Color.parseColor("#EAF5EE"));
        Paint lnPaint= new Paint(); lnPaint.setColor(Color.parseColor("#DDDDDD")); lnPaint.setStrokeWidth(0.5f);

        // Header box
        canvas.drawRect(30, y-12, W-30, y+36, bgGreen);
        canvas.drawText("LAPORAN KEUANGAN KELUARGA", 40, y+8, pTitle);
        canvas.drawText(NAMA_BULAN[bulan] + " " + tahun + "  ·  Keluarga " + user.getNama(), 40, y+24, pSub);
        y += 52;

        // Ringkasan 3 kotak
        double totalKeluar = 0;
        for (PosSummary s : summaries) totalKeluar += s.getTerpakai();
        double sisa = user.getPemasukanBulanan() - totalKeluar;

        drawStatBox(canvas, 30, y, 170, "Pemasukan", CurrencyFormatter.format(user.getPemasukanBulanan()), Color.parseColor("#1D9E75"), bgGray);
        drawStatBox(canvas, 210, y, 170, "Pengeluaran", CurrencyFormatter.format(totalKeluar), Color.parseColor("#D85A30"), bgGray);
        drawStatBox(canvas, 390, y, 175, sisa >= 0 ? "Surplus" : "Defisit", CurrencyFormatter.format(Math.abs(sisa)),
            sisa >= 0 ? Color.parseColor("#0F6E56") : Color.RED, bgGray);
        y += 60;

        // Tabel pos
        Paint pTh = new Paint(); pTh.setTextSize(10); pTh.setFakeBoldText(true); pTh.setColor(Color.WHITE);
        canvas.drawRect(30, y, W-30, y+20, bgGreen);
        canvas.drawText("Pos", 36, y+13, pTh);
        canvas.drawText("Alokasi", 186, y+13, pTh);
        canvas.drawText("Terpakai", 276, y+13, pTh);
        canvas.drawText("Sisa", 366, y+13, pTh);
        canvas.drawText("%", 466, y+13, pTh);
        y += 22;

        for (int i = 0; i < summaries.size(); i++) {
            PosSummary s = summaries.get(i);
            Paint bg = (i % 2 == 0) ? new Paint() : bgGray;
            bg.setColor(i % 2 == 0 ? Color.WHITE : Color.parseColor("#F5F7F6"));
            canvas.drawRect(30, y, W-30, y+18, bg);
            canvas.drawText(s.getPos().getNama(), 36, y+12, pBody);
            canvas.drawText(CurrencyFormatter.format(s.getAlokasi()), 186, y+12, pBody);
            canvas.drawText(CurrencyFormatter.format(s.getTerpakai()), 276, y+12, pBody);
            canvas.drawText(s.isMinus() ? "-"+CurrencyFormatter.format(Math.abs(s.getSisa())) : CurrencyFormatter.format(s.getSisa()),
                366, y+12, s.isMinus() ? pRed : pGreen);
            canvas.drawText(String.format("%.0f%%", s.getPersenTerpakai()*100), 466, y+12, pBody);
            y += 18;
        }
        // Total row
        canvas.drawRect(30, y, W-30, y+18, bgAlt);
        canvas.drawText("TOTAL", 36, y+12, pGreen);
        canvas.drawText(CurrencyFormatter.format(user.getPemasukanBulanan()), 186, y+12, pGreen);
        canvas.drawText(CurrencyFormatter.format(totalKeluar), 276, y+12, pGreen);
        canvas.drawText(CurrencyFormatter.format(Math.abs(sisa)), 366, y+12, sisa >= 0 ? pGreen : pRed);
        y += 28;

        // Footer
        canvas.drawLine(30, H-30, W-30, H-30, lnPaint);
        Paint pFooter = new Paint(); pFooter.setTextSize(8); pFooter.setColor(Color.GRAY);
        canvas.drawText("Digenerate oleh DompetKeluarga · SPK Keuangan Rumah Tangga", 30, H-15, pFooter);

        doc.finishPage(page1);

        // ── Halaman 2: Detail Transaksi ──────────────────
        if (!transaksiList.isEmpty()) {
            PdfDocument.PageInfo pageInfo2 = new PdfDocument.PageInfo.Builder(W, H, 2).create();
            PdfDocument.Page page2 = doc.startPage(pageInfo2);
            android.graphics.Canvas c2 = page2.getCanvas();
            int y2 = 40;

            Paint pT = new Paint(); pT.setTextSize(14); pT.setFakeBoldText(true); pT.setColor(Color.parseColor("#1A3A2A"));
            c2.drawText("DETAIL TRANSAKSI — " + NAMA_BULAN[bulan] + " " + tahun, 30, y2, pT);
            y2 += 20;

            c2.drawRect(30, y2, W-30, y2+20, bgGreen);
            c2.drawText("Tanggal", 36, y2+13, pTh);
            c2.drawText("Keterangan", 106, y2+13, pTh);
            c2.drawText("Pos", 276, y2+13, pTh);
            c2.drawText("Nominal", 386, y2+13, pTh);
            c2.drawText("Jenis", 476, y2+13, pTh);
            y2 += 22;

            for (int i = 0; i < transaksiList.size() && y2 < H - 50; i++) {
                Transaksi t = transaksiList.get(i);
                Paint bg = new Paint(); bg.setColor(i % 2 == 0 ? Color.WHITE : Color.parseColor("#F5F7F6"));
                c2.drawRect(30, y2, W-30, y2+18, bg);
                String tgl = t.getTanggal().length() >= 10 ? t.getTanggal().substring(5,10).replace("-","/") : t.getTanggal();
                c2.drawText(tgl, 36, y2+12, pBody);
                String ket = t.getCatatan() != null && !t.getCatatan().isEmpty() ? t.getCatatan() : "-";
                if (ket.length() > 22) ket = ket.substring(0, 20) + "..";
                c2.drawText(ket, 106, y2+12, pBody);
                c2.drawText(t.getNamaPos(), 276, y2+12, pBody);
                c2.drawText(CurrencyFormatter.format(t.getNominal()), 386, y2+12,
                    t.getJenis().equals("pengeluaran") ? pRed : pGreen);
                c2.drawText(t.getJenis().equals("pengeluaran") ? "Keluar" : "Masuk", 476, y2+12, pBody);
                y2 += 18;
            }

            c2.drawLine(30, H-30, W-30, H-30, lnPaint);
            Paint pF = new Paint(); pF.setTextSize(8); pF.setColor(Color.GRAY);
            c2.drawText("DompetKeluarga · SPK Keuangan Rumah Tangga", 30, H-15, pF);
            doc.finishPage(page2);
        }

        // Simpan file
        File folder = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "DompetKeluarga");
        if (!folder.exists()) folder.mkdirs();
        String fileName = "Laporan_" + NAMA_BULAN[bulan] + "_" + tahun + ".pdf";
        File file = new File(folder, fileName);
        FileOutputStream fos = new FileOutputStream(file);
        doc.writeTo(fos);
        doc.close();
        fos.close();
        return file.getAbsolutePath();
    }

    private static void drawStatBox(android.graphics.Canvas canvas, int x, int y, int w,
            String label, String value, int accentColor, Paint bgPaint) {
        bgPaint.setColor(Color.parseColor("#F5F7F6"));
        canvas.drawRect(x, y, x+w, y+46, bgPaint);
        Paint pLbl = new Paint(); pLbl.setTextSize(9);  pLbl.setColor(Color.GRAY);
        Paint pVal = new Paint(); pVal.setTextSize(13); pVal.setFakeBoldText(true); pVal.setColor(accentColor);
        canvas.drawText(label, x+8, y+16, pLbl);
        canvas.drawText(value, x+8, y+34, pVal);
    }
}
