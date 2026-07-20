import os
import re

for root, dirs, files in os.walk(r'c:\Users\brand\Documentos\GIDS6092\EjerciciosTacho\CunaSegura'):
    if 'build' in root or '.gradle' in root:
        continue
    for file in files:
        if file.endswith('.kt'):
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            original = content
            content = re.sub(r'^(?!\s*private\s+)val (\w+) @androidx\.compose\.runtime\.Composable', r'private val \1 @androidx.compose.runtime.Composable', content, flags=re.MULTILINE)
            
            if 'cunaseguratv' in filepath:
                content = content.replace('androidx.compose.material3.MaterialTheme.colorScheme', 'androidx.tv.material3.MaterialTheme.colorScheme')
            if 'cunasegurawear' in filepath:
                content = content.replace('androidx.compose.material3.MaterialTheme.colorScheme', 'androidx.wear.compose.material.MaterialTheme.colors')
                
            if content != original:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(content)
                print(f"Fixed {filepath}")
