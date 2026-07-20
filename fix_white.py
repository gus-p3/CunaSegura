import os
import re

def replace_in_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original = content
    
    # Replace Card backgrounds explicitly set to White
    content = re.sub(r'containerColor\s*=\s*Color\.White', 'containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface', content)
    
    # Replace other Color.White with onPrimary (since they are usually text on primary background)
    content = re.sub(r'Color\.White', 'androidx.compose.material3.MaterialTheme.colorScheme.onPrimary', content)
    
    # Replace Color.Black with onSurface
    content = re.sub(r'Color\.Black', 'androidx.compose.material3.MaterialTheme.colorScheme.onSurface', content)
    
    # Fix potential double replacements if some imports were already there
    content = content.replace('androidx.compose.material3.androidx.compose.material3.', 'androidx.compose.material3.')
    
    if content != original:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Updated {filepath}")

for root, dirs, files in os.walk(r'c:\Users\brand\Documentos\GIDS6092\EjerciciosTacho\CunaSegura'):
    if 'build' in root or '.gradle' in root:
        continue
    for file in files:
        if file.endswith('.kt') and 'Color.kt' not in file and 'Theme.kt' not in file:
            replace_in_file(os.path.join(root, file))
