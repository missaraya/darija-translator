"""
generate_icons.py — Creates simple PNG icons for the Chrome extension.

Run this script once before loading the extension in Chrome:
    python generate_icons.py

Requirements: Pillow
    pip install Pillow
"""

from PIL import Image, ImageDraw, ImageFont
import os

SIZES = [16, 48, 128]
OUTPUT_DIR = os.path.dirname(os.path.abspath(__file__))

# Extension brand color: blue gradient
BG_COLOR    = (43, 108, 176)   # #2B6CB0
TEXT_COLOR  = (255, 255, 255)

def create_icon(size: int):
    img  = Image.new("RGBA", (size, size), BG_COLOR)
    draw = ImageDraw.Draw(img)

    # Draw a simple crescent moon symbol that fits in the icon
    moon_margin = max(2, size // 8)
    draw.ellipse(
        [moon_margin, moon_margin, size - moon_margin, size - moon_margin],
        fill=(255, 255, 255, 230)
    )
    # Inner circle to create crescent effect
    offset = size // 5
    draw.ellipse(
        [moon_margin + offset, moon_margin - offset // 2,
         size - moon_margin + offset, size - moon_margin - offset // 2],
        fill=BG_COLOR
    )

    out_path = os.path.join(OUTPUT_DIR, f"icon{size}.png")
    img.save(out_path, "PNG")
    print(f"Created {out_path}")

if __name__ == "__main__":
    for s in SIZES:
        create_icon(s)
    print("All icons generated successfully.")
