"""Procedurally paints every texture of Echoes of the Void.

Run:  python3 tools/gen_textures.py src/main/resources/assets/voidechoes
All art is generated from seeded noise and hand-placed pixel shapes, so it is
fully original and reproducible.
"""
import json
import math
import os
import sys

import numpy as np
from PIL import Image

ROOT = sys.argv[1] if len(sys.argv) > 1 else "src/main/resources/assets/voidechoes"
TEX = os.path.join(ROOT, "textures")


# ----------------------------------------------------------------- helpers
def hexc(h, a=255):
    h = h.lstrip("#")
    return np.array([int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a], dtype=float)


def canvas(w=16, h=16):
    return np.zeros((h, w, 4), dtype=float)


def noise(w, h, cell, seed, cell_y=None):
    """Tileable value noise in [0,1]."""
    cell_y = cell_y or cell
    gx, gy = max(1, w // cell), max(1, h // cell_y)
    g = np.random.default_rng(seed).random((gy, gx))
    out = np.zeros((h, w))
    for y in range(h):
        for x in range(w):
            fx, fy = x / cell, y / cell_y
            x0, y0 = int(fx) % gx, int(fy) % gy
            x1, y1 = (x0 + 1) % gx, (y0 + 1) % gy
            tx, ty = fx - int(fx), fy - int(fy)
            tx, ty = tx * tx * (3 - 2 * tx), ty * ty * (3 - 2 * ty)
            a = g[y0, x0] * (1 - tx) + g[y0, x1] * tx
            b = g[y1, x0] * (1 - tx) + g[y1, x1] * tx
            out[y, x] = a * (1 - ty) + b * ty
    return out


def fbm(w, h, seed, cells=(8, 4, 2), weights=(0.5, 0.3, 0.2), stretch=(1, 1)):
    acc = np.zeros((h, w))
    for i, (c, wt) in enumerate(zip(cells, weights)):
        acc += wt * noise(w, h, max(1, int(c * stretch[0])), seed + i * 101, max(1, int(c * stretch[1])))
    acc -= acc.min()
    return acc / max(acc.max(), 1e-6)


def posterize(values, palette):
    pal = [hexc(c) for c in palette]
    out = canvas(values.shape[1], values.shape[0])
    idx = np.clip((values * len(pal)).astype(int), 0, len(pal) - 1)
    for y in range(values.shape[0]):
        for x in range(values.shape[1]):
            out[y, x] = pal[idx[y, x]]
    return out


def save(img, rel):
    path = os.path.join(TEX, rel + ".png")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    Image.fromarray(np.clip(img, 0, 255).astype(np.uint8), "RGBA").save(path)


def save_anim(frames, rel, frametime, interpolate=True):
    save(np.concatenate(frames, axis=0), rel)
    with open(os.path.join(TEX, rel + ".png.mcmeta"), "w") as f:
        json.dump({"animation": {"frametime": frametime, "interpolate": interpolate}}, f, indent=2)


def put(img, x, y, color):
    h, w = img.shape[:2]
    if 0 <= x < w and 0 <= y < h:
        img[y, x] = color


def scale_rgb(c, k):
    out = c.copy()
    out[:3] = np.clip(out[:3] * k, 0, 255)
    return out


def mix(a, b, t):
    return a * (1 - t) + b * t


def outline(img, color):
    h, w = img.shape[:2]
    alpha = img[:, :, 3] > 0
    out = img.copy()
    for y in range(h):
        for x in range(w):
            if alpha[y, x]:
                continue
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nx, ny = x + dx, y + dy
                if 0 <= nx < w and 0 <= ny < h and alpha[ny, nx]:
                    out[y, x] = hexc(color)
                    break
    return out


def seg_dist(px, py, ax, ay, bx, by):
    abx, aby = bx - ax, by - ay
    t = ((px - ax) * abx + (py - ay) * aby) / (abx * abx + aby * aby)
    t = max(0.0, min(1.0, t))
    cx, cy = ax + abx * t, ay + aby * t
    side = (px - ax) * aby - (py - ay) * abx
    return math.hypot(px - cx, py - cy), t, side


def crystal_segment(img, a, b, width, palette, taper_base=True, glow=0.0):
    """Shaded crystal prism along segment a->b (pointed tip at b)."""
    dark, mid, light, hi = [hexc(c) for c in palette]
    for y in range(img.shape[0]):
        for x in range(img.shape[1]):
            d, t, side = seg_dist(x + 0.5, y + 0.5, *a, *b)
            taper = (1 - t) ** 0.9 if t > 0.55 else 1.0
            if taper_base and t < 0.12:
                taper = min(taper, 0.45 + t * 4.5)
            r = width * taper
            if d > r:
                continue
            if d < r * 0.35:
                c = hi if side > 0 else light
            elif side > 0:
                c = light
            else:
                c = mid if d < r * 0.75 else dark
            if glow:
                c = mix(c, hexc("#ffffff"), glow * (1 - d / max(r, 1e-3)))
            img[y, x] = c


# ----------------------------------------------------------------- blocks
def void_moss():
    v = fbm(16, 16, 11, (8, 4, 2))
    top = posterize(v, ["#1d0f33", "#2c1650", "#3e1f6b", "#552b8a", "#6c3aa6"])
    rng = np.random.default_rng(12)
    for _ in range(9):
        x, y = rng.integers(0, 16, 2)
        put(top, x, y, hexc("#9ffff4"))
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            if rng.random() < 0.5:
                put(top, (x + dx) % 16, (y + dy) % 16, hexc("#2fb7b0"))
    save(top, "block/void_moss_top")

    stone = posterize(fbm(16, 16, 13, (4, 2, 1)), ["#b8b27a", "#cbc68c", "#dad79c", "#e7e5b4"])
    edge = noise(16, 1, 2, 14)[0]
    side = stone.copy()
    for x in range(16):
        depth = 3 + int(edge[x] * 3)
        for y in range(depth):
            side[y, x] = top[y, x]
        if rng.random() < 0.3:
            put(side, x, depth, hexc("#3e1f6b"))
        if rng.random() < 0.15:
            put(side, x, depth - 1, hexc("#9ffff4"))
    save(side, "block/void_moss_side")


def voidstone_base(seed=21):
    img = posterize(fbm(16, 16, seed, (8, 4, 2)), ["#0d0b1a", "#151229", "#1d1938", "#262046"])
    rng = np.random.default_rng(seed)
    for _ in range(2):
        x, y = rng.integers(0, 16, 2)
        for _ in range(9):
            put(img, x % 16, y % 16, hexc("#3b2a6b") if rng.random() < 0.8 else hexc("#6a4fc0"))
            x += rng.integers(-1, 2)
            y += 1
    return img


def voidstone():
    save(voidstone_base(), "block/voidstone")

    base = posterize(fbm(16, 16, 31, (4, 2, 1)), ["#17142c", "#1e1a3a", "#262148", "#2d2754"])
    img = base.copy()
    mortar, hi, lo = hexc("#07060e"), hexc("#3a3268"), hexc("#100d20")
    for y in range(16):
        row = y // 4
        off = 0 if row % 2 == 0 else 4
        for x in range(16):
            lx, ly = (x + off) % 8, y % 4
            if ly == 3 or lx == 7:
                img[y, x] = mortar
            elif ly == 0 or lx == 0:
                img[y, x] = mix(img[y, x], hi, 0.6)
            elif ly == 2 or lx == 6:
                img[y, x] = mix(img[y, x], lo, 0.5)
    for x, y in ((3, 5), (11, 9), (6, 13)):
        put(img, x, y, hexc("#4b3f8c"))
    save(img, "block/voidstone_bricks")


RUNE = [
    "..X....X..",
    "..XX..XX..",
    "...XXXX...",
    "X...XX...X",
    "XX..XX..XX",
    ".XXXXXXXX.",
    "....XX....",
    "...X..X...",
    "..X....X..",
    ".XX....XX.",
]


def chiseled_voidstone():
    base = voidstone_base(41)
    for i in range(16):
        for j in range(16):
            if i in (0, 15) or j in (0, 15):
                base[j, i] = hexc("#07060e")
            elif i == 1 or j == 1:
                base[j, i] = hexc("#3a3268")
            elif i == 14 or j == 14:
                base[j, i] = hexc("#0a0815")
    frames = []
    dim, bright, white = hexc("#1b5f6b"), hexc("#5ff0f0"), hexc("#e8ffff")
    for f in range(12):
        img = base.copy()
        pulse = 0.5 + 0.5 * math.sin(f / 12 * math.tau)
        scan = (f / 12) * 14
        for ry, row in enumerate(RUNE):
            for rx, ch in enumerate(row):
                if ch != "X":
                    # faint halo around glyph strokes
                    near = any(
                        0 <= ry + dy < 10 and 0 <= rx + dx < 10 and RUNE[ry + dy][rx + dx] == "X"
                        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))
                    )
                    if near:
                        img[3 + ry, 3 + rx] = mix(img[3 + ry, 3 + rx], bright, 0.18 + 0.2 * pulse)
                    continue
                c = mix(dim, bright, 0.35 + 0.65 * pulse)
                if abs((3 + ry) - (1 + scan)) < 1.2:
                    c = mix(c, white, 0.8)
                img[3 + ry, 3 + rx] = c
        frames.append(img)
    save_anim(frames, "block/chiseled_voidstone", 3)


def ashen_sand():
    img = posterize(fbm(16, 16, 51, (4, 2, 1)), ["#8d8580", "#a39b94", "#b7afa6", "#c9c1b6"])
    rng = np.random.default_rng(52)
    for _ in range(14):
        x, y = rng.integers(0, 16, 2)
        put(img, x, y, hexc("#4a4340") if rng.random() < 0.6 else hexc("#e2dbcf"))
    save(img, "block/ashen_sand")


def aetherwood():
    bark = posterize(fbm(16, 16, 61, (8, 4, 2), stretch=(0.5, 2)), ["#1f1030", "#2e1846", "#3d2160", "#4d2a75"])
    rng = np.random.default_rng(62)
    for cx in (4, 11):
        y = rng.integers(0, 6)
        while y < 16:
            ln = rng.integers(2, 6)
            for k in range(ln):
                put(bark, cx, (y + k) % 16, hexc("#3fd9d0") if k % 2 == 0 else hexc("#2a9e9a"))
            y += ln + rng.integers(3, 7)
    save(bark, "block/aetherwood_log")

    top = canvas()
    rings = [hexc("#6a3c99"), hexc("#5a3289"), hexc("#4d2a75")]
    n = noise(16, 16, 4, 63)
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            d = max(abs(dx), abs(dy)) * 0.7 + math.hypot(dx, dy) * 0.3
            if d > 7.0:
                top[y, x] = bark[y, x]
            else:
                top[y, x] = rings[int(d * 1.1 + n[y, x] * 0.9) % 3]
            if d < 1.2:
                top[y, x] = hexc("#7ff6ee")
    save(top, "block/aetherwood_log_top")

    planks = posterize(fbm(16, 16, 64, (8, 4, 2), stretch=(2, 0.5)), ["#4a2a6b", "#55307a", "#5f3688", "#6b3f94"])
    for y in range(16):
        for x in range(16):
            board = y // 4
            if y % 4 == 3:
                planks[y, x] = hexc("#2a173f")
            elif (x + board * 5) % 16 == 0:
                planks[y, x] = hexc("#34204d")
    save(planks, "block/aetherwood_planks")


def aetherwood_leaves():
    v = fbm(16, 16, 71, (4, 2, 1))
    holes = noise(16, 16, 2, 72)
    base = posterize(v, ["#0f5b63", "#16808a", "#22a3a8", "#3cc9c4"])
    for y in range(16):
        for x in range(16):
            if holes[y, x] < 0.22:
                base[y, x] = 0
    rng = np.random.default_rng(73)
    sparks = [(int(x), int(y)) for x, y in rng.integers(0, 16, (8, 2)) if base[y, x, 3] > 0]
    frames = []
    for f in range(4):
        img = base.copy()
        for i, (x, y) in enumerate(sparks):
            k = 0.5 + 0.5 * math.sin((f / 4 + i / len(sparks)) * math.tau)
            img[y, x] = mix(hexc("#3cc9c4"), hexc("#d8fff8"), k)
        frames.append(img)
    save_anim(frames, "block/aetherwood_leaves", 10)


def resonant_crystal():
    rng = np.random.default_rng(81)
    seeds = rng.random((7, 2)) * 16
    pal = [hexc(c, 205) for c in ("#3a2a8a", "#4a4fc0", "#3fa7d6", "#6fe3f0")]
    cell_col = [pal[i % 4] for i in range(len(seeds))]
    base = canvas()
    edge = np.zeros((16, 16), dtype=bool)
    for y in range(16):
        for x in range(16):
            ds = []
            for sx, sy in seeds:
                dx = min(abs(x + 0.5 - sx), 16 - abs(x + 0.5 - sx))
                dy = min(abs(y + 0.5 - sy), 16 - abs(y + 0.5 - sy))
                ds.append(math.hypot(dx, dy))
            order = np.argsort(ds)
            base[y, x] = cell_col[order[0]]
            # shade inside each facet by distance to its seed
            base[y, x, :3] *= 1.15 - min(ds[order[0]] / 9, 0.35)
            edge[y, x] = ds[order[1]] - ds[order[0]] < 0.45
    frames = []
    for f in range(16):
        img = base.copy()
        for y in range(16):
            for x in range(16):
                if edge[y, x]:
                    img[y, x] = mix(img[y, x], hexc("#b8f6ff", 225), 0.7)
                band = ((x + y) - f * 2) % 32
                if band < 3:
                    img[y, x] = mix(img[y, x], hexc("#ffffff", 235), 0.55 if band != 1 else 0.85)
        frames.append(img)
    save_anim(frames, "block/resonant_crystal", 2)


def glowbell():
    stem = canvas()
    s1, s2, leaf = hexc("#1d5d63"), hexc("#2a7f86"), hexc("#3b2a6b")
    for y in range(9, 16):
        put(stem, 7, y, s1)
        put(stem, 8, y, s2)
    for x, y in ((8, 8), (9, 7), (10, 6), (11, 6), (12, 7), (7, 8), (6, 7), (5, 7), (4, 8)):
        put(stem, x, y, s2)
    for x, y in ((6, 14), (5, 13), (4, 13), (9, 13), (10, 12), (11, 12)):
        put(stem, x, y, leaf)
    bells = [
        [(12, 8), (11, 9), (12, 9), (13, 9), (11, 10), (12, 10), (13, 10), (10, 11), (11, 11), (12, 11), (13, 11), (14, 11)],
        [(4, 9), (3, 10), (4, 10), (5, 10), (2, 11), (3, 11), (4, 11), (5, 11), (6, 11)],
    ]
    drops = [(12, 12), (4, 12)]
    frames = []
    for f in range(8):
        k = 0.65 + 0.35 * (0.5 + 0.5 * math.sin(f / 8 * math.tau))
        img = stem.copy()
        for bell, (dx, dy) in zip(bells, drops):
            ys = [p[1] for p in bell]
            for x, y in bell:
                t = (y - min(ys)) / max(1, max(ys) - min(ys))
                c = mix(hexc("#6fb0ff"), hexc("#b9fdff"), t)
                img[y, x] = scale_rgb(mix(c, hexc("#ffffff"), 0.15), k)
            img[dy, dx] = scale_rgb(hexc("#ffffff"), 0.75 + 0.25 * k)
        frames.append(img)
    save_anim(frames, "block/glowbell", 4)


def fern():
    img = canvas()
    cols = [hexc("#3b1f63"), hexc("#5a2f8f"), hexc("#7b47b8"), hexc("#b07cf0")]
    fronds = [((8, 15), (3, 4)), ((8, 15), (13, 3)), ((8, 15), (8, 2)), ((8, 15), (1, 9)), ((8, 15), (15, 8))]
    for (ax, ay), (bx, by) in fronds:
        steps = 14
        for i in range(steps + 1):
            t = i / steps
            x = ax + (bx - ax) * t
            y = ay + (by - ay) * t + math.sin(t * math.pi) * -1.0
            c = cols[min(3, int(t * 4))]
            put(img, int(x), int(y), c)
            if i % 3 == 1 and 0.2 < t < 0.9:
                nx, ny = -(by - ay), (bx - ax)
                ln = math.hypot(nx, ny)
                put(img, int(x + nx / ln * 1.2), int(y + ny / ln * 1.2), cols[min(3, int(t * 4) + 1)])
    save(img, "block/void_fern")


def ash_grass():
    img = canvas()
    rng = np.random.default_rng(91)
    cols = [hexc("#7f786f"), hexc("#9c958b"), hexc("#b5ad9f"), hexc("#cfc7b8")]
    for bx in (2, 4, 5, 7, 8, 10, 12, 13):
        h = rng.integers(5, 13)
        lean = rng.choice([-1, 0, 1]) * 0.25
        for i in range(h):
            x = int(bx + lean * i)
            put(img, x, 15 - i, cols[min(3, i * 4 // h)])
    save(img, "block/ash_grass")


# ----------------------------------------------------------------- items
SHARD_PAL = ("#2c2170", "#3f6fd0", "#5fd6ee", "#d6ffff")


def items():
    shard = canvas()
    crystal_segment(shard, (4.0, 13.0), (12.5, 2.5), 2.7, SHARD_PAL)
    crystal_segment(shard, (3.0, 12.5), (2.5, 6.5), 1.4, SHARD_PAL)
    crystal_segment(shard, (6.0, 14.0), (11.0, 11.0), 1.3, SHARD_PAL)
    save(outline(shard, "#140e33"), "item/void_crystal_shard")

    # Blade: void crystal (shard). Guard: netherite with ender-pearl gems at both ends
    # and an end-crystal core. Grip: netherite. Pommel: a glowing end-rod tip.
    sword = canvas()
    crystal_segment(sword, (6.2, 9.8), (14.6, 1.4), 1.75, SHARD_PAL, taper_base=False)
    neth_d, neth, neth_hi = hexc("#241f22"), hexc("#3d3539"), hexc("#6a5f66")
    for x, y in ((3, 8), (4, 9), (6, 11), (7, 12)):
        put(sword, x, y, neth)
    for x, y in ((4, 8), (7, 11)):
        put(sword, x, y, neth_hi)
    for x, y in ((3, 9), (6, 12)):
        put(sword, x, y, neth_d)
    pearl_d, pearl, pearl_hi = hexc("#0b3b3a"), hexc("#2a9c8c"), hexc("#9ff5df")
    put(sword, 2, 7, pearl); put(sword, 1, 7, pearl_d); put(sword, 2, 6, pearl_hi)
    put(sword, 8, 13, pearl); put(sword, 9, 13, pearl_d); put(sword, 8, 14, pearl_hi)
    put(sword, 5, 10, hexc("#ff3046"))          # end-crystal core
    for x, y in ((4, 10), (5, 9), (6, 10), (5, 11)):
        put(sword, x, y, hexc("#c58fe0"))
    put(sword, 5, 9, hexc("#e9c9ff"))
    for x, y in ((3, 12), (2, 13)):
        put(sword, x, y, neth)
    put(sword, 4, 11, neth_d)
    put(sword, 3, 13, neth_d)
    put(sword, 1, 14, hexc("#fff8e8"))           # end-rod pommel
    put(sword, 0, 15, hexc("#b9b0a0"))
    put(sword, 2, 14, hexc("#e8dcc4"))
    save(outline(sword, "#140e33"), "item/voidcrystal_sword")

    frames = []
    for f in range(8):
        k = 0.5 + 0.5 * math.sin(f / 8 * math.tau)
        img = canvas()
        for y in range(16):
            for x in range(16):
                d = math.hypot(x + 0.5 - 8, y + 0.5 - 9.5)
                if 4.4 < d < 5.6:
                    shade = 0.75 + 0.35 * (-(x - 8) - (y - 9.5)) / 8
                    img[y, x] = scale_rgb(hexc("#9c96c4"), shade)
        for x, y in ((8, 2), (7, 3), (9, 3), (8, 4)):
            put(img, x, y, hexc("#6d668f"))
        gem = [(8, 7), (7, 8), (8, 8), (9, 8), (6, 9), (7, 9), (8, 9), (9, 9), (10, 9),
               (7, 10), (8, 10), (9, 10), (7, 11), (8, 11), (9, 11), (8, 12)]
        for x, y in gem:
            t = (y - 7) / 5
            c = mix(hexc("#b07cf0"), hexc("#3d3aa8"), t)
            img[y, x] = mix(c, hexc("#7ff6ff"), 0.6 * k)
        put(img, 7, 8, mix(hexc("#e8d8ff"), hexc("#ffffff"), k))
        put(img, 8, 7, hexc("#ffffff"))
        frames.append(outline(img, "#140e33"))
    save_anim(frames, "item/voidstep_charm", 3)


# ----------------------------------------------------------------- particles
def particles():
    for i, r in enumerate((1.4, 2.2, 3.0, 3.7)):
        img = canvas(8, 8)
        for y in range(8):
            for x in range(8):
                d = math.hypot(x + 0.5 - 4, y + 0.5 - 4)
                if d < r:
                    a = (1 - d / r) ** 0.8
                    img[y, x] = [255, 255, 255, 255 * min(1, a * 1.6)]
        save(img, f"particle/void_mote_{i}")
    for i, ln in enumerate((1, 2, 3, 2)):
        img = canvas(8, 8)
        img[3:5, 3:5] = [255, 255, 255, 255]
        for k in range(1, ln + 1):
            a = 255 * (1 - k / (ln + 1.5))
            for x, y in ((3 - k, 3), (4 + k, 4), (3, 3 - k), (4, 4 + k), (3 - k, 4), (4 + k, 3), (4, 3 - k), (3, 4 + k)):
                put(img, x, y, [255, 255, 255, a])
        save(img, f"particle/crystal_glint_{i}")


# ----------------------------------------------------------------- entity
def box_faces(u, v, w, h, d):
    """Rects (x, y, width, height) of each face in Minecraft's box-UV layout."""
    return {
        "top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h), "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h), "back": (u + d + w + d, v + d, w, h),
    }


def paint_box(img, u, v, w, h, d, fn):
    for face, (x0, y0, fw, fh) in box_faces(u, v, w, h, d).items():
        for y in range(fh):
            for x in range(fw):
                c = fn(face, x, y, fw, fh)
                if c is not None:
                    img[y0 + y, x0 + x] = c


def entity():
    tex, glow = canvas(64, 64), canvas(64, 64)
    n = noise(64, 64, 2, 101)
    shell = [hexc("#1a0b2e", 235), hexc("#2a1450", 235), hexc("#3b1f6e", 235)]

    def core(face, x, y, fw, fh):
        c = shell[int(n[y, x + 8 * len(face)] * 2.99) % 3]
        crack = (x + y * 3 + len(face)) % 7 == 0 and 0 < x < fw - 1 and 0 < y < fh - 1
        if face == "front" and 2 <= x <= 5 and 3 <= y <= 4:
            return hexc("#ffffff") if x in (3, 4) else hexc("#7ff6ff")
        if crack:
            return hexc("#5fd6ee")
        if x in (0, fw - 1) or y in (0, fh - 1):
            return scale_rgb(c, 1.4)
        return c

    def core_glow(face, x, y, fw, fh):
        if face == "front" and 2 <= x <= 5 and 3 <= y <= 4:
            return hexc("#ffffff") if x in (3, 4) else hexc("#6ff3ff")
        if (x + y * 3 + len(face)) % 7 == 0 and 0 < x < fw - 1 and 0 < y < fh - 1:
            return hexc("#3fb6d0")
        return None

    paint_box(tex, 0, 0, 8, 8, 8, core)
    paint_box(glow, 0, 0, 8, 8, 8, core_glow)

    def shard(face, x, y, fw, fh):
        t = y / max(1, fh - 1)
        return mix(hexc("#d6ffff", 240), hexc("#3f6fd0", 220), t) if x == 0 else mix(hexc("#5fd6ee", 230), hexc("#2c2170", 220), t)

    paint_box(tex, 32, 0, 2, 5, 2, shard)
    paint_box(glow, 32, 0, 2, 5, 2, lambda f, x, y, fw, fh: hexc("#2a8fa8") if y < 3 else None)

    def tail(alpha_top, alpha_bot):
        def fn(face, x, y, fw, fh):
            t = y / max(1, fh - 1)
            c = mix(hexc("#5a2f8f"), hexc("#2fb7b0"), t)
            c[3] = alpha_top * (1 - t) + alpha_bot * t
            if (x + y) % 3 == 0:
                c[:3] = np.clip(c[:3] * 1.3, 0, 255)
            return c
        return fn

    paint_box(tex, 0, 16, 5, 5, 5, tail(200, 120))
    paint_box(tex, 20, 16, 3, 4, 3, tail(130, 50))
    paint_box(glow, 20, 16, 3, 4, 3, lambda f, x, y, fw, fh: hexc("#1f6f7a") if (x + y) % 2 == 0 else None)

    for ox in (12, 24):
        for y in range(12):
            for x in range(12):
                d = math.hypot(x + 0.5 - 6, y + 0.5 - 6)
                ang = math.atan2(y + 0.5 - 6, x + 0.5 - 6)
                if 4.3 < d < 5.9 and int((ang + math.pi) / math.tau * 12) % 3 != 2:
                    tex[32 + y, ox + x] = hexc("#7ff6ff", 200)
                    glow[32 + y, ox + x] = hexc("#2ea6b8")
    save(tex, "entity/void_wisp")
    save(glow, "entity/void_wisp_glow")


def icon():
    size = 128
    img = canvas(size, size)
    for y in range(size):
        for x in range(size):
            d = math.hypot(x - 64, y - 64) / 64
            c = mix(hexc("#3b1f6e"), hexc("#07060e"), min(1, d))
            img[y, x] = c
    shard = np.array(Image.open(os.path.join(TEX, "item/void_crystal_shard.png")).convert("RGBA"), dtype=float)
    big = np.array(Image.fromarray(shard.astype(np.uint8)).resize((96, 96), Image.NEAREST), dtype=float)
    for y in range(96):
        for x in range(96):
            if big[y, x, 3] > 0:
                img[16 + y, 16 + x] = big[y, x]
    rng = np.random.default_rng(7)
    for _ in range(40):
        x, y = rng.integers(4, 124, 2)
        if img[y, x, 0] < 80:
            img[y:y + 2, x:x + 2] = hexc("#9ffff4") if rng.random() < 0.6 else hexc("#c9a4ff")
    path = os.path.join(ROOT, "icon.png")
    Image.fromarray(img.astype(np.uint8), "RGBA").save(path)


def workbench():
    top_base = voidstone_base(141)
    frames = []
    for f in range(8):
        k = 0.5 + 0.5 * math.sin(f / 8 * math.tau)
        img = top_base.copy()
        for i in range(16):
            for j in (0, 3, 6, 9, 12, 15):
                c = mix(hexc("#1d1938"), hexc("#3fb6c8"), 0.35 + 0.45 * k)
                img[j, i] = c
                img[i, j] = c
        for cx, cy in ((7, 7), (8, 7), (7, 8), (8, 8)):
            img[cy, cx] = mix(hexc("#c58fe0"), hexc("#ffffff"), k * 0.6)
        frames.append(img)
    save_anim(frames, "block/void_workbench_top", 4)

    planks = posterize(fbm(16, 16, 64, (8, 4, 2), stretch=(2, 0.5)), ["#4a2a6b", "#55307a", "#5f3688", "#6b3f94"])
    side = planks.copy()
    band = voidstone_base(142)
    for y in range(16):
        for x in range(16):
            if y < 5:
                side[y, x] = band[y, x]
            elif y % 4 == 0:
                side[y, x] = hexc("#2a173f")
            if y == 4:
                side[y, x] = hexc("#07060e")
            if x in (0, 15) and y >= 5:
                side[y, x] = hexc("#1d1938")
    for x in range(3, 13):
        if x % 2 == 1:
            side[2, x] = hexc("#5ff0f0")
    side[2, 7] = side[2, 8] = hexc("#e8ffff")
    # tools hanging on the front: a little crystal and a hammer silhouette
    for x, y in ((5, 8), (5, 9), (5, 10), (4, 8), (6, 8)):
        side[y, x] = hexc("#6a5f66")
    for x, y in ((10, 8), (11, 9), (10, 10), (11, 11)):
        side[y, x] = hexc("#5fd6ee")
    save(side, "block/void_workbench_side")


def gui():
    W, H = 176, 204
    img = np.zeros((256, 256, 4))
    bg, light, dark, edge = hexc("#2a2340"), hexc("#4b4170"), hexc("#120e20"), hexc("#07060e")
    img[0:H, 0:W] = bg
    img[0, 2:W - 2] = edge; img[H - 1, 2:W - 2] = edge
    img[2:H - 2, 0] = edge; img[2:H - 2, W - 1] = edge
    img[1, 1] = img[1, W - 2] = img[H - 2, 1] = img[H - 2, W - 2] = edge
    img[1, 2:W - 2] = light; img[2:H - 2, 1] = light
    img[H - 2, 2:W - 2] = dark; img[2:H - 2, W - 2] = dark
    img[0, 0] = img[0, 1] = img[1, 0] = img[0, W - 1] = img[0, W - 2] = img[1, W - 1] = 0
    img[H - 1, 0] = img[H - 1, 1] = img[H - 2, 0] = img[H - 1, W - 1] = img[H - 1, W - 2] = img[H - 2, W - 1] = 0

    def slot(x, y, size=18):
        img[y:y + size, x:x + size] = hexc("#14101f")
        img[y, x:x + size - 1] = dark
        img[y:y + size - 1, x] = dark
        img[y + size - 1, x + 1:x + size] = light
        img[y + 1:y + size, x + size - 1] = light

    # crafting grid with a faint glowing frame
    gx, gy = 16, 16
    img[gy - 2:gy + 92, gx - 2:gx + 92] = hexc("#1d1830")
    for i in range(gx - 2, gx + 92):
        img[gy - 2, i] = img[gy + 91, i] = hexc("#2fb7b0")
    for j in range(gy - 2, gy + 92):
        img[j, gx - 2] = img[j, gx + 91] = hexc("#2fb7b0")
    for r in range(5):
        for c in range(5):
            slot(gx + c * 18, gy + r * 18)
    # arrow
    ax, ay = 108, 53
    for i in range(14):
        for j in range(-1, 2):
            img[ay + 7 + j, ax + i] = hexc("#7ff6ff")
    for d in range(7):
        for j in range(-(6 - d), 6 - d + 1):
            img[ay + 7 + j, ax + 14 + d] = hexc("#7ff6ff")
    # big result slot
    rx, ry = 133, 48
    img[ry:ry + 26, rx:rx + 26] = hexc("#14101f")
    img[ry, rx:rx + 25] = dark; img[ry:ry + 25, rx] = dark
    img[ry + 25, rx + 1:rx + 26] = light; img[ry + 1:ry + 26, rx + 25] = light
    for i in range(rx - 1, rx + 27):
        img[ry - 1, i] = img[ry + 26, i] = hexc("#b07cf0")
    for j in range(ry - 1, ry + 27):
        img[j, rx - 1] = img[j, rx + 26] = hexc("#b07cf0")
    # player inventory
    for r in range(3):
        for c in range(9):
            slot(7 + c * 18, 121 + r * 18)
    for c in range(9):
        slot(7 + c * 18, 179)
    save(img, "gui/container/void_workbench")


# ----------------------------------------------------------------- enderite
EN = {"o": "#0e0717", "d": "#26123d", "m": "#3b1d5c", "l": "#6a3aa0", "h": "#d9b8ff", "g": "#d66bff", "c": "#7ff6ff",
      "w": "#3a2a4a", "W": "#5b4670", "r": "#b9b4c8", "R": "#7d7891", "P": "#c58fe0", "p": "#7a3cc0", "s": "#f0d8ff"}


def sprite(rows, pal=EN, outline_color="#0e0717"):
    img = canvas()
    for y, row in enumerate(rows):
        for x, ch in enumerate(row[:16]):
            if ch != ".":
                img[y, x] = hexc(pal[ch])
    return outline(img, outline_color) if outline_color else img


def draw(img, pts, ch):
    for x, y in pts:
        put(img, x, y, hexc(EN[ch]))


def diag(x0, y0, n):
    return [(x0 + i, y0 - i) for i in range(n)]


def enderite_items():
    # tools: handle from bottom-left, head at top-right
    def handle(img, x0, y0, n):
        for i, (x, y) in enumerate(diag(x0, y0, n)):
            put(img, x, y, hexc(EN["W"] if i % 2 else EN["w"]))

    sword = canvas()
    draw(sword, diag(5, 9, 9), "l"); draw(sword, diag(5, 10, 9), "h"); draw(sword, diag(6, 10, 9), "m")
    put(sword, 14, 1, hexc(EN["h"]))
    for i, (x, y) in enumerate(diag(5, 10, 9)):
        if i % 3 == 2: put(sword, x, y, hexc(EN["g"]))
    draw(sword, [(3, 10), (4, 11), (5, 12)], "d"); draw(sword, [(2, 9), (6, 13)], "c")
    draw(sword, [(3, 12), (2, 13)], "w"); put(sword, 1, 14, hexc(EN["g"]))
    save(outline(sword, EN["o"]), "item/enderite_sword")

    pick = canvas()
    handle(pick, 2, 13, 8)
    draw(pick, [(4, 3), (5, 2), (6, 2), (7, 2), (8, 2), (9, 3), (10, 3), (11, 4), (12, 5), (12, 6), (13, 7), (13, 8), (13, 9), (13, 10)], "l")
    draw(pick, [(5, 3), (6, 3), (7, 3), (8, 3), (9, 4), (10, 4), (11, 5), (11, 6), (12, 7), (12, 8), (12, 9)], "m")
    draw(pick, [(3, 4), (13, 11)], "h"); draw(pick, [(10, 5), (9, 5)], "d"); put(pick, 10, 4, hexc(EN["g"]))
    save(outline(pick, EN["o"]), "item/enderite_pickaxe")

    axe = canvas()
    handle(axe, 3, 13, 10)
    rows = {1: (6, 12), 2: (5, 12), 3: (4, 11), 4: (4, 10), 5: (5, 9), 6: (6, 8)}
    for y, (a, b) in rows.items():
        for x in range(a, b + 1):
            put(axe, x, y, hexc(EN["h"] if x == a else EN["l"] if x < a + 2 else EN["m"] if x < b - 1 else EN["d"]))
    put(axe, 7, 3, hexc(EN["g"])); put(axe, 13, 3, hexc(EN["W"])); put(axe, 13, 2, hexc(EN["w"]))
    save(outline(axe, EN["o"]), "item/enderite_axe")

    shovel = canvas()
    handle(shovel, 3, 13, 7)
    rows = {1: (11, 13), 2: (10, 14), 3: (9, 14), 4: (9, 13), 5: (10, 12)}
    for y, (a, b) in rows.items():
        for x in range(a, b + 1):
            put(shovel, x, y, hexc(EN["h"] if y == 1 or x == a else EN["l"] if y < 4 else EN["m"]))
    put(shovel, 12, 3, hexc(EN["g"])); draw(shovel, [(9, 6)], "d")
    save(outline(shovel, EN["o"]), "item/enderite_shovel")

    hoe = canvas()
    handle(hoe, 3, 13, 8)
    draw(hoe, [(6, 4), (7, 4), (8, 4), (9, 4), (10, 4), (11, 4)], "l")
    draw(hoe, [(7, 3), (8, 3), (9, 3), (10, 3), (11, 3)], "h")
    draw(hoe, [(6, 5), (6, 6), (5, 5)], "m"); put(hoe, 10, 4, hexc(EN["g"]))
    save(outline(hoe, EN["o"]), "item/enderite_hoe")

    save(sprite(["................", "................", "................", "................",
                 "....mmmmmmmm....", "...mllhhhhllm...", "..mlhhllllhhlm..", "..mlgmmmmmmglm..",
                 "..ml........lm..", "..ml........lm..", "..md........dm..", "..mm........mm..",
                 "................", "................", "................", "................"]), "item/enderite_helmet")
    save(sprite(["................", "..mmm......mmm..", ".mlllm....mlllm.", ".mlhlmmmmmmlhlm.",
                 ".mlhllllllllhlm.", "..mllllggllllm..", "...mllllllllm...", "...mlllcclllm...",
                 "...mllllllllm...", "...mllllllllm...", "...mmlllllllm...", "....mmmmmmmm....",
                 "................", "................", "................", "................"]), "item/enderite_chestplate")
    save(sprite(["................", "................", "..mmmmmmmmmmmm..", "..mllllllllllm..",
                 "..mlhggggggglm..", "..mllllmmllllm..", "..mlllm..mlllm..", "..mlhlm..mlhlm..",
                 "..mlllm..mlllm..", "..mlllm..mlllm..", "..mlclm..mlclm..", "..mlllm..mlllm..",
                 "..mmmmm..mmmmm..", "................", "................", "................"]), "item/enderite_leggings")
    save(sprite(["................", "................", "................", "................",
                 "................", "...mmm....mmm...", "...mlm....mlm...", "...mlm....mlm...",
                 "...mgm....mgm...", "..mllm...mllm...", ".mhhlm..mhhlm...", ".mmmmm..mmmmm...",
                 "................", "................", "................", "................"]), "item/enderite_boots")

    save(sprite(["................", "................", ".....mm.........", "....mlhm..mm....",
                 "...mllllmmlhm...", "..mlllgllllllm..", "..mllllllglllm..", "...mdllllllldm..",
                 "...mdddlllddm...", "....mmddddmm....", ".......mm.......", "................",
                 "................", "................", "................", "................"]), "item/enderite_scrap")

    ingot_rows = ["................", "................", "................", "................",
                  "......mmmmmmm...", "....mlhhhhhhhm..", "...mlllllllllm..", "..mmmmmmmmmmlm..",
                  "..mddddddddmlm..", "..mddddddddmm...", "..mmmmmmmmmm....", "................",
                  "................", "................", "................", "................"]
    frames = []
    base = sprite(ingot_rows)
    for f in range(12):
        img = base.copy()
        for y in range(16):
            for x in range(16):
                if img[y, x, 3] > 0 and not np.array_equal(img[y, x], hexc(EN["o"])):
                    band = (x - y + 16 - f * 3) % 36
                    if band < 3:
                        img[y, x] = mix(img[y, x], hexc(EN["c"] if band == 1 else EN["g"]), 0.7)
        frames.append(img)
    save_anim(frames, "item/enderite_ingot", 2)

    tpl = canvas()
    rng = np.random.default_rng(301)
    for y in range(1, 15):
        for x in range(2, 14):
            if (x in (2, 13)) and (y in (1, 14)):
                continue
            edge = x in (2, 13) or y in (1, 14)
            tpl[y, x] = hexc(EN["d"]) if edge else (hexc(EN["m"]) if rng.random() < 0.8 else hexc(EN["d"]))
    for x, y in ((7, 3), (8, 3), (6, 4), (9, 4), (5, 6), (10, 6), (7, 5), (8, 5), (7, 7), (8, 7), (6, 9), (9, 9), (7, 10), (8, 10), (7, 12), (8, 12)):
        put(tpl, x, y, hexc(EN["g"]))
    for x, y in ((7, 6), (8, 6), (7, 11), (8, 11)):
        put(tpl, x, y, hexc(EN["c"]))
    save(outline(tpl, EN["o"]), "item/enderite_upgrade_smithing_template")


    eff = np.zeros((18, 18, 4))
    for i, y0 in enumerate((4, 9)):
        for k in range(6):
            for x, y in ((8 - k, y0 + k), (9 + k, y0 + k)):
                put(eff, x, y, hexc("#d66bff") if i == 0 else hexc("#b07cf0"))
                put(eff, x, y + 1, hexc("#6a3aa0"))
    put(eff, 8, 2, hexc("#f0d8ff")); put(eff, 9, 2, hexc("#f0d8ff"))
    save(eff, "mob_effect/weightless")


def enderite_ore():
    stone = posterize(fbm(16, 16, 13, (4, 2, 1)), ["#b8b27a", "#cbc68c", "#dad79c", "#e7e5b4"])
    crystals = [(3, 4), (4, 4), (4, 3), (5, 4), (4, 5), (5, 5), (3, 5), (5, 3),
                (10, 8), (11, 8), (11, 9), (10, 9), (12, 9), (11, 10), (12, 10), (10, 10), (11, 7),
                (5, 11), (6, 11), (6, 12), (5, 12), (7, 12), (6, 13), (4, 12), (13, 3), (12, 4), (2, 9)]
    glints = [(4, 4), (11, 9), (6, 12), (13, 3)]
    frames = []
    for f in range(8):
        k = 0.5 + 0.5 * math.sin(f / 8 * math.tau)
        img = stone.copy()
        for x, y in crystals:
            img[y, x] = hexc(EN["m"])
            for dx, dy in ((1, 0), (0, 1)):
                if (x + dx, y + dy) not in crystals:
                    put(img, x + dx, y + dy, hexc(EN["d"]))
        for x, y in crystals[::2]:
            img[y, x] = hexc(EN["l"])
        for x, y in glints:
            img[y, x] = mix(hexc(EN["g"]), hexc(EN["s"]), k)
        frames.append(img)
    save_anim(frames, "block/enderite_ore", 4)


def ooze():
    """Stellar Aether: deep indigo liquid with drifting nebula swirls and twinkling stars."""
    n = 32
    pal = ["#191052", "#241a70", "#30268f", "#4033ad", "#5d4bd0"]
    for name, size in (("void_ooze_still", 16), ("void_ooze_flow", 32)):
        frames = []
        a = fbm(size, size, 401, (8, 4, 2))
        b = fbm(size, size, 402, (4, 2, 1))
        rng = np.random.default_rng(403 + size)
        stars = [(int(x), int(y), rng.random()) for x, y in rng.integers(0, size, (size // 2, 2))]
        for f in range(n):
            img = canvas(size, size)
            shift = f * size // n
            for y in range(size):
                for x in range(size):
                    if name.endswith("flow"):
                        v = 0.6 * a[(y - shift) % size, x] + 0.4 * b[(y - 2 * shift) % size, x]
                    else:
                        v = 0.6 * a[y, (x + shift) % size] + 0.4 * b[(y + shift) % size, (x - shift) % size]
                    c = hexc(pal[min(4, int(v * 5))], 225)
                    if v > 0.8:   # magenta nebula wisps
                        c = mix(c, hexc("#c06cf0", 230), (v - 0.8) * 4)
                    img[y, x] = c
            for sx, sy, ph in stars:
                yy = (sy - shift) % size if name.endswith("flow") else sy
                xx = sx if name.endswith("flow") else (sx + shift // 2) % size
                k = 0.5 + 0.5 * math.sin((f / n + ph) * math.tau * 2)
                if k > 0.35:
                    img[yy, xx] = mix(hexc("#7ff6ff", 235), hexc("#ffffff", 245), k)
            frames.append(img)
        save_anim(frames, "block/" + name, 2)

    bucket_pal = dict(EN, P="#5b4cd0", p="#221a6a", s="#ffffff")
    save(sprite(["................", "................", ".....RRRRRR.....", "....R......R....",
                 "...R........R...", "..rrrrrrrrrrrr..", "..rPPPsPPPPPPr..", "..rpppppppcppr..",
                 "...rpspppppppr..", "...rppppppcpr...", "...rppppppppr...", "....rrrrrrrr....",
                 "................", "................", "................", "................"], bucket_pal),
         "item/void_ooze_bucket")


def enderite_armor_layers():
    def plates(face, x, y, fw, fh, seed):
        rng = np.random.default_rng(seed + x * 31 + y * 7 + len(face))
        c = hexc(EN["m"]) if rng.random() < 0.7 else hexc(EN["d"])
        if x in (0, fw - 1) or y in (0, fh - 1):
            c = hexc(EN["l"])
        if face == "front" and fw >= 8 and y == fh // 2:
            c = hexc(EN["g"])
        if (x + y) % 5 == 0 and rng.random() < 0.3:
            c = hexc(EN["h"])
        return c

    l1 = np.zeros((32, 64, 4))
    paint_box(l1, 0, 0, 8, 8, 8, lambda f, x, y, fw, fh: None if (f == "bottom" or (f == "front" and 2 <= y <= 5 and 1 <= x <= 6)) else plates(f, x, y, fw, fh, 1))
    paint_box(l1, 16, 16, 8, 12, 4, lambda f, x, y, fw, fh: plates(f, x, y, fw, fh, 2))
    paint_box(l1, 40, 16, 4, 12, 4, lambda f, x, y, fw, fh: plates(f, x, y, fw, fh, 3) if (f in ("top",) or y < 6) else None)
    paint_box(l1, 0, 16, 4, 12, 4, lambda f, x, y, fw, fh: plates(f, x, y, fw, fh, 4) if (f == "bottom" or y >= 8) else None)
    # visor glow
    for x in range(10, 14):
        l1[11, x] = hexc(EN["c"])
    save(l1, "models/armor/enderite_layer_1")

    l2 = np.zeros((32, 64, 4))
    paint_box(l2, 0, 16, 4, 12, 4, lambda f, x, y, fw, fh: plates(f, x, y, fw, fh, 5) if (f != "bottom" and y < 10) else None)
    paint_box(l2, 16, 16, 8, 12, 4, lambda f, x, y, fw, fh: plates(f, x, y, fw, fh, 6) if (f != "top" and y >= 7) else None)
    save(l2, "models/armor/enderite_layer_2")



def aether_wings():
    """Elytra-model texture (64x32) for the Aether Wings plus a glow layer and the item icon."""
    tex, glow = np.zeros((32, 64, 4)), np.zeros((32, 64, 4))
    pal = ["#1a1052", "#241a70", "#30268f", "#4033ad"]
    n = fbm(10, 20, 501, (4, 2, 1))
    rng = np.random.default_rng(502)
    lengths = [20, 19, 18, 16, 14]          # feather lengths, longest at the body side

    def wing(face, x, y, fw, fh):
        if face in ("front", "back"):
            fx = x if face == "front" else fw - 1 - x
            if y >= lengths[min(4, fx // 2)]:
                return None
            c = hexc(pal[min(3, int(n[y, fx] * 4))])
            if fx % 2 == 1 and y > 3:
                c = mix(c, hexc("#5fd6ee"), 0.55)              # feather vein
            if y < 2:
                c = hexc("#6a52d8")                            # leading edge
            if y == lengths[min(4, fx // 2)] - 1:
                c = hexc("#b07cf0")                            # feather tips
            return c
        return hexc("#241a70")

    def wing_glow(face, x, y, fw, fh):
        if face not in ("front", "back"):
            return None
        fx = x if face == "front" else fw - 1 - x
        if y >= lengths[min(4, fx // 2)]:
            return None
        if fx % 2 == 1 and y > 3:
            return hexc("#1f7f8f")
        if y == lengths[min(4, fx // 2)] - 1:
            return hexc("#5a2d86")
        return None

    paint_box(tex, 22, 0, 10, 20, 2, wing)
    paint_box(glow, 22, 0, 10, 20, 2, wing_glow)
    for _ in range(14):                                       # stars
        x, y = int(rng.integers(0, 10)), int(rng.integers(2, 16))
        for ox in (24, 36):
            if tex[2 + y, ox + x, 3] > 0:
                tex[2 + y, ox + x] = hexc("#e8ffff")
                glow[2 + y, ox + x] = hexc("#bfefff")
    save(tex, "entity/aether_wings")
    save(glow, "entity/aether_wings_glow")

    icon_pal = dict(EN, m="#30268f", l="#4a3cb8", h="#b07cf0", c="#5fd6ee", d="#1a1052", s="#e8ffff")
    save(sprite(["................", "................", ".hh..........hh.", ".hlh........hlh.",
                 ".mllh......hllm.", ".mclh......hlcm.", "..mcl.h..h.lcm..", "..mmcllhhllcmm..",
                 "...mmclssllcmm..", "...dmmcllcmmd...", "....dmmccmmd....", ".....dmmmmd.....",
                 "......d..d......", "................", "................", "................"], icon_pal),
         "item/aether_wings")



VANILLA_JAR = os.environ.get("MC_CLIENT_JAR", "")


def vanilla_alpha(name):
    """Only the *shape* (alpha mask) of a vanilla item is read; no vanilla pixels are copied."""
    import zipfile, io
    with zipfile.ZipFile(VANILLA_JAR) as z:
        im = Image.open(io.BytesIO(z.read(f"assets/minecraft/textures/item/{name}.png"))).convert("RGBA")
    return np.array(im)[:, :, 3] > 0


def vanilla_style_overlays():
    """Overlays drawn on top of vanilla item textures referenced by the models (ingot tinted in code)."""
    if not VANILLA_JAR:
        print("MC_CLIENT_JAR not set, skipping vanilla-style overlays")
        return
    ingot = vanilla_alpha("iron_ingot")
    frames = []
    for f in range(16):
        img = canvas()
        for y in range(16):
            for x in range(16):
                if not ingot[y, x]:
                    continue
                band = (x - y + 22 - f * 3) % 48          # a glint sweeping across every ~2.4 s
                if band < 4:
                    a = (0.95, 0.7, 0.45, 0.2)[band]
                    img[y, x] = hexc("#f4e2ff", int(255 * a))
        twinkle = 0.5 + 0.5 * math.sin(f / 16 * math.tau)
        for (x, y), col in (((10, 6), "#7ff6ff"), ((5, 9), "#e070ff")):
            if ingot[y, x]:
                img[y, x] = hexc(col, int(120 + 135 * twinkle))
        frames.append(img)
    save_anim(frames, "item/enderite_ingot_shine", 2)

    # Fluid surface exactly where vanilla buckets show their water/lava: rows 2..5 inside the rim.
    bucket = vanilla_alpha("bucket")
    fluid = np.zeros((16, 16), dtype=bool)
    rows = {2: (5, 10), 3: (3, 12), 4: (3, 12), 5: (5, 10)}
    for y, (a, b) in rows.items():
        fluid[y, a:b + 1] = True
    n = fbm(16, 16, 601, (4, 2, 1))
    pal = ["#4a1c8a", "#6328b0", "#7d3cd0", "#9a5ee8"]
    rng = np.random.default_rng(602)
    stars = [(int(x), int(y), rng.random()) for x, y in zip(rng.integers(3, 13, 6), rng.integers(2, 6, 6))]
    frames = []
    for f in range(16):
        img = canvas()
        for y in range(16):
            for x in range(16):
                if fluid[y, x]:
                    v = n[y, (x + f) % 16]
                    img[y, x] = hexc(pal[min(3, int(v * 4))])
        for x, y, ph in stars:
            if fluid[y, x]:
                k = 0.5 + 0.5 * math.sin((f / 16 + ph) * math.tau)
                if k > 0.45:
                    img[y, x] = mix(hexc("#b07cf0"), hexc("#ffffff"), k)
        # one glowing drip running down the side, like the lava bucket
        dy = 6 + (f // 4) % 3
        if bucket[dy, 3]:
            img[dy, 3] = hexc("#7a5ee0")
        frames.append(img)
    save_anim(frames, "item/void_ooze_bucket_fluid", 3)


if __name__ == "__main__":
    vanilla_style_overlays()
    aether_wings()
    enderite_items()
    enderite_ore()
    ooze()
    enderite_armor_layers()
    workbench()
    gui()
    void_moss()
    voidstone()
    chiseled_voidstone()
    ashen_sand()
    aetherwood()
    aetherwood_leaves()
    resonant_crystal()
    glowbell()
    fern()
    ash_grass()
    items()
    particles()
    entity()
    icon()
    print("textures written to", TEX)
