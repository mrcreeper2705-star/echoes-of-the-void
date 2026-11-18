"""Synthesises an original ambient track for the trailer.  Usage: python3 music.py out.wav seconds"""
import sys
import wave

import numpy as np

SR = 44100
OUT, DUR = sys.argv[1], float(sys.argv[2])
N = int(SR * DUR)
t = np.arange(N) / SR
rng = np.random.default_rng(7)


def note(m):
    return 440.0 * 2 ** ((m - 69) / 12)


def lowpass(x, cutoff):
    # one-pole low-pass, run forward and back for a gentle 12 dB slope
    a = np.exp(-2 * np.pi * cutoff / SR)
    y = np.empty_like(x)
    for _ in range(2):
        acc = 0.0
        for i in range(len(x)):
            acc = (1 - a) * x[i] + a * acc
            y[i] = acc
        x = y[::-1].copy()
    return x


def env(n, attack, release):
    e = np.ones(n)
    a, r = int(attack * SR), int(release * SR)
    e[:a] = np.linspace(0, 1, a) ** 2
    e[-r:] *= np.linspace(1, 0, r) ** 2
    return e


# A-minor-ish progression, 8 s per chord: Am(add9) - Fmaj7 - Cmaj7 - G6 - Em7 - Fmaj7 ...
CHORDS = [[57, 60, 64, 71], [53, 57, 60, 64], [48, 55, 59, 64], [55, 59, 62, 64], [52, 55, 59, 62], [53, 57, 60, 67]]
BAR = 8.0
mix = np.zeros(N)

# Pads: detuned saw-ish stacks, low-passed, with slow swells.
pad = np.zeros(N)
for ci in range(int(np.ceil(DUR / BAR))):
    chord = CHORDS[ci % len(CHORDS)]
    s0 = int(ci * BAR * SR)
    n = min(int((BAR + 2.5) * SR), N - s0)
    if n <= 0:
        break
    tt = np.arange(n) / SR
    seg = np.zeros(n)
    for m in chord:
        for det in (-0.07, 0.0, 0.08):
            f = note(m) * 2 ** (det / 12)
            ph = rng.random() * 2 * np.pi
            seg += sum(np.sin(2 * np.pi * f * k * tt + ph) / k for k in (1, 2, 3, 4)) * 0.6
    pad[s0:s0 + n] += seg * env(n, 2.5, 3.0)
pad = lowpass(pad[::2].copy(), 900 / 2).repeat(2)[:N]  # filter at half rate to keep it quick
mix += pad / np.abs(pad).max() * 0.33

# Sub bass on chord roots.
for ci in range(int(np.ceil(DUR / BAR))):
    root = CHORDS[ci % len(CHORDS)][0] - 12
    s0 = int(ci * BAR * SR)
    n = min(int(BAR * SR), N - s0)
    if n <= 0:
        break
    tt = np.arange(n) / SR
    mix[s0:s0 + n] += np.sin(2 * np.pi * note(root) * tt) * env(n, 1.0, 1.5) * 0.16

# Glassy bell arpeggio (end-crystal chimes), starting after the intro.
bells = np.zeros(N)
step = 0.5
for i in range(int(8.0 / step), int((DUR - 6) / step)):
    if rng.random() < 0.38:
        continue
    chord = CHORDS[int(i * step / BAR) % len(CHORDS)]
    m = chord[rng.integers(0, len(chord))] + 12 * rng.choice([1, 2])
    s0 = int(i * step * SR)
    n = min(int(3.0 * SR), N - s0)
    tt = np.arange(n) / SR
    f = note(m)
    tone = (np.sin(2 * np.pi * f * tt) + 0.35 * np.sin(2 * np.pi * f * 2.76 * tt) * np.exp(-tt * 6)) * np.exp(-tt * 2.2)
    bells[s0:s0 + n] += tone * (0.5 + 0.5 * rng.random())
mix += bells / max(1e-9, np.abs(bells).max()) * 0.2

# Airy noise wind.
noise = lowpass(rng.standard_normal(N // 4), 300 / 4).repeat(4)[:N]
mix += noise / np.abs(noise).max() * 0.05 * (0.6 + 0.4 * np.sin(2 * np.pi * t / 11))

# Simple stereo reverb: a few feedback combs per channel.
def reverb(x, delays, fb=0.72):
    y = np.zeros(len(x))
    for d in delays:
        dd = int(d * SR)
        buf = x.copy()
        for k in range(1, 6):
            if dd * k >= len(x):
                break
            buf[dd * k:] += x[:-dd * k] * fb ** k
        y += buf
    return y / len(delays)


L = mix * 0.6 + reverb(mix, [0.0297, 0.0371, 0.0411, 0.0437]) * 0.5
R = mix * 0.6 + reverb(mix, [0.0311, 0.0353, 0.0429, 0.0451]) * 0.5
fade = np.ones(N)
fade[: int(2.5 * SR)] = np.linspace(0, 1, int(2.5 * SR))
fade[-int(4 * SR):] = np.linspace(1, 0, int(4 * SR))
st = np.stack([L * fade, R * fade], axis=1)
st = st / np.abs(st).max() * 0.85
with wave.open(OUT, "wb") as w:
    w.setnchannels(2)
    w.setsampwidth(2)
    w.setframerate(SR)
    w.writeframes((st * 32767).astype("<i2").tobytes())
print("music", OUT, DUR)
