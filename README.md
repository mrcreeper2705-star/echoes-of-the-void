# Echoes of the Void (Отголоски Пустоты)

Мод для **Minecraft 1.21.1 · Fabric**, который оживляет внешние острова Края.

## Скачать
- **Мод:** [voidechoes-1.4.4.jar](https://github.com/mrcreeper2705-star/echoes-of-the-void/raw/main/downloads/voidechoes-1.4.4.jar)
- Шаблон текстур брони: [EnderiteArmorTextures.zip](https://github.com/mrcreeper2705-star/echoes-of-the-void/raw/main/downloads/EnderiteArmorTextures.zip)
- Трейлер: [trailer.mp4](https://github.com/mrcreeper2705-star/echoes-of-the-void/raw/main/downloads/trailer.mp4)

Также нужен **Fabric API** для 1.21.1 (modrinth.com).

## Что в моде
- **3 биома:** Роща Пустоты, Кристальные шпили, Пепельные пределы.
- **Святилища Пустоты:** руины со спавнером виспов и сундуками с лутом.
- **Висп Пустоты:** летающий моб с анимированной моделью.
- **Верстак Пустоты 5×5:** Клинок из кристалла Пустоты (телепорт по ПКМ) и Эфирные крылья (рывок в полёте).
- **Эндерит:** руда внутри островов, слиток, шаблон улучшения, инструменты и броня выше незерита.
- **Звёздный эфир:** жидкость с эффектом «Невесомость», водопады с краёв островов в бездну.
- **Буря Пустоты:** редкая погода Края со Столпами Пустоты.

## Установка
1. Установите **Fabric Loader** для 1.21.1 (fabricmc.net) или выберите «Fabric 1.21.1» в своём лаунчере.
2. Скачайте **Fabric API** для 1.21.1 (modrinth.com).
3. Положите `voidechoes-*.jar` и Fabric API в папку `.minecraft/mods`.

Быстрый тест в мире с читами: `/execute in minecraft:the_end run tp @s 1000 100 1000`, `/locate biome voidechoes:void_grove`, `/voidstorm start 60`.

## Сборка из исходников
```
./gradlew build
```
Готовый jar появится в `build/libs/`. Текстуры и JSON генерируются скриптами в `tools/`.

Неофициальный фан-мод. NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFT.
