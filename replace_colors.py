import os
import re

mapping = {
    # Whites/Backgrounds
    'Color(0xFFFFFFFF)': 'androidx.compose.material3.MaterialTheme.colorScheme.surface',
    'Color(0xFFF7F9FC)': 'androidx.compose.material3.MaterialTheme.colorScheme.background',
    'Color(0xFFF9F9FF)': 'androidx.compose.material3.MaterialTheme.colorScheme.background',
    'Color(0xFFF0F4F8)': 'androidx.compose.material3.MaterialTheme.colorScheme.background',
    'Color(0xFFE2E2E9)': 'androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant',
    'Color(0xFFE8F5E9)': 'androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer',
    'Color(0xFFF0F0F7)': 'androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant',
    'Color(0xFFEEEEEE)': 'androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant',
    'Color(0xFFE3F2FD)': 'androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer',
    'Color(0xFFFFF3E0)': 'androidx.compose.material3.MaterialTheme.colorScheme.tertiaryContainer',
    'Color(0xFFFFEBEE)': 'androidx.compose.material3.MaterialTheme.colorScheme.errorContainer',
    'Color(0xFFFFF3F3)': 'androidx.compose.material3.MaterialTheme.colorScheme.errorContainer',
    'Color(0xFFECEFF1)': 'androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant',
    'Color(0xFFDDDDDD)': 'androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant',

    # Grays/Borders
    'Color(0xFFE0E0E0)': 'androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant',
    'Color(0xFF9E9E9E)': 'androidx.compose.material3.MaterialTheme.colorScheme.outline',
    'Color(0xFF44474E)': 'androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant',
    
    # Darks/Texts
    'Color(0xFF000000)': 'androidx.compose.material3.MaterialTheme.colorScheme.onSurface',
    'Color(0xFF191C20)': 'androidx.compose.material3.MaterialTheme.colorScheme.onBackground',
    'Color(0xFF111318)': 'androidx.compose.material3.MaterialTheme.colorScheme.onSurface',
    'Color(0xFF2E3036)': 'androidx.compose.material3.MaterialTheme.colorScheme.onSurface',
    'Color(0xFF0D2137)': 'androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer',

    # Blues/Primary
    'Color(0xFF1F4E79)': 'androidx.compose.material3.MaterialTheme.colorScheme.primary',
    'Color(0xFF415F91)': 'androidx.compose.material3.MaterialTheme.colorScheme.primary',
    'Color(0xFF274777)': 'androidx.compose.material3.MaterialTheme.colorScheme.primary',
    'Color(0xFFAAC7FF)': 'androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer',
    'Color(0xFFD6E3FF)': 'androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer',
    'Color(0xFFDAE2F9)': 'androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer',
    'Color(0xFF3E4759)': 'androidx.compose.material3.MaterialTheme.colorScheme.secondary',
    'Color(0xFF2196F3)': 'androidx.compose.material3.MaterialTheme.colorScheme.primary',
    'Color(0xFF2E6DA4)': 'androidx.compose.material3.MaterialTheme.colorScheme.primary',
    'Color(0xFF85D1E8)': 'androidx.compose.material3.MaterialTheme.colorScheme.primary',

    # Greens/Success
    'Color(0xFF4CAF50)': 'androidx.compose.material3.MaterialTheme.colorScheme.secondary',
    
    # Reds/Errors
    'Color(0xFFD32F2F)': 'androidx.compose.material3.MaterialTheme.colorScheme.error',
    'Color(0xFFF44336)': 'androidx.compose.material3.MaterialTheme.colorScheme.error',
    'Color(0xFFE91E63)': 'androidx.compose.material3.MaterialTheme.colorScheme.error',
    'Color(0xFFFFCDD2)': 'androidx.compose.material3.MaterialTheme.colorScheme.errorContainer',
    'Color(0xFFBA1A1A)': 'androidx.compose.material3.MaterialTheme.colorScheme.error',
    'Color(0xFF93000A)': 'androidx.compose.material3.MaterialTheme.colorScheme.error',
    'Color(0xFFFFDAD6)': 'androidx.compose.material3.MaterialTheme.colorScheme.errorContainer',
    'Color(0xFFFFB4AB)': 'androidx.compose.material3.MaterialTheme.colorScheme.error',
    'Color(0xFF690005)': 'androidx.compose.material3.MaterialTheme.colorScheme.error',
    
    # Yellows/Oranges/Warnings
    'Color(0xFFFF9800)': 'androidx.compose.material3.MaterialTheme.colorScheme.tertiary',
    'Color(0xFFFFC107)': 'androidx.compose.material3.MaterialTheme.colorScheme.tertiary',

    # Others
    'Color(0xFF565F71)': 'androidx.compose.material3.MaterialTheme.colorScheme.secondary',
    'Color(0xFF705575)': 'androidx.compose.material3.MaterialTheme.colorScheme.tertiary',
    'Color(0xFFFAD8FD)': 'androidx.compose.material3.MaterialTheme.colorScheme.tertiaryContainer',
    'Color(0xFF573E5C)': 'androidx.compose.material3.MaterialTheme.colorScheme.onTertiaryContainer',
}

def replace_in_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original = content
    
    # Replace `private val Name = Color(...)`
    def replacer(m):
        name = m.group(1)
        color_str = m.group(2)
        if color_str in mapping:
            return f"val {name} @androidx.compose.runtime.Composable get() = {mapping[color_str]}"
        return m.group(0)
        
    content = re.sub(r'private val\s+(\w+)\s*=\s*(Color\(0x[A-Fa-f0-9]+\))', replacer, content)
    
    # Replace any inline remaining colors
    for color_str, theme_str in mapping.items():
        content = content.replace(color_str, theme_str)
        
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
