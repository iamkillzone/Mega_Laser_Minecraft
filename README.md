# Mega Laser 90 — Cinematic Edition

Minecraft **Java 1.21.1** Fabric mod: 90 laser modes, destruction caps from 100 to 100,000 blocks, five 5-second animated cinematics on modes #86–#90.

## Build
GitHub Actions → **Build Mega Laser 90 Mod** → download the **MegaLaser90-Cinematics-JAR** artifact, then extract `megalaser90-1.1.0.jar` and move it to your Minecraft `mods` folder. Install Fabric Loader and Fabric API for Minecraft 1.21.1.

Alternatively, with Java 21 and Gradle 8.10.2: `gradle build`.

## Play
Creative Mode: `/give @s megalaser90:laser_pointer`. Hold it, press **M** or **Shift+right-click** to select a mode. Right-click a block to attack. Space/Escape skips cinematic UI, not the blast.

**WARNING:** This permanently destroys world blocks. Back up worlds first. Mode limits are caps; unloaded chunks and protected blocks are skipped.

Built and tested status is reported by GitHub Actions; a successful build does not substitute for an in-game test.
