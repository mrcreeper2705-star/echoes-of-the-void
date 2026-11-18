"""Scripted camera for recording the trailer.

Moves the spectator player along smooth Catmull-Rom paths by sending /tp over
RCON ~30 times a second while ffmpeg records the X display.
"""
import math
import socket
import struct
import subprocess
import time

DIM = "minecraft:the_end"


class Rcon:
    def __init__(self, host="127.0.0.1", port=25575, password="test"):
        self.s = socket.create_connection((host, port), timeout=30)
        self._send(3, password)
        self._recv()

    def _send(self, kind, body):
        data = struct.pack("<ii", 7, kind) + body.encode() + b"\0\0"
        self.s.sendall(struct.pack("<i", len(data)) + data)

    def _recv(self):
        n = struct.unpack("<i", self._read(4))[0]
        return self._read(n)[8:-2].decode(errors="replace")

    def _read(self, n):
        buf = b""
        while len(buf) < n:
            chunk = self.s.recv(n - len(buf))
            if not chunk:
                raise ConnectionError("rcon closed")
            buf += chunk
        return buf

    def cmd(self, c):
        self._send(2, c)
        return self._recv()


def catmull(p0, p1, p2, p3, t):
    t2, t3 = t * t, t * t * t
    return 0.5 * ((2 * p1) + (-p0 + p2) * t + (2 * p0 - 5 * p1 + 4 * p2 - p3) * t2 + (-p0 + 3 * p1 - 3 * p2 + p3) * t3)


def ease(t):
    return t * t * (3 - 2 * t)


def sample(keys, u):
    """keys: list of (x, y, z, yaw, pitch); u in [0,1] across the whole path."""
    n = len(keys) - 1
    f = min(u * n, n - 1e-9)
    i = int(f)
    t = f - i
    pts = [keys[max(0, i - 1)], keys[i], keys[i + 1], keys[min(n, i + 2)]]
    out = []
    for c in range(5):
        vals = [p[c] for p in pts]
        if c == 3:  # unwrap yaw so it turns the short way
            for k in range(1, 4):
                while vals[k] - vals[k - 1] > 180: vals[k] -= 360
                while vals[k] - vals[k - 1] < -180: vals[k] += 360
        out.append(catmull(*vals, t))
    return out


class Camera:
    def __init__(self, rcon, player="Tester"):
        self.r = rcon
        self.player = player

    def tp(self, x, y, z, yaw, pitch):
        self.r.cmd(f"execute in {DIM} run tp {self.player} {x:.3f} {y:.3f} {z:.3f} {yaw:.2f} {pitch:.2f}")

    def fly(self, keys, seconds, eased=True, hz=30):
        start = time.time()
        while True:
            el = time.time() - start
            u = min(1.0, el / seconds)
            self.tp(*sample(keys, ease(u) if eased else u))
            if u >= 1.0:
                break
            time.sleep(max(0.0, 1.0 / hz - (time.time() - start - el)))

    def orbit(self, cx, cy, cz, radius, height, a0, a1, seconds, pitch=None, look_y=None, hz=30):
        """Circle around a point while always looking at it. Angles in degrees."""
        start = time.time()
        look_y = cy if look_y is None else look_y
        while True:
            u = min(1.0, (time.time() - start) / seconds)
            a = math.radians(a0 + (a1 - a0) * u)
            x, z = cx + math.cos(a) * radius, cz + math.sin(a) * radius
            y = cy + height
            dx, dz, dy = cx - x, cz - z, look_y - y
            yaw = math.degrees(math.atan2(-dx, dz))
            p = pitch if pitch is not None else -math.degrees(math.atan2(dy, math.hypot(dx, dz)))
            self.tp(x, y - 1.62, z, yaw, p)  # y is the eye; tp moves the feet
            if u >= 1.0:
                break
            time.sleep(1.0 / hz)


class Recorder:
    def __init__(self, path, size="1280x720", fps=24, display=":99.0"):
        self.p = subprocess.Popen(
            ["ffmpeg", "-loglevel", "error", "-y", "-f", "x11grab", "-framerate", str(fps), "-video_size", size,
             "-i", display, "-c:v", "libx264", "-preset", "veryfast", "-crf", "16", "-pix_fmt", "yuv420p", path],
            stdin=subprocess.PIPE)
        time.sleep(0.4)

    def stop(self):
        self.p.stdin.write(b"q")
        self.p.stdin.flush()
        self.p.wait(timeout=30)
