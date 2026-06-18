import urllib.request
import os

font_dir = "app/src/main/res/font"

headers = {'User-Agent': 'Mozilla/5.0 (Linux; U; Android 4.1.1; en-gb; Build/JRO03C) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30'}

# Direct URL from Google Fonts CDN
url = "https://fonts.gstatic.com/s/nunito/v32/XRXI3I6Li01BKofiOc5wtlZ2di8HDOumRTM9iI7f.ttf"
path = os.path.join(font_dir, "nunito_bold.ttf")

req = urllib.request.Request(url, headers=headers)
try:
    with urllib.request.urlopen(req) as response:
        data = response.read()
        with open(path, 'wb') as f:
            f.write(data)
        print(f"Success: nunito_bold.ttf ({len(data)} bytes)")
except Exception as e:
    # Try alternate approach
    print(f"Direct failed: {e}")
    import re
    css_url = "https://fonts.googleapis.com/css2?family=Nunito:wght@700"
    req2 = urllib.request.Request(css_url, headers=headers)
    with urllib.request.urlopen(req2) as response:
        css = response.read().decode('utf-8')
        urls = re.findall(r'url\((https://[^)]+)\)', css)
        if urls:
            font_url = urls[0]
            print(f"CSS URL: {font_url}")
            urllib.request.urlretrieve(font_url, path)
            print(f"Success via CSS: nunito_bold.ttf")
        else:
            print("No URL found in CSS")
