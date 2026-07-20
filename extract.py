import json

with open(r'C:\Users\brand\.gemini\antigravity\brain\c932149a-5c1b-4e08-b093-1ee07d1c6dbd\.system_generated\logs\transcript_full.jsonl', encoding='utf-8') as f:
    for line in f:
        if '"type":"USER_INPUT"' in line:
            data = json.loads(line)
            content = data.get('content', '')
            if 'Todas las pantallas del telefono' in content:
                # Extract the code block
                start = content.find('package com.example.compose')
                if start != -1:
                    code = content[start:]
                    with open('new_colors.kt', 'w', encoding='utf-8') as out:
                        out.write(code)
