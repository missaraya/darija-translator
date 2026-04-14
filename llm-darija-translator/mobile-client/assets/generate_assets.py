"""
generate_assets.py — Creates the required Expo app asset images.

Run once before starting the app:
    pip install Pillow
    python generate_assets.py

Generates:
  icon.png          1024x1024  — App icon
  splash.png        1024x1024  — Splash screen image
  adaptive-icon.png 1024x1024  — Android adaptive icon foreground
  favicon.png         48x48   — Web favicon
"""

from PIL import Image, ImageDraw
import os

OUTPUT_DIR = os.path.dirname(os.path.abspath(__file__))

BG_COLOR   = (43, 108, 176)    # #2B6CB0 — brand blue
TEXT_COLOR = (255, 255, 255)


def draw_crescent(draw, size, bg_color):
    """Draw a simple crescent moon centred in the image."""
    margin = size // 6
    # Full moon circle
    draw.ellipse(
        [margin, margin, size - margin, size - margin],
        fill=(255, 255, 255, 230)
    )
    # Overlap circle to create crescent
    offset = size // 5
    draw.ellipse(
        [margin + offset, margin - offset // 2,
         size - margin + offset, size - margin - offset // 2],
        fill=bg_color
    )


def create_image(size: int, filename: str, with_padding: bool = False):
    """Create a branded PNG at the given square size."""
    img  = Image.new("RGBA", (size, size), BG_COLOR)
    draw = ImageDraw.Draw(img)

    if with_padding:
        # For splash: draw a smaller moon so it looks centred on a large canvas
        inner = Image.new("RGBA", (size // 2, size // 2), BG_COLOR)
        inner_draw = ImageDraw.Draw(inner)
        draw_crescent(inner_draw, size // 2, BG_COLOR)
        # Paste centred
        offset_x = (size - size // 2) // 2
        offset_y = (size - size // 2) // 2
        img.paste(inner, (offset_x, offset_y), inner)
    else:
        draw_crescent(draw, size, BG_COLOR)

    out_path = os.path.join(OUTPUT_DIR, filename)
    img.save(out_path, "PNG")
    print(f"  Created  {out_path}  ({size}x{size})")


if __name__ == "__main__":
    print("Generating Expo assets…")
    create_image(1024, "icon.png")
    create_image(1024, "splash.png",        with_padding=True)
    create_image(1024, "adaptive-icon.png")
    create_image(48,   "favicon.png")
    print("Done — all 4 assets created.")
