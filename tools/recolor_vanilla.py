"""Builds the Enderite armor and Aether Wings textures by recoloring vanilla netherite armor
and the vanilla elytra with a violet gradient map (shading kept), plus our own details on top.

Run:  python3 tools/recolor_vanilla.py <minecraft-client.jar> src/main/resources/assets/voidechoes
"""
import io
import os
import sys
import zipfile

import numpy as np
from PIL import Image

JAR, ROOT = sys.argv[1], sys.argv[2]
TEX = os.path.join(ROOT, "textures")
z = zipfile.ZipFile(JAR)


def vanilla(path):
    return np.array(Image.open(io.BytesIO(z.read("assets/minecraft/textures/" + path + ".png"))).convert("RGBA")).astype(float)


def hexc(h):
    h = h.lstrip("#")
    return np.array([int(h[i:i + 2], 16) for i in (0, 2, 4)], dtype=float)


def gradient_map(img, stops):
    """Map each pixel's brightness (normalised to the texture's own range) onto a colour ramp."""
    out = img.copy()
    mask = img[:, :, 3] > 0
    lum = img[:, :, :3] @ np.array([0.299, 0.587, 0.114])
    lo, hi = lum[mask].min(), lum[mask].max()
    t = np.clip((lum - lo) / max(hi - lo, 1), 0, 1)
    pos = np.linspace(0, 1, len(stops))
    cols = np.array([hexc(c) for c in stops])
    for ch in range(3):
        out[:, :, ch] = np.interp(t, pos, cols[:, ch])
    out[~mask] = 0
    return out


def save(img, rel):
    p = os.path.join(TEX, rel + ".png")
    os.makedirs(os.path.dirname(p), exist_ok=True)
    Image.fromarray(np.clip(img, 0, 255).astype(np.uint8), "RGBA").save(p)


ARMOR = ["#120a26", "#2a1650", "#45247e", "#6a3cb0", "#9a6ae0", "#d4b8ff"]
WINGS = ["#100a30", "#1e1660", "#302590", "#4a3cc0", "#6e5ee0", "#a898f4"]

# Armor layers: netherite shape + violet ramp; keep our own arm guards ("gloves") on top.
GLOVES = os.path.join(os.path.dirname(os.path.abspath(__file__)), "enderite_gloves.png")
gloves = np.array(Image.open(GLOVES).convert("RGBA")).astype(float)
l1 = gradient_map(vanilla("models/armor/netherite_layer_1"), ARMOR)
gm = gloves[:, :, 3] > 0
region = l1[16:32, 40:56]
region[gm] = gloves[gm]
l1[16:32, 40:56] = region
save(l1, "models/armor/enderite_layer_1")
save(gradient_map(vanilla("models/armor/netherite_layer_2"), ARMOR), "models/armor/enderite_layer_2")

for piece in ("helmet", "chestplate", "leggings", "boots"):
    save(gradient_map(vanilla(f"item/netherite_{piece}"), ARMOR), f"item/enderite_{piece}")

# Wings: elytra shape + indigo ramp, a sprinkle of stars; glow layer = stars only.
el = gradient_map(vanilla("entity/elytra"), WINGS)
glow = np.zeros_like(el)
rng = np.random.default_rng(77)
ys, xs = np.nonzero(el[:, :, 3] > 0)
for i in rng.choice(len(xs), size=min(14, len(xs)), replace=False):
    x, y = xs[i], ys[i]
    el[y, x, :3] = hexc("#e8ffff")
    glow[y, x] = [150, 230, 255, 255]
save(el, "entity/aether_wings")
save(glow, "entity/aether_wings_glow")

icon = gradient_map(vanilla("item/elytra"), WINGS)
ys, xs = np.nonzero(icon[:, :, 3] > 0)
for i in rng.choice(len(xs), size=5, replace=False):
    icon[ys[i], xs[i], :3] = hexc("#e8ffff")
save(icon, "item/aether_wings")
print("recolored textures written")
