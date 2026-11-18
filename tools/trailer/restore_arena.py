"""Rebuilds natural-looking terrain over the flattened test arena (x 732..748, z 712..744)."""
import math
import random
import sys

sys.path.insert(0, __file__.rsplit("/", 1)[0])
from cam import Rcon

r = Rcon()
D = "execute in minecraft:the_end run "
X0, X1, Z0, Z1 = 732, 748, 712, 744

# 1. wipe what I built: the old shrine remnants, the arena trees and plants
for b in ["voidechoes:voidstone_bricks", "voidechoes:voidstone", "voidechoes:chiseled_voidstone", "minecraft:chest",
          "minecraft:spawner", "voidechoes:resonant_crystal"]:
    r.cmd(D + f"fill 728 56 708 752 80 748 air replace {b}")
r.cmd(D + f"fill {X0} 56 {Z0} {X1} 90 {Z1} air")


def top(x, z):
    for y in range(76, 52, -1):
        for b in ("voidechoes:void_moss", "minecraft:end_stone"):
            if "passed" in r.cmd(D.replace("run ", "") + f"if block {x} {y} {z} {b}"):
                return y
    return 63


# 2. heights along the border just outside the arena
north = {x: top(x, Z0 - 1) for x in range(X0 - 1, X1 + 2)}
south = {x: top(x, Z1 + 1) for x in range(X0 - 1, X1 + 2)}
west = {z: top(X0 - 1, z) for z in range(Z0 - 1, Z1 + 2)}
east = {z: top(X1 + 1, z) for z in range(Z0 - 1, Z1 + 2)}
print("border", min(north.values()), max(south.values()), min(west.values()), max(east.values()))

rng = random.Random(5)
phase = [rng.random() * 6.28 for _ in range(4)]
for x in range(X0, X1 + 1):
    for z in range(Z0, Z1 + 1):
        u = (x - (X0 - 1)) / (X1 - X0 + 2)
        v = (z - (Z0 - 1)) / (Z1 - Z0 + 2)
        # Coons patch: blend the four border curves
        h = ((1 - v) * north[x] + v * south[x] + (1 - u) * west[z] + u * east[z]
             - ((1 - u) * (1 - v) * north[X0 - 1] + u * (1 - v) * north[X1 + 1]
                + (1 - u) * v * south[X0 - 1] + u * v * south[X1 + 1]))
        h += 0.9 * math.sin(x * 0.35 + phase[0]) * math.sin(z * 0.27 + phase[1]) + 0.5 * math.sin(x * 0.8 + z * 0.6 + phase[2])
        h = int(round(h))
        r.cmd(D + f"fill {x} 56 {z} {x} {h - 1} {z} minecraft:end_stone")
        r.cmd(D + f"setblock {x} {h} {z} voidechoes:void_moss")
print("terrain rebuilt")

# 3. replant
for x, z in ((735, 717), (744, 738), (746, 721), (737, 733)):
    print(r.cmd(D.replace("run ", "") + f"positioned {x} 0 {z} positioned over motion_blocking_no_leaves run place feature voidechoes:aetherwood_tree ~ ~ ~"))
for f, x, z in (("void_fern_patch", 740, 725), ("glowbell_patch", 742, 716), ("void_fern_patch", 736, 740), ("glowbell_patch", 745, 731)):
    r.cmd(D.replace("run ", "") + f"positioned {x} 0 {z} positioned over motion_blocking_no_leaves run place feature voidechoes:{f} ~ ~ ~")
print("done")
