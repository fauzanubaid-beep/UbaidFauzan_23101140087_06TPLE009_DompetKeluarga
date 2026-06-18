import urllib.request
import os
import re

font_dir = "app/src/main/res/font"
os.makedirs(font_dir, exist_ok=True)

headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36'}

fonts_to_download = {
    "plus_jakarta_sans_regular.ttf": "400",
    "plus_jakarta_sans_medium.ttf": "500",
    "plus_jakarta_sans_semibold.ttf": "600",
    "plus_jakarta_sans_bold.ttf": "700"
}

for filename, weight in fonts_to_download.items():
    path = os.path.join(font_dir, filename)
    css_url = f"https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@{weight}"
    print(f"Fetching CSS for {filename} (weight {weight}): {css_url}")
    
    req = urllib.request.Request(css_url, headers=headers)
    try:
        with urllib.request.urlopen(req) as response:
            css = response.read().decode('utf-8')
            # Look for the .ttf or .woff2 URL. Since we want ttf, we should try to specify a generic user agent that gets TTF.
            # But the user agent above might get woff2. 
            # Let's change the user agent to an older one to force TTF.
            pass
    except Exception as e:
        print(f"Error fetching CSS: {e}")

# Re-fetching with Android User Agent to force TTF
headers_ttf = {'User-Agent': 'Mozilla/5.0 (Linux; U; Android 4.1.1; en-gb; Build/JRO03C) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30'}

for filename, weight in fonts_to_download.items():
    path = os.path.join(font_dir, filename)
    css_url = f"https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@{weight}"
    
    req = urllib.request.Request(css_url, headers=headers_ttf)
    try:
        with urllib.request.urlopen(req) as response:
            css = response.read().decode('utf-8')
            urls = re.findall(r'url\((https://[^)]+)\)', css)
            if urls:
                font_url = urls[0]
                print(f"Downloading {filename} from {font_url}")
                urllib.request.urlretrieve(font_url, path)
                print(f"Success: {filename}")
            else:
                print(f"No URL found in CSS for {filename}")
    except Exception as e:
        print(f"Error fetching TTF for {filename}: {e}")
