import os
import shutil

base_dir = r"c:\Users\lenovo\Downloads\New folder (8)\dompet_keluarga_java"
old_pkg = "com.dompetkeluarga.app"
new_pkg = "com.ubaidfauzan.dompetkeluarga"

def replace_in_file(filepath):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        if old_pkg in content:
            new_content = content.replace(old_pkg, new_pkg)
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(new_content)
            print(f"Updated: {filepath}")
    except Exception as e:
        print(f"Error reading {filepath}: {e}")

# Update file contents
for root, dirs, files in os.walk(base_dir):
    if '.git' in root or '.idea' in root or 'build' in root or '.gradle' in root:
        continue
    for file in files:
        if file.endswith(('.java', '.xml', '.gradle', '.properties')):
            filepath = os.path.join(root, file)
            replace_in_file(filepath)

# Move directory
old_dir = os.path.join(base_dir, r"app\src\main\java\com\dompetkeluarga\app")
new_dir_parent = os.path.join(base_dir, r"app\src\main\java\com\ubaidfauzan")
new_dir = os.path.join(base_dir, r"app\src\main\java\com\ubaidfauzan\dompetkeluarga")

if os.path.exists(old_dir):
    os.makedirs(new_dir_parent, exist_ok=True)
    shutil.move(old_dir, new_dir)
    print(f"Moved directory from {old_dir} to {new_dir}")
    
    # check if com/dompetkeluarga is empty, if so delete
    parent_old = os.path.join(base_dir, r"app\src\main\java\com\dompetkeluarga")
    if os.path.exists(parent_old) and not os.listdir(parent_old):
        os.rmdir(parent_old)
        print(f"Removed empty directory {parent_old}")
