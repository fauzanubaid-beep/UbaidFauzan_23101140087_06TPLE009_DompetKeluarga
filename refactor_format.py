import os
import re

java_dir = r"c:\Users\lenovo\Downloads\New folder (8)\dompet_keluarga_java\app\src\main\java"

for root, dirs, files in os.walk(java_dir):
    for file in files:
        if file.endswith(".java"):
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()

            original_content = content
            
            # Replace SpkService.formatRp -> CurrencyFormatter.format
            content = content.replace('SpkService.formatRp(', 'CurrencyFormatter.format(')
            
            # Replace manual string concatenation for Rp
            # e.g., "Rp " + (long) pos.getTargetNominal()
            content = re.sub(r'"Rp "\s*\+\s*\(long\)\s*([^ \n+]+)', r'CurrencyFormatter.format(\1)', content)
            
            # Check if any change was made
            if content != original_content:
                # Add import if missing
                import_stmt = "import com.ubaidfauzan.dompetkeluarga.utils.CurrencyFormatter;"
                if import_stmt not in content:
                    # Insert after the last import or package
                    if "import " in content:
                        content = re.sub(r'(import [^;]+;\n)(?!.*import )', r'\1' + import_stmt + '\n', content, count=1, flags=re.DOTALL)
                    elif "package " in content:
                        content = re.sub(r'(package [^;]+;\n)', r'\1\n' + import_stmt + '\n', content, count=1)

                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(content)
                print(f"Updated {file}")
