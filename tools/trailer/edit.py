"""Cuts the recorded shots into the final trailer.  Usage: python3 edit.py SHOTS_DIR music.wav out.mp4 version"""
import os
import subprocess
import sys

from PIL import Image, ImageDraw, ImageFilter, ImageFont

SHOTS, MUSIC, OUT, VERSION = sys.argv[1:5]
WORK = os.path.join(SHOTS, "edit")
os.makedirs(WORK, exist_ok=True)
W, H, FPS, XF = 1280, 720, 24, 0.6
BOLD = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
REG = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
CYAN, VIOLET, WHITE = (111, 243, 255), (176, 124, 240), (240, 236, 250)


def glow_text(img, xy, text, font, fill, glow, radius=8, anchor="la"):
    layer = Image.new("RGBA", img.size, (0, 0, 0, 0))
    ImageDraw.Draw(layer).text(xy, text, font=font, fill=glow + (200,), anchor=anchor)
    img.alpha_composite(layer.filter(ImageFilter.GaussianBlur(radius)))
    ImageDraw.Draw(img).text(xy, text, font=font, fill=fill + (255,), anchor=anchor)


def caption(path, eyebrow, title, top=False):
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    shade = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(shade)
    for i in range(200):
        y = i if top else H - i
        d.line([(0, y), (W, y)], fill=(8, 5, 18, int(150 * (1 - i / 200) ** 1.5)))
    img.alpha_composite(shade)
    d = ImageDraw.Draw(img)
    base = 120 if top else H - 62     # bottom of the text block
    d.rectangle([60, base - 56, 66, base], fill=CYAN + (255,))
    d.text((84, base - 58), eyebrow.upper(), font=ImageFont.truetype(REG, 18), fill=CYAN + (255,))
    glow_text(img, (82, base - 34), title, ImageFont.truetype(BOLD, 40), WHITE, VIOLET, 10)
    img.save(path)


def title_card(path, top, big, sub):
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    shade = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(shade)
    for i in range(260):
        a = int(140 * (1 - abs(i - 130) / 130))
        d.line([(0, H // 2 - 130 + i), (W, H // 2 - 130 + i)], fill=(8, 5, 18, a))
    img.alpha_composite(shade)
    d = ImageDraw.Draw(img)
    d.text((W // 2, H // 2 - 78), top.upper(), font=ImageFont.truetype(REG, 20), fill=CYAN + (255,), anchor="mm")
    glow_text(img, (W // 2, H // 2), big, ImageFont.truetype(BOLD, 84), WHITE, VIOLET, 16, anchor="mm")
    d.text((W // 2, H // 2 + 76), sub, font=ImageFont.truetype(REG, 22), fill=(200, 192, 225, 255), anchor="mm")
    img.save(path)


# name, start, end, speed, crop (x, y, w, h) or None, overlay(kind, args)
EDL = [
    ("01_grove_open", 0.3, 8.1, 1.0, None, ("title", ("Мод для Minecraft 1.21.1 · Fabric", "Echoes of the Void", "Край, в который хочется возвращаться"))),
    ("02_grove_low", 0.3, 6.0, 1.0, None, ("cap", ("Биом", "Роща Пустоты"))),
    ("03_spire_island", 0.3, 5.0, 1.0, None, None),
    ("04_crystal_field", 0.3, 7.0, 1.0, None, ("cap", ("Биом", "Кристальные шпили"))),
    ("05_ashen", 0.3, 6.0, 1.0, None, ("cap", ("Биом", "Пепельные пределы"))),
    ("06_shrine", 0.3, 8.1, 1.0, None, ("cap", ("Данж", "Святилища Пустоты"))),
    ("07_wisp", 0.4, 9.0, 1.0, None, ("cap", ("Моб", "Висп Пустоты"))),
    ("08_craft", 0.8, 18.8, 1.6, (250, 140, 780, 439), ("cap", ("Новый блок · сетка 5×5", "Верстак Пустоты"))),
    ("09_blink", 0.8, 9.2, 1.0, None, ("cap_top", ("ПКМ — телепорт", "Клинок из кристалла Пустоты"))),
    ("11_falls", 0.3, 7.1, 1.0, None, ("cap", ("Жидкость · водопады в бездну", "Звёздный эфир"))),
    ("12_enderite", 0.3, 8.1, 1.0, None, ("cap", ("Легендарный материал", "Эндерит"))),
    ("13_storm", 0.2, None, 1.0, None, ("cap", ("Погода Края", "Буря Пустоты"))),
    ("14_wings", 0.5, 9.7, 1.0, None, ("cap", ("Прыжок в полёте — рывок вверх и вперёд", "Эфирные крылья"))),
    ("10_end", 0.3, 7.6, 1.0, None, ("title", (f"Версия {VERSION}", "Echoes of the Void", "Fabric · Minecraft 1.21.1"))),
]

segments, durations = [], []
for i, (name, a, b, speed, crop, ov) in enumerate(EDL):
    src = os.path.join(SHOTS, name + ".mp4")
    if b is None:
        b = float(subprocess.run(["ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "csv=p=0", src],
                                 capture_output=True, text=True).stdout) - 0.1
    dur = (b - a) / speed
    out = os.path.join(WORK, f"seg{i:02d}.mp4")
    vf = [f"trim={a}:{b}", "setpts=PTS-STARTPTS"]
    if speed != 1.0:
        vf.append(f"setpts=PTS/{speed}")
    if crop:
        x, y, w, h = crop
        vf.append(f"crop={w}:{h}:{x}:{y}")
    vf += [f"scale={W}:{H}:flags=lanczos", f"fps={FPS}", "format=yuv420p"]
    cmd = ["ffmpeg", "-loglevel", "error", "-y", "-i", src]
    if ov:
        png = os.path.join(WORK, f"ov{i:02d}.png")
        if ov[0] == "cap":
            caption(png, *ov[1])
        elif ov[0] == "cap_top":
            caption(png, *ov[1], top=True)
        else:
            title_card(png, *ov[1])
        fin, fout = 0.5, max(1.0, dur - 1.4)
        cmd += ["-loop", "1", "-t", f"{dur:.3f}", "-i", png, "-filter_complex",
                f"[0:v]{','.join(vf)}[v];[1:v]format=rgba,fade=in:st={fin}:d=0.6:alpha=1,"
                f"fade=out:st={fout:.3f}:d=0.7:alpha=1[o];[v][o]overlay=0:0:shortest=1,format=yuv420p[out]",
                "-map", "[out]"]
    else:
        cmd += ["-vf", ",".join(vf)]
    cmd += ["-an", "-c:v", "libx264", "-preset", "medium", "-crf", "17", out]
    subprocess.run(cmd, check=True)
    segments.append(out)
    durations.append(dur)
    print(f"seg {i} {name} {dur:.2f}s", flush=True)

# Cross-fade chain.
inputs, chain, offset, last = [], [], 0.0, "0:v"
for i, s in enumerate(segments):
    inputs += ["-i", s]
for i in range(1, len(segments)):
    offset += durations[i - 1] - XF
    outl = f"x{i}"
    chain.append(f"[{last}][{i}:v]xfade=transition=fade:duration={XF}:offset={offset:.3f}[{outl}]")
    last = outl
total = sum(durations) - XF * (len(segments) - 1)
chain.append(f"[{last}]fade=in:st=0:d=1.0,fade=out:st={total - 1.5:.3f}:d=1.5,format=yuv420p[vout]")
chain.append(f"[{len(segments)}:a]atrim=0:{total:.3f},afade=out:st={total - 3:.3f}:d=3[aout]")
subprocess.run(["ffmpeg", "-loglevel", "error", "-y", *inputs, "-i", MUSIC, "-filter_complex", ";".join(chain),
                "-map", "[vout]", "-map", "[aout]", "-c:v", "libx264", "-preset", "slow", "-crf", "19",
                "-c:a", "aac", "-b:a", "160k", "-movflags", "+faststart", OUT], check=True)
print(f"trailer {OUT} {total:.1f}s")
