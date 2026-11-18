"""Records every trailer shot into OUT/<name>.mp4.  Usage: python3 shoot.py OUT [scene ...]"""
import os
import subprocess
import sys
import time

from cam import Camera, Rcon, Recorder

OUT = sys.argv[1]
ONLY = set(sys.argv[2:])
os.makedirs(OUT, exist_ok=True)
r = Rcon()
cam = Camera(r)
D = "execute in minecraft:the_end run "


def key(*keys):
    subprocess.run(["xdotool", "key", *keys], env={**os.environ, "DISPLAY": ":99"})


def click(x, y, button=1, pause=0.35):
    env = {**os.environ, "DISPLAY": ":99"}
    subprocess.run(["xdotool", "mousemove", str(x), str(y)], env=env)
    time.sleep(0.15)
    subprocess.run(["xdotool", "click", str(button)], env=env)
    time.sleep(pause)


def ground(x, z):
    for y in range(80, 50, -1):
        for b in ("voidechoes:void_moss", "minecraft:end_stone", "voidechoes:voidstone_bricks", "voidechoes:chiseled_voidstone"):
            if "passed" in r.cmd(f"execute in minecraft:the_end if block {x} {y} {z} {b}"):
                return y
    raise RuntimeError(f"no ground at {x} {z}")


def warm(pos, wait):
    cam.tp(*pos)
    time.sleep(wait)


def shot(name, start, wait, action):
    if ONLY and name not in ONLY:
        return
    print("shot", name, flush=True)
    warm(start, wait)
    rec = Recorder(os.path.join(OUT, name + ".mp4"))
    action()
    rec.stop()


def spectator():
    r.cmd("gamemode spectator Tester")


# --------------------------------------------------------------- shots
spectator()
r.cmd("difficulty peaceful")
r.cmd("time set 6000")

GROVE_OPEN = [(812, 82, 778, 128, 16), (806, 79, 764, 122, 14), (798, 76, 748, 112, 11)]
shot("01_grove_open", GROVE_OPEN[0], 15, lambda: cam.fly(GROVE_OPEN, 8.0))

GROVE_LOW = [(700, 79, 792, -125, 14), (708, 76, 784, -130, 10), (718, 73, 776, -138, 8)]
shot("02_grove_low", GROVE_LOW[0], 15, lambda: cam.fly(GROVE_LOW, 6.0))

ISLAND = [(642, 82, 786, 26, 18), (636, 80, 796, 36, 16)]
shot("03_spire_island", ISLAND[0], 18, lambda: cam.fly(ISLAND, 5.0))

FIELD = [(-420, 82, -1102, -92, 24), (-412, 79, -1092, -112, 18), (-402, 77, -1080, -140, 16)]
shot("04_crystal_field", FIELD[0], 25, lambda: cam.fly(FIELD, 7.0))

ASH = [(-1300, 86, -2362, -138, 22), (-1290, 83, -2370, -118, 18), (-1278, 81, -2378, -100, 16)]
shot("05_ashen", ASH[0], 30, lambda: cam.fly(ASH, 6.0))


SHRINE = (705.5, 61.0, 770.5)


def shrine_scene():
    cam.orbit(*SHRINE, 11.5, 7, 236, 214, 8.0, look_y=SHRINE[1] + 0.5)


if not ONLY or "06_shrine" in ONLY:
    r.cmd("difficulty easy")
    for dx, dz, dy in ((2, -2, 3), (-3, 1, 4), (1, 3, 2)):
        r.cmd(D + f"summon voidechoes:void_wisp {SHRINE[0] + dx} {SHRINE[1] + dy} {SHRINE[2] + dz} {{NoGravity:1b}}")
shot("06_shrine", (697, 61.4, 762, -45, 20), 15, shrine_scene)

if not ONLY or "07_wisp" in ONLY:
    r.cmd("kill @e[type=voidechoes:void_wisp]")
    r.cmd("difficulty easy")
    r.cmd("kill @e[type=voidechoes:void_wisp]")
    WY = ground(752, 724) + 2.6
    r.cmd(D + f"summon voidechoes:void_wisp 752.5 {WY} 724.5 {{NoAI:1b,NoGravity:1b,PersistenceRequired:1b,Rotation:[90f,0f]}}")
else:
    WY = 66.5
shot("07_wisp", (750.5, WY - 0.9, 724.5, -90, 5), 10,
     lambda: cam.orbit(752.5, WY + 0.65, 724.5, 1.55, 0.2, 170, 290, 9.0, look_y=WY + 0.65))


def crafting():
    # GUI scale 2 at 1280x720: screen is 640x360 scaled, the 176x204 window starts at (232, 78)
    def p(sx, sy):
        return (232 + sx + 8) * 2, (78 + sy + 8) * 2

    def hot(c):
        return p(8 + c * 18, 180)

    def grid(row, col):
        return p(17 + col * 18, 17 + row * 18)

    time.sleep(1.0)
    click(640, 360, 3, 1.2)  # open the workbench
    for h, cells in ((0, [(1, 2)]), (1, [(2, 2)]), (2, [(3, 1), (3, 3)]), (3, [(3, 2)]), (4, [(4, 2)])):
        click(*hot(h), 1, 0.45)
        for rc in cells:
            click(*grid(*rc), 3, 0.45)
        click(*hot(h), 1, 0.45)
    time.sleep(1.2)
    click(*p(138, 53), 1, 0.8)
    click(*hot(7), 1, 1.5)
    key("Escape")
    time.sleep(0.8)


if not ONLY or "08_craft" in ONLY:
    r.cmd("kill @e[type=voidechoes:void_wisp]")
    r.cmd("difficulty peaceful")
    BY = ground(742, 718) + 1
    r.cmd(D + f"setblock 742 {BY} 718 voidechoes:void_workbench")
    PY = ground(742, 715) + 1
    r.cmd("gamemode survival Tester")
    r.cmd("clear Tester")
    for i in range(9):
        r.cmd(f"item replace entity Tester hotbar.{i} with air")
    for slot, item in enumerate(["voidechoes:void_crystal_shard", "minecraft:end_crystal", "minecraft:ender_pearl 2",
                                 "minecraft:netherite_ingot", "minecraft:end_rod"]):
        r.cmd(f"item replace entity Tester hotbar.{slot} with {item}")
    key("1")
if ONLY and "08_craft" not in ONLY:
    BY = PY = 64
import math as _m
_pitch = _m.degrees(_m.atan2((PY + 1.62) - (BY + 0.5), 2.4))
shot("08_craft", (742.5, PY, 715.6, 0, _pitch), 10, crafting)


def blinks():
    time.sleep(1.2)
    for _ in range(3):
        click(640, 360, 3, 2.1)
    time.sleep(0.8)


if not ONLY or "09_blink" in ONLY:
    r.cmd(D + "fill 742 55 718 742 80 718 air replace voidechoes:void_workbench")
    for block in ("voidechoes:void_fern", "voidechoes:glowbell"):
        r.cmd(D + f"fill 724 58 719 756 70 728 air replace {block}")
    r.cmd("gamemode survival Tester")
    r.cmd("clear Tester")
    r.cmd("item replace entity Tester hotbar.5 with voidechoes:voidcrystal_sword")
    key("6")   # first person, HUD on: the hand, the spinning frames and the cooldown are visible
shot("09_blink", (752.5, ground(752, 724) + 1, 724.5, 90, 9), 10, blinks)

if not ONLY or "10_end" in ONLY:
    spectator()
END = [(805, 80, 765, 114, 15), (822, 92, 780, 118, 19), (840, 110, 800, 124, 23)]
shot("10_end", END[0], 12, lambda: cam.fly(END, 8.0))
# ----------------------------------------------------------------- v1.2-1.3 additions
FALLS = [(17040, 52, 17108, 172, 4), (17052, 50, 17102, 190, 2), (17064, 49, 17094, 206, 0)]
shot("11_falls", FALLS[0], 25, lambda: cam.fly(FALLS, 7.0))

if not ONLY or "12_enderite" in ONLY:
    spectator()
    r.cmd("kill @e[type=minecraft:armor_stand]")
    gy = ground(746, 726) + 1
    r.cmd(D + f"summon minecraft:armor_stand 746.5 {gy} 726.5 {{ShowArms:1b,NoBasePlate:1b,Rotation:[200f,0f],"
          "Pose:{RightArm:[-60f,10f,0f],Head:[-5f,0f,0f]},"
          "ArmorItems:[{id:\"voidechoes:enderite_boots\",count:1},{id:\"voidechoes:enderite_leggings\",count:1},"
          "{id:\"voidechoes:enderite_chestplate\",count:1},{id:\"voidechoes:enderite_helmet\",count:1}],"
          "HandItems:[{id:\"voidechoes:enderite_sword\",count:1},{}]}")
else:
    gy = 63
shot("12_enderite", (746.5, gy, 730, 180, 10), 12,
     lambda: cam.orbit(746.5, gy + 1.05, 726.5, 2.1, 0.25, 70, 160, 8.0, look_y=gy + 1.05))


def storm():
    for delay, (x, z) in ((1.0, (635, 801)), (1.2, (648, 808)), (0.8, (624, 797)), (0.3, (652, 820)), (1.0, (628, 812)), (0.25, (640, 822))):
        time.sleep(delay)
        r.cmd(f"voidstorm strike {x} {z}")
    time.sleep(1.6)


if not ONLY or "13_storm" in ONLY:
    spectator()
STORM = [(642, 84, 784, 22, 6), (646, 84, 780, 28, 6)]
shot("13_storm", STORM[0], 15, lambda: (__import__("threading").Thread(target=lambda: cam.fly(STORM, 7.2)).start(), storm()))
# ----------------------------------------------------------------- v1.4 Aether Wings
def wings_flight():
    env = {**os.environ, "DISPLAY": ":99"}

    def jump():
        subprocess.run(["xdotool", "keydown", "space"], env=env)
        time.sleep(0.12)
        subprocess.run(["xdotool", "keyup", "space"], env=env)

    r.cmd(D + "setblock 804 107 806 air")
    time.sleep(0.5)
    jump()                      # open the wings
    for _ in range(3):
        time.sleep(1.0 if _ == 0 else 3.1)
        jump()                  # flap: dash up and forward
    time.sleep(2.0)


if not ONLY or "14_wings" in ONLY:
    r.cmd("difficulty peaceful")
    r.cmd("gamemode survival Tester")
    r.cmd("clear Tester")
    r.cmd("item replace entity Tester armor.chest with voidechoes:aether_wings")
    r.cmd(D + "setblock 804 107 806 minecraft:obsidian")
    key("F5")                   # third person, from behind
shot("14_wings", (804.5, 108, 806.5, 140, 22), 14, wings_flight)
if not ONLY or "14_wings" in ONLY:
    key("F5")
    key("F5")                   # back to first person
    spectator()
print("done", flush=True)
