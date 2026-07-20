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
            
            # Fix androidx.compose.ui.graphics.androidx.compose.material3.MaterialTheme.colorScheme
            content = re.sub(r'androidx\.compose\.ui\.graphics\.androidx\.compose\.material3\.MaterialTheme\.colorScheme', 'MaterialTheme.colorScheme', content)
            content = re.sub(r'androidx\.compose\.ui\.graphics\.androidx\.tv\.material3\.MaterialTheme\.colorScheme', 'androidx.tv.material3.MaterialTheme.colorScheme', content)
            content = re.sub(r'androidx\.compose\.ui\.graphics\.androidx\.wear\.compose\.material\.MaterialTheme\.colors', 'androidx.wear.compose.material.MaterialTheme.colors', content)
            
            if content != original:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(content)
                print(f"Fixed {filepath}")
