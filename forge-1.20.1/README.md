# Illusioner Fixes (Forge 1.20.1)

> **Disclaimer:** Illusioner Fixes is an **unofficial, fan-made addon**. It is not affiliated with or endorsed by **fqkeh**, the author of *The Illusioner*, or **lukidonu**, the author of *Luki's Woodland Mansions*. All credit for those mods goes to their authors. This addon contains none of their code or assets (except, in the Forge 1.20.1 version, one small corrected Luki's loot-table file), and *The Illusioner* must be downloaded from its official page. Please report problems you only see with this addon to me, not to them.
>
> If fqkeh or lukidonu would like this addon changed or taken down, just contact me and I'll gladly do it right away.

Addon for fqkeh's [The Illusioner](https://www.curseforge.com/minecraft/mc-mods/the-illusioner) on Forge 1.20.1 (47.4.29+). Install it next to The Illusioner (1.2.0+).

- **Cutscene and arena chunks.** The arena's chunk ticket is re-asserted every tick, in case a chunk-management mod drops it, and the boss is only spawned once its chunk can hold it. (Since The Illusioner 1.2.0, it waits for a hidden or unloaded boss and cleans up leftover bosses itself, so those parts of this addon were removed.)
- **Corpse compatibility (optional).** Corpses of players who die in the arena are moved to the centre of the current circle, follow it across the shattered path ("come to me"), and are carried back to the overworld, to where their owner entered, when the encounter ends.
- **Luki's Woodland Mansions (optional).** Luki's mod turns the mansion into a jigsaw structure, so The Illusioner's basement was never built under it and the sculk note block had nothing to find. The basement is now built under Luki's stone maze level: walk in the front door, through the passage under the entrance terrace and down the corridor straight ahead, and the stairs go down in the east side of the central hall. Luki's floor is kept around the stairwell, and the whole basement stays under the mansion's footprint so it always generates complete. Only new mansions get it. Luki's 1.20 build also writes one loot table (`mansions:mansion_common`) in the newer 1.20.5 format, which 1.20.1 cannot read, so those chests were empty; a corrected copy is loaded on top.
- **Arena floor with Embeddium / Oculus (optional).** The arena floor is obsidian that The Illusioner hides while building chunk meshes, then covers with its own see-through mirror floor. Embeddium replaces vanilla's chunk builder, so the obsidian was never hidden and the floor looked like plain obsidian. The blocks The Illusioner hides (the arena obsidian, vanishing platforms, the blackout) are now hidden in Embeddium's chunk builder too. (1.4.4: removed a second floor tweak that, together with this one, made ghost models flash on screen.)
- **No death waypoints in the void (optional, Xaero's Minimap).** Dying in the corrupted void no longer drops a Xaero death waypoint there. The Illusioner respawns you where you entered the arena, and your items (or corpse) go there too, so a void waypoint is useless, and making one would also mark your real last death waypoint as old or delete it.
- **Now in The Illusioner itself (removed here in 1.5.0):** waiting for a hidden or unloaded boss, removing leftover bosses from interrupted fights, and the dry basement.

Build: put The Illusioner jar, widened for compiling only, in `libs/` as `illusioner-widened-1.jar` (see `build.gradle`), and run `./gradlew build` with Java 17.

## Tested alongside

The Illusioner 1.2.0, Embeddium 0.3.31 + Oculus 1.8.0 (Complementary Reimagined), Luki's Woodland Mansions 1.0 (1.20), Corpse 1.0.23, Xaero's Minimap 26.6.0 and World Map 1.47.0.
