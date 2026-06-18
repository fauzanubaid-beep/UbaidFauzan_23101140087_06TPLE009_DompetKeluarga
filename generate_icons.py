import os

icons = {
    'ic_cat_dana_darurat': 'M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z', # shield
    'ic_cat_tabungan': 'M19 6V5A2 2 0 0 0 17 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14c1.1 0 2-.9 2-2v-1h2v-2h-2v-4h2V8h-2V6h-2zm-2 0h2v2h-2V6zm0 6h2v2h-2v-2zm-2 7H5V5h10v14zm-6-9h4v2H9v-2z', # vault / safe (simplified to close representation, using safe/money box)
    'ic_cat_keb_pokok': 'M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z', # home
    'ic_cat_pendidikan': 'M5 13.18v4L12 21l7-3.82v-4L12 17l-7-3.82zM12 3L1 9l11 6 9-4.91V17h2V9L12 3z', # school/book
    'ic_cat_investasi': 'M16 6l2.29 2.29-4.88 4.88-4-4L2 16.59 3.41 18l6-6 4 4 6.3-6.29L22 12V6z', # trending up
    'ic_cat_hiburan': 'M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z', # star
    'ic_cat_sosial': 'M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z', # heart (favorite)
    'ic_notification': 'M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2zm-2 1H8v-6c0-2.48 1.51-4.5 4-4.5s4 2.02 4 4.5v6z', # outline notification
    'ic_upload': 'M5 20h14v-2H5v2zm0-10h4v6h6v-6h4l-7-7-7 7z',
    'ic_download': 'M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z',
    'ic_chevron_left': 'M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z',
    'ic_chevron_right': 'M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z'
}

template = '''<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24.0"
    android:viewportHeight="24.0">
    <path
        android:fillColor="#FFFFFFFF"
        android:pathData="{path}"/>
</vector>'''

base_path = r'c:\\Users\\lenovo\\Downloads\\New folder (8)\\dompet_keluarga_java\\app\\src\\main\\res\\drawable'
for name, path in icons.items():
    with open(os.path.join(base_path, name + '.xml'), 'w') as f:
        f.write(template.format(path=path))

print("Vector icons generated successfully.")
