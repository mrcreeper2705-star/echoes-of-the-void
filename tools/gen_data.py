"""Writes all JSON assets and data for Echoes of the Void.

Run:  python3 tools/gen_data.py src/main/resources
"""
import json
import os
import sys

RES = sys.argv[1] if len(sys.argv) > 1 else "src/main/resources"
M = "voidechoes"
A = os.path.join(RES, "assets", M)
D = os.path.join(RES, "data")


def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write("\n")


def ns(p):
    return f"{M}:{p}"


# ------------------------------------------------------------------ assets
CUBES = ["voidstone", "voidstone_bricks", "chiseled_voidstone", "ashen_sand", "aetherwood_planks",
         "aetherwood_leaves", "resonant_crystal"]
PLANTS = ["glowbell", "void_fern", "ash_grass"]

for b in CUBES:
    w(f"{A}/blockstates/{b}.json", {"variants": {"": {"model": ns(f"block/{b}")}}})
    parent = "minecraft:block/leaves" if b.endswith("leaves") else "minecraft:block/cube_all"
    w(f"{A}/models/block/{b}.json", {"parent": parent, "textures": {"all": ns(f"block/{b}")}})
    w(f"{A}/models/item/{b}.json", {"parent": ns(f"block/{b}")})

# Ground cover with random rotation on top for a less tiled look.
w(f"{A}/blockstates/void_moss.json", {"variants": {"": [
    {"model": ns("block/void_moss")}, {"model": ns("block/void_moss"), "y": 90},
    {"model": ns("block/void_moss"), "y": 180}, {"model": ns("block/void_moss"), "y": 270}]}})
w(f"{A}/models/block/void_moss.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
    "top": ns("block/void_moss_top"), "side": ns("block/void_moss_side"), "bottom": "minecraft:block/end_stone"}})
w(f"{A}/models/item/void_moss.json", {"parent": ns("block/void_moss")})

w(f"{A}/blockstates/aetherwood_log.json", {"variants": {
    "axis=y": {"model": ns("block/aetherwood_log")},
    "axis=z": {"model": ns("block/aetherwood_log"), "x": 90},
    "axis=x": {"model": ns("block/aetherwood_log"), "x": 90, "y": 90}}})
w(f"{A}/models/block/aetherwood_log.json", {"parent": "minecraft:block/cube_column", "textures": {
    "end": ns("block/aetherwood_log_top"), "side": ns("block/aetherwood_log")}})
w(f"{A}/models/item/aetherwood_log.json", {"parent": ns("block/aetherwood_log")})

for p in PLANTS:
    w(f"{A}/blockstates/{p}.json", {"variants": {"": {"model": ns(f"block/{p}")}}})
    w(f"{A}/models/block/{p}.json", {"parent": "minecraft:block/cross", "textures": {"cross": ns(f"block/{p}")}})
    w(f"{A}/models/item/{p}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": ns(f"block/{p}")}})

for it in ["void_crystal_shard", "voidstep_charm"]:
    w(f"{A}/models/item/{it}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": ns(f"item/{it}")}})
w(f"{A}/models/item/voidcrystal_sword.json", {"parent": "minecraft:builtin/entity", "gui_light": "front"})
w(f"{A}/models/item/voidcrystal_sword_inner.json", {"parent": "minecraft:item/handheld",
                                                    "textures": {"layer0": ns("item/voidcrystal_sword")}})

w(f"{A}/blockstates/void_workbench.json", {"variants": {"": {"model": ns("block/void_workbench")}}})
w(f"{A}/models/block/void_workbench.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
    "top": ns("block/void_workbench_top"), "side": ns("block/void_workbench_side"), "bottom": ns("block/voidstone")}})
w(f"{A}/models/item/void_workbench.json", {"parent": ns("block/void_workbench")})
w(f"{A}/models/item/void_wisp_spawn_egg.json", {"parent": "minecraft:item/template_spawn_egg"})

w(f"{A}/particles/void_mote.json", {"textures": [ns(f"void_mote_{i}") for i in (0, 1, 2, 3, 2, 1)]})
w(f"{A}/particles/crystal_glint.json", {"textures": [ns(f"crystal_glint_{i}") for i in range(4)]})

names = {
    "void_moss": ("Void Moss", "Мох Пустоты"),
    "voidstone": ("Voidstone", "Камень Пустоты"),
    "voidstone_bricks": ("Voidstone Bricks", "Кирпичи из камня Пустоты"),
    "chiseled_voidstone": ("Chiseled Voidstone", "Резной камень Пустоты"),
    "ashen_sand": ("Ashen Sand", "Пепельный песок"),
    "aetherwood_log": ("Aetherwood Log", "Бревно эфирного древа"),
    "aetherwood_planks": ("Aetherwood Planks", "Доски эфирного древа"),
    "aetherwood_leaves": ("Aetherwood Leaves", "Листва эфирного древа"),
    "resonant_crystal": ("Resonant Crystal", "Резонирующий кристалл"),
    "glowbell": ("Glowbell", "Светоколокольчик"),
    "void_fern": ("Void Fern", "Папоротник Пустоты"),
    "ash_grass": ("Ash Grass", "Пепельная трава"),
    "void_workbench": ("Void Workbench", "Верстак Пустоты"),
}
items = {
    "void_crystal_shard": ("Void Crystal Shard", "Осколок кристалла Пустоты"),
    "voidstep_charm": ("Voidstep Charm", "Амулет шага сквозь Пустоту"),
    "voidcrystal_sword": ("Voidcrystal Blade", "Клинок из кристалла Пустоты"),
    "void_wisp_spawn_egg": ("Void Wisp Spawn Egg", "Яйцо призыва виспа Пустоты"),
}
en, ru = {}, {}
for k, (e, r) in names.items():
    en[f"block.{M}.{k}"], ru[f"block.{M}.{k}"] = e, r
for k, (e, r) in items.items():
    en[f"item.{M}.{k}"], ru[f"item.{M}.{k}"] = e, r
extra = {
    f"entity.{M}.void_wisp": ("Void Wisp", "Висп Пустоты"),
    f"itemGroup.{M}": ("Echoes of the Void", "Отголоски Пустоты"),
    f"biome.{M}.void_grove": ("Void Grove", "Роща Пустоты"),
    f"biome.{M}.crystal_spires": ("Crystal Spires", "Кристальные шпили"),
    f"biome.{M}.ashen_reach": ("Ashen Reach", "Пепельные пределы"),
    f"item.{M}.artifact": ("Artifact", "Артефакт"),
    f"item.{M}.voidstep_charm.tooltip": ("Use: blink up to 10 blocks where you look",
                                         "ПКМ: мгновенный рывок до 10 блоков по взгляду"),
    f"container.{M}.void_workbench": ("Void Workbench", "Верстак Пустоты"),
    f"item.{M}.voidcrystal_sword.tooltip.blink": ("Use: teleport to the block you look at (-3 durability, 1.5 damage)",
                                                  "ПКМ: телепорт к блоку, на который смотришь (−3 прочности, 1,5 урона)"),
    f"item.{M}.voidcrystal_sword.tooltip": ("Every 3rd hit echoes: bonus damage and slowness around the target",
                                            "Каждый 3-й удар — эхо: доп. урон и замедление вокруг цели"),
}
for k, (e, r) in extra.items():
    en[k], ru[k] = e, r
w(f"{A}/lang/en_us.json", en)
w(f"{A}/lang/ru_ru.json", ru)

# ------------------------------------------------------------------ enderite + ooze assets
TOOLS = ["sword", "pickaxe", "axe", "shovel", "hoe"]
ARMOR = ["helmet", "chestplate", "leggings", "boots"]
for t in TOOLS:
    w(f"{A}/models/item/enderite_{t}.json", {"parent": "minecraft:item/handheld", "textures": {"layer0": ns(f"item/enderite_{t}")}})
for it in ["aether_wings"] + [f"enderite_{a}" for a in ARMOR] + ["enderite_scrap", "enderite_upgrade_smithing_template"]:
    w(f"{A}/models/item/{it}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": ns(f"item/{it}")}})
# Vanilla-looking ingot and bucket: vanilla textures are referenced (not copied); ours are overlays.
w(f"{A}/models/item/enderite_ingot.json", {"parent": "minecraft:item/generated", "textures": {
    "layer0": "minecraft:item/iron_ingot", "layer1": ns("item/enderite_ingot_shine")}})
w(f"{A}/models/item/void_ooze_bucket.json", {"parent": "minecraft:item/generated", "textures": {
    "layer0": "minecraft:item/bucket", "layer1": ns("item/void_ooze_bucket_fluid")}})
w(f"{A}/blockstates/enderite_ore.json", {"variants": {"": {"model": ns("block/enderite_ore")}}})
w(f"{A}/models/block/enderite_ore.json", {"parent": "minecraft:block/cube_all", "textures": {"all": ns("block/enderite_ore")}})
w(f"{A}/models/item/enderite_ore.json", {"parent": ns("block/enderite_ore")})
w(f"{A}/blockstates/void_ooze.json", {"variants": {"": {"model": ns("block/void_ooze")}}})
w(f"{A}/models/block/void_ooze.json", {"textures": {"particle": ns("block/void_ooze_still")}})

more = {
    "block.voidechoes.enderite_ore": ("Enderite Ore", "Эндеритовая руда"),
    "block.voidechoes.void_ooze": ("Stellar Aether", "Звёздный эфир"),
    "item.voidechoes.void_ooze_bucket": ("Stellar Aether Bucket", "Ведро звёздного эфира"),
    "item.voidechoes.enderite_scrap": ("Enderite Scrap", "Обломки эндерита"),
    "item.voidechoes.enderite_ingot": ("Enderite Ingot", "Эндеритовый слиток"),
    "item.voidechoes.enderite_upgrade_smithing_template": ("Smithing Template", "Кузнечный шаблон"),
    "upgrade.voidechoes.enderite_upgrade": ("Enderite Upgrade", "Эндеритовое улучшение"),
    "item.voidechoes.smithing_template.enderite_upgrade.applies_to": ("Netherite Equipment", "Незеритовое снаряжение"),
    "item.voidechoes.smithing_template.enderite_upgrade.ingredients": ("Enderite Ingot", "Эндеритовый слиток"),
    "item.voidechoes.smithing_template.enderite_upgrade.base_slot_description": ("Add netherite armor, weapon, or tool", "Поместите незеритовую броню, оружие или инструмент"),
    "item.voidechoes.smithing_template.enderite_upgrade.additions_slot_description": ("Add Enderite Ingot", "Поместите эндеритовый слиток"),
    "effect.voidechoes.weightless": ("Weightless", "Невесомость"),
    "item.voidechoes.aether_wings": ("Aether Wings", "Эфирные крылья"),
    "item.voidechoes.aether_wings.tooltip": ("In flight, press Jump to beat the wings: +5 blocks, every 10 s",
                                             "В полёте нажмите Прыжок: взмах +5 блоков, раз в 10 с"),
    "commands.voidechoes.voidstorm.start": ("A Void Storm gathers over the End", "Над Краем собирается Буря Пустоты"),
    "commands.voidechoes.voidstorm.stop": ("The Void Storm subsides", "Буря Пустоты стихает"),
    "item.voidechoes.enderite_sword": ("Enderite Sword", "Эндеритовый меч"),
    "item.voidechoes.enderite_pickaxe": ("Enderite Pickaxe", "Эндеритовая кирка"),
    "item.voidechoes.enderite_axe": ("Enderite Axe", "Эндеритовый топор"),
    "item.voidechoes.enderite_shovel": ("Enderite Shovel", "Эндеритовая лопата"),
    "item.voidechoes.enderite_hoe": ("Enderite Hoe", "Эндеритовая мотыга"),
    "item.voidechoes.enderite_helmet": ("Enderite Helmet", "Эндеритовый шлем"),
    "item.voidechoes.enderite_chestplate": ("Enderite Chestplate", "Эндеритовый нагрудник"),
    "item.voidechoes.enderite_leggings": ("Enderite Leggings", "Эндеритовые поножи"),
    "item.voidechoes.enderite_boots": ("Enderite Boots", "Эндеритовые ботинки"),
}
for k, (e, r_) in more.items():
    en[k], ru[k] = e, r_
w(f"{A}/lang/en_us.json", en)
w(f"{A}/lang/ru_ru.json", ru)

# ------------------------------------------------------------------ loot
MD = os.path.join(D, M)


def self_drop(b):
    return {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": ns(b)}],
                                                   "conditions": [{"condition": "minecraft:survives_explosion"}]}]}


def silk_or(b, other, count=None):
    alt = {"type": "minecraft:item", "name": other, "functions": [{"function": "minecraft:explosion_decay"}]}
    if count:
        alt["functions"].insert(0, {"function": "minecraft:set_count", "count": count})
        alt["functions"].insert(1, {"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune",
                                    "formula": "minecraft:uniform_bonus_count", "parameters": {"bonusMultiplier": 1}})
    return {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:alternatives", "children": [
        {"type": "minecraft:item", "name": ns(b), "conditions": [{"condition": "minecraft:match_tool", "predicate": {
            "predicates": {"minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}]},
        alt]}]}]}


for b in ["void_workbench", "voidstone", "voidstone_bricks", "chiseled_voidstone", "ashen_sand", "aetherwood_log",
          "aetherwood_planks", "glowbell", "void_fern", "ash_grass"]:
    w(f"{MD}/loot_table/blocks/{b}.json", self_drop(b))
w(f"{MD}/loot_table/blocks/void_moss.json", silk_or("void_moss", "minecraft:end_stone"))
w(f"{MD}/loot_table/blocks/resonant_crystal.json",
  silk_or("resonant_crystal", ns("void_crystal_shard"), {"type": "minecraft:uniform", "min": 2, "max": 4}))
w(f"{MD}/loot_table/blocks/aetherwood_leaves.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [
    {"type": "minecraft:item", "name": ns("aetherwood_leaves"), "conditions": [{"condition": "minecraft:match_tool",
                                                                                "predicate": {"items": "minecraft:shears"}}]}]}]})

w(f"{MD}/loot_table/entities/void_wisp.json", {"type": "minecraft:entity", "pools": [{"rolls": 1, "entries": [
    {"type": "minecraft:item", "name": ns("void_crystal_shard"), "functions": [
        {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 0, "max": 2}},
        {"function": "minecraft:enchanted_count_increase", "enchantment": "minecraft:looting",
         "count": {"type": "minecraft:uniform", "min": 0, "max": 1}}]}]}]})


def entry(item, weight, lo=1, hi=1, extra_fn=None):
    e = {"type": "minecraft:item", "name": item, "weight": weight}
    fns = []
    if hi > 1:
        fns.append({"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}})
    if extra_fn:
        fns.append(extra_fn)
    if fns:
        e["functions"] = fns
    return e


w(f"{MD}/loot_table/chests/void_shrine.json", {"type": "minecraft:chest", "pools": [
    {"rolls": {"type": "minecraft:uniform", "min": 3, "max": 6}, "entries": [
        entry(ns("void_crystal_shard"), 30, 2, 6),
        entry("minecraft:ender_pearl", 20, 1, 3),
        entry("minecraft:experience_bottle", 15, 2, 5),
        entry("minecraft:golden_apple", 8),
        entry("minecraft:diamond", 8, 1, 3),
        entry("minecraft:emerald", 12, 2, 6),
        entry(ns("glowbell"), 10, 2, 5),
        entry(ns("chiseled_voidstone"), 8, 1, 3),
    ]},
    {"rolls": 1, "entries": [
        {"type": "minecraft:empty", "weight": 57},
        entry(ns("voidstep_charm"), 20),
        entry(ns("voidcrystal_sword"), 5, extra_fn={"function": "minecraft:enchant_randomly"}),
        entry("minecraft:enchanted_golden_apple", 3),
        entry(ns("enderite_upgrade_smithing_template"), 8),
        entry("minecraft:echo_shard", 7, 1, 2),
    ]},
]})

# ------------------------------------------------------------------ recipes
R = f"{MD}/recipe"
# Void Workbench (5x5) recipes
w(f"{R}/voidcrystal_sword.json", {"type": ns("void_crafting"),
                                  "pattern": [" S ", " C ", "PNP", " R "],
                                  "key": {"S": {"item": ns("void_crystal_shard")}, "C": {"item": "minecraft:end_crystal"},
                                          "P": {"item": "minecraft:ender_pearl"}, "N": {"item": "minecraft:netherite_ingot"},
                                          "R": {"item": "minecraft:end_rod"}},
                                  "result": {"id": ns("voidcrystal_sword"), "count": 1}})
w(f"{R}/aether_wings.json", {"type": ns("void_crafting"),
                              "pattern": ["M   M", "MS SM", "MSWSM", " EAE "],
                              "key": {"M": {"item": "minecraft:phantom_membrane"}, "S": {"item": ns("void_crystal_shard")},
                                      "W": {"item": "minecraft:elytra"}, "E": {"item": ns("enderite_ingot")},
                                      "A": {"item": ns("void_ooze_bucket")}},
                              "result": {"id": ns("aether_wings"), "count": 1}})
w(f"{R}/void_workbench.json", {"type": "minecraft:crafting_shaped", "category": "misc",
                               "pattern": ["SCS", "OTO", "BBB"],
                               "key": {"S": {"item": ns("void_crystal_shard")}, "C": {"item": ns("chiseled_voidstone")},
                                       "O": {"item": "minecraft:obsidian"}, "T": {"item": "minecraft:crafting_table"},
                                       "B": {"item": ns("voidstone_bricks")}},
                               "result": {"id": ns("void_workbench"), "count": 1}})
w(f"{R}/voidstep_charm.json", {"type": "minecraft:crafting_shaped", "category": "misc",
                               "pattern": ["PSP", "SES", "PSP"],
                               "key": {"P": {"item": "minecraft:ender_pearl"}, "S": {"item": ns("void_crystal_shard")},
                                       "E": {"item": "minecraft:ender_eye"}},
                               "result": {"id": ns("voidstep_charm"), "count": 1}})
w(f"{R}/resonant_crystal.json", {"type": "minecraft:crafting_shaped", "category": "building",
                                 "pattern": ["SS", "SS"], "key": {"S": {"item": ns("void_crystal_shard")}},
                                 "result": {"id": ns("resonant_crystal"), "count": 1}})
w(f"{R}/voidstone_bricks.json", {"type": "minecraft:crafting_shaped", "category": "building",
                                 "pattern": ["VV", "VV"], "key": {"V": {"item": ns("voidstone")}},
                                 "result": {"id": ns("voidstone_bricks"), "count": 4}})
w(f"{R}/chiseled_voidstone.json", {"type": "minecraft:crafting_shapeless", "category": "building",
                                   "ingredients": [{"item": ns("voidstone_bricks")}, {"item": ns("void_crystal_shard")}],
                                   "result": {"id": ns("chiseled_voidstone"), "count": 1}})
w(f"{R}/aetherwood_planks.json", {"type": "minecraft:crafting_shapeless", "category": "building",
                                  "group": "planks", "ingredients": [{"item": ns("aetherwood_log")}],
                                  "result": {"id": ns("aetherwood_planks"), "count": 4}})
w(f"{R}/voidstone_bricks_stonecutting.json", {"type": "minecraft:stonecutting",
                                              "ingredient": {"item": ns("voidstone")},
                                              "result": {"id": ns("voidstone_bricks"), "count": 1}})

w(f"{MD}/loot_table/blocks/enderite_ore.json", self_drop("enderite_ore"))

for kind, time_ in (("smelting", 200), ("blasting", 100)):
    w(f"{R}/enderite_scrap_from_{kind}.json", {"type": f"minecraft:{kind}", "category": "misc",
                                              "ingredient": {"item": ns("enderite_ore")},
                                              "result": {"id": ns("enderite_scrap")}, "experience": 2.0, "cookingtime": time_})
w(f"{R}/enderite_ingot.json", {"type": "minecraft:crafting_shapeless", "category": "misc",
                               "ingredients": [{"item": ns("enderite_scrap")}] * 4 + [{"item": ns("void_crystal_shard")}] * 4
                               + [{"item": "minecraft:netherite_ingot"}],
                               "result": {"id": ns("enderite_ingot"), "count": 1}})
w(f"{R}/enderite_upgrade_smithing_template.json", {"type": "minecraft:crafting_shaped", "category": "misc",
                                                   "pattern": ["SXS", "SES", "SSS"],
                                                   "key": {"S": {"item": ns("void_crystal_shard")},
                                                           "X": {"item": ns("enderite_upgrade_smithing_template")},
                                                           "E": {"item": "minecraft:end_stone"}},
                                                   "result": {"id": ns("enderite_upgrade_smithing_template"), "count": 2}})
for piece in TOOLS + ARMOR:
    w(f"{R}/enderite_{piece}_smithing.json", {"type": "minecraft:smithing_transform",
                                             "template": {"item": ns("enderite_upgrade_smithing_template")},
                                             "base": {"item": f"minecraft:netherite_{piece}"},
                                             "addition": {"item": ns("enderite_ingot")},
                                             "result": {"id": ns(f"enderite_{piece}"), "count": 1}})

# ------------------------------------------------------------------ tags
MC = os.path.join(D, "minecraft", "tags")


def tag(path, values):
    w(path, {"replace": False, "values": values})


tag(f"{MC}/block/mineable/pickaxe.json", [ns(b) for b in
                                           ["void_moss", "voidstone", "voidstone_bricks", "chiseled_voidstone", "resonant_crystal", "void_workbench"]])
tag(f"{MC}/block/mineable/axe.json", [ns("aetherwood_log"), ns("aetherwood_planks")])
tag(f"{MC}/block/mineable/hoe.json", [ns("aetherwood_leaves")])
tag(f"{MC}/block/mineable/shovel.json", [ns("ashen_sand")])
tag(f"{MC}/block/needs_iron_tool.json", [ns("resonant_crystal")])
tag(f"{MC}/block/logs.json", [ns("aetherwood_log")])
tag(f"{MC}/block/leaves.json", [ns("aetherwood_leaves")])
tag(f"{MC}/block/planks.json", [ns("aetherwood_planks")])
tag(f"{MC}/block/dragon_immune.json", [])
tag(f"{MC}/item/logs.json", [ns("aetherwood_log")])
tag(f"{MC}/item/leaves.json", [ns("aetherwood_leaves")])
tag(f"{MC}/item/planks.json", [ns("aetherwood_planks")])
tag(f"{MC}/item/swords.json", [ns("voidcrystal_sword")])
for t in ["sword", "sharp_weapon", "weapon", "fire_aspect", "durability", "vanishing"]:
    tag(f"{MC}/item/enchantable/{t}.json", [ns("voidcrystal_sword")])
tag(f"{MC}/item/enchantable/durability.json", [ns("voidcrystal_sword"), ns("voidstep_charm")])
tag(f"{MC}/item/enchantable/vanishing.json", [ns("voidcrystal_sword"), ns("voidstep_charm")])
os.remove(f"{MC}/block/dragon_immune.json")

tag(f"{MC}/block/mineable/pickaxe.json", [ns(b) for b in
                                           ["void_moss", "voidstone", "voidstone_bricks", "chiseled_voidstone", "resonant_crystal",
                                            "void_workbench", "enderite_ore"]])
for tier in ("wooden", "stone", "iron", "gold", "diamond"):
    tag(f"{MC}/block/incorrect_for_{tier}_tool.json", [ns("enderite_ore")])
E_TOOLS = [ns(f"enderite_{t}") for t in TOOLS]
E_ARMOR = [ns(f"enderite_{a}") for a in ARMOR]
tag(f"{D}/{M}/tags/item/void_proof.json", E_TOOLS + E_ARMOR + [ns("aether_wings"), ns("enderite_scrap"), ns("enderite_ingot"), ns("enderite_ore"),
                                                         ns("enderite_upgrade_smithing_template")])
for t in TOOLS:
    tag(f"{MC}/item/{t}s.json", [ns(f"enderite_{t}")] + ([ns("voidcrystal_sword")] if t == "sword" else []))
ench = {
    "sword": [ns("voidcrystal_sword"), ns("enderite_sword")],
    "sharp_weapon": [ns("voidcrystal_sword"), ns("enderite_sword"), ns("enderite_axe")],
    "weapon": [ns("voidcrystal_sword"), ns("enderite_sword"), ns("enderite_axe")],
    "fire_aspect": [ns("voidcrystal_sword"), ns("enderite_sword")],
    "mining": [ns("enderite_pickaxe"), ns("enderite_axe"), ns("enderite_shovel"), ns("enderite_hoe")],
    "mining_loot": [ns("enderite_pickaxe"), ns("enderite_axe"), ns("enderite_shovel"), ns("enderite_hoe")],
    "durability": [ns("voidcrystal_sword"), ns("voidstep_charm"), ns("aether_wings")] + E_TOOLS + E_ARMOR,
    "vanishing": [ns("voidcrystal_sword"), ns("voidstep_charm"), ns("aether_wings")] + E_TOOLS + E_ARMOR,
    "equippable": E_ARMOR + [ns("aether_wings")],
    "armor": E_ARMOR,
    "head_armor": [ns("enderite_helmet")], "chest_armor": [ns("enderite_chestplate")],
    "leg_armor": [ns("enderite_leggings")], "foot_armor": [ns("enderite_boots")],
}
for k, v in ench.items():
    tag(f"{MC}/item/enchantable/{k}.json", v)
tag(f"{MC}/item/trimmable_armor.json", E_ARMOR)
tag(f"{MC}/item/beacon_payment_items.json", [ns("enderite_ingot")])

BIOMES = ["void_grove", "crystal_spires", "ashen_reach"]
tag(f"{MC}/worldgen/biome/has_structure/end_city.json", [ns(b) for b in BIOMES])
tag(f"{D}/c/tags/worldgen/biome/is_end.json", [ns(b) for b in BIOMES])
tag(f"{D}/c/tags/worldgen/biome/is_outer_end_island.json", [ns(b) for b in BIOMES])

# ------------------------------------------------------------------ worldgen
WG = f"{MD}/worldgen"


def state(name, props=None):
    s = {"Name": name if ":" in name else ns(name)}
    if props:
        s["Properties"] = props
    return s


def simple(name, props=None):
    return {"type": "minecraft:simple_state_provider", "state": state(name, props)}


def weighted(*pairs):
    return {"type": "minecraft:weighted_state_provider",
            "entries": [{"weight": wt, "data": state(n)} for n, wt in pairs]}


def cf(name, ftype, config):
    w(f"{WG}/configured_feature/{name}.json", {"type": ftype, "config": config})


def pf(name, feature, placement):
    w(f"{WG}/placed_feature/{name}.json", {"feature": ns(feature), "placement": placement})


def biome_filter():
    return {"type": "minecraft:biome"}


def surface_placement(count=None, rarity=None):
    p = []
    if count is not None:
        p.append({"type": "minecraft:count", "count": count})
    if rarity is not None:
        p.append({"type": "minecraft:rarity_filter", "chance": rarity})
    p += [{"type": "minecraft:in_square"},
          {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"},
          biome_filter()]
    return p


# surface repaint
for b, top, under, depth in [("void_grove", simple("void_moss"), simple("minecraft:end_stone"), 0),
                             ("crystal_spires", weighted(("voidstone", 6), ("minecraft:end_stone", 1)),
                              simple("voidstone"), 2),
                             ("ashen_reach", simple("ashen_sand"), simple("ashen_sand"), 2)]:
    cf(f"{b}_surface", ns("surface_cover"), {"biome": ns(b), "top": top, "under": under, "depth": depth})
    pf(f"{b}_surface", f"{b}_surface", [])

# Aetherwood: drooping, glowing canopy trees.
cf("aetherwood_tree", "minecraft:tree", {
    "ignore_vines": True, "force_dirt": False,
    "minimum_size": {"type": "minecraft:two_layers_feature_size", "limit": 1, "lower_size": 0, "upper_size": 2},
    "dirt_provider": simple("void_moss"),
    "trunk_provider": simple("aetherwood_log", {"axis": "y"}),
    "foliage_provider": simple("aetherwood_leaves", {"distance": "7", "persistent": "false", "waterlogged": "false"}),
    "trunk_placer": {"type": "minecraft:cherry_trunk_placer", "base_height": 8, "height_rand_a": 2, "height_rand_b": 1,
                     "branch_count": {"type": "minecraft:weighted_list", "distribution": [
                         {"weight": 1, "data": 1}, {"weight": 2, "data": 2}, {"weight": 1, "data": 3}]},
                     "branch_horizontal_length": {"type": "minecraft:uniform", "min_inclusive": 2, "max_inclusive": 5},
                     "branch_start_offset_from_top": {"min_inclusive": -5, "max_inclusive": -3},
                     "branch_end_offset_from_top": {"type": "minecraft:uniform", "min_inclusive": -1, "max_inclusive": 1}},
    "foliage_placer": {"type": "minecraft:cherry_foliage_placer", "radius": 4, "offset": 0, "height": 5,
                       "wide_bottom_layer_hole_chance": 0.3, "corner_hole_chance": 0.3,
                       "hanging_leaves_chance": 0.3, "hanging_leaves_extension_chance": 0.5},
    "decorators": []})
pf("aetherwood_tree", "aetherwood_tree",
   [{"type": "minecraft:count", "count": {"type": "minecraft:weighted_list", "distribution": [
       {"weight": 3, "data": 1}, {"weight": 2, "data": 2}, {"weight": 1, "data": 0}]}},
    {"type": "minecraft:in_square"},
    {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"},
    {"type": "minecraft:block_predicate_filter", "predicate": {
        "type": "minecraft:matching_blocks", "offset": [0, -1, 0], "blocks": ns("void_moss")}},
    biome_filter()])


def patch(name, block, tries, spread):
    cf(name, "minecraft:random_patch", {"tries": tries, "xz_spread": spread, "y_spread": 3, "feature": {
        "feature": {"type": "minecraft:simple_block", "config": {"to_place": simple(block)}},
        "placement": [{"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:all_of", "predicates": [
            {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"},
            {"type": "minecraft:would_survive", "state": state(block)}]}}]}})


patch("glowbell_patch", "glowbell", 40, 6)
patch("void_fern_patch", "void_fern", 64, 7)
patch("ash_grass_patch", "ash_grass", 56, 7)
pf("glowbell_patch", "glowbell_patch", surface_placement(count=3))
pf("void_fern_patch", "void_fern_patch", surface_placement(count=5))
pf("ash_grass_patch", "ash_grass_patch", surface_placement(count=4))
pf("glowbell_patch_sparse", "glowbell_patch", surface_placement(rarity=3))

cf("crystal_spire", ns("crystal_spike"), {
    "body": weighted(("resonant_crystal", 1)), "collar": weighted(("voidstone", 3), ("voidstone_bricks", 1)),
    "tip": simple("resonant_crystal"), "height": {"type": "minecraft:uniform", "min_inclusive": 6, "max_inclusive": 22},
    "lean": 0.35})
cf("crystal_shardling", ns("crystal_spike"), {
    "body": simple("resonant_crystal"), "collar": simple("voidstone"), "tip": simple("resonant_crystal"),
    "height": {"type": "minecraft:uniform", "min_inclusive": 2, "max_inclusive": 5}, "lean": 0.6})
cf("ash_pillar", ns("crystal_spike"), {
    "body": weighted(("voidstone", 4), ("ashen_sand", 1)), "collar": simple("ashen_sand"),
    "tip": simple("chiseled_voidstone"),
    "height": {"type": "minecraft:uniform", "min_inclusive": 5, "max_inclusive": 16}, "lean": 0.5})
pf("crystal_spire", "crystal_spire", surface_placement(rarity=2))
pf("crystal_shardling", "crystal_shardling", surface_placement(count=2))
pf("ash_pillar", "ash_pillar", surface_placement(rarity=3))

cf("ore_enderite", "minecraft:ore", {"size": 3, "discard_chance_on_air_exposure": 1.0, "targets": [
    {"target": {"predicate_type": "minecraft:block_match", "block": "minecraft:end_stone"}, "state": state("enderite_ore")}]})
pf("ore_enderite", "ore_enderite", [{"type": "minecraft:count", "count": 2}, {"type": "minecraft:in_square"},
                                    {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform",
                                     "min_inclusive": {"absolute": 15}, "max_inclusive": {"absolute": 60}}},
                                    biome_filter()])
cf("void_ooze_lake", "minecraft:lake", {"fluid": simple("void_ooze", {"level": "0"}), "barrier": simple("void_moss")})
pf("void_ooze_lake", "void_ooze_lake", surface_placement(rarity=6))
pf("void_ooze_lake_rare", "void_ooze_lake", surface_placement(rarity=12))

# Stellar Aether falls: springs in island cliffs that pour into the abyss.
cf("aether_fall", ns("aether_fall"), {})
pf("aether_fall", "aether_fall", surface_placement(count=2))

cf("void_shrine", ns("void_shrine"), {})
pf("void_shrine", "void_shrine", surface_placement(rarity=55))


def features(raw, vegetal, surface_structures=(), lakes=()):
    steps = [[] for _ in range(11)]
    steps[0] = [ns(x) for x in raw]
    steps[1] = [ns(x) for x in lakes]
    steps[6] = [ns("ore_enderite")]
    steps[8] = [ns("aether_fall")]
    steps[4] = ["minecraft:end_gateway_return"] + [ns(x) for x in surface_structures]
    steps[9] = [ns(x) for x in vegetal]
    return steps


def biome(name, fog, particle, prob, raw, vegetal, structures, wisp_weight, ambient=None, mood=None, lakes=()):
    effects = {"fog_color": fog, "sky_color": 0, "water_color": 0x3f76e4, "water_fog_color": 0x050533,
               "particle": {"options": {"type": particle}, "probability": prob}}
    if ambient:
        effects["ambient_sound"] = ambient
    if mood:
        effects["mood_sound"] = {"sound": mood, "tick_delay": 6000, "block_search_extent": 8, "offset": 2.0}
    w(f"{WG}/biome/{name}.json", {
        "has_precipitation": False, "temperature": 0.5, "downfall": 0.5, "effects": effects,
        "spawners": {"monster": [
            {"type": "minecraft:enderman", "weight": 10, "minCount": 1, "maxCount": 4},
            {"type": ns("void_wisp"), "weight": wisp_weight, "minCount": 1, "maxCount": 2}],
            "creature": [], "ambient": [], "water_creature": [], "underground_water_creature": [],
            "water_ambient": [], "misc": [], "axolotls": []},
        "spawn_costs": {}, "carvers": {},
        "features": features(raw, vegetal, structures, lakes)})


biome("void_grove", 0x2a1046, ns("void_mote"), 0.012,
      ["void_grove_surface"], ["aetherwood_tree", "glowbell_patch", "void_fern_patch"], ["void_shrine"], 4,
      mood="minecraft:ambient.warped_forest.mood", lakes=["void_ooze_lake"])
biome("crystal_spires", 0x0f2a40, ns("crystal_glint"), 0.006,
      ["crystal_spires_surface"], ["crystal_spire", "crystal_shardling", "glowbell_patch_sparse"], ["void_shrine"], 6,
      mood="minecraft:ambient.soul_sand_valley.mood", lakes=["void_ooze_lake_rare"])
biome("ashen_reach", 0x3a3430, "minecraft:white_ash", 0.06,
      ["ashen_reach_surface"], ["ash_pillar", "ash_grass_patch"], ["void_shrine"], 3,
      ambient="minecraft:ambient.basalt_deltas.loop", mood="minecraft:ambient.basalt_deltas.mood")

print("data written to", RES)
