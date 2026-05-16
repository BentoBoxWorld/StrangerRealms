# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & test

Maven project, Java 21. Default goal is `clean package`.

- Build the addon jar: `mvn clean package` (output in `target/`, named `StrangerRealms-${build.version}-SNAPSHOT-LOCAL.jar` locally)
- Run all tests: `mvn test`
- Run a single test class: `mvn test -Dtest=StrangerRealmsTest`
- Run a single test method: `mvn test -Dtest=StrangerRealmsTest#methodName`
- Skip tests during a build: `mvn package -DskipTests`

Surefire is preconfigured with the `--add-opens` flags required by MockBukkit on JDK 21 — don't run tests outside Maven without replicating them. JaCoCo runs on the `test` phase and writes an XML report to `target/site/jacoco/`.

Versioning is driven by Maven properties: `build.version` (in `pom.xml`) sets the release version, and the `master` / `ci` profiles flip `-SNAPSHOT` and `-bNNN` build numbers automatically from Jenkins env vars. Bump `build.version` in `pom.xml` to cut a new release. CI runs on Jenkins at codemc.org (see `pom.xml` `<ciManagement>`) and on GitHub Actions (`.github/workflows/build.yml`).

## Architecture

StrangerRealms is a **BentoBox GameModeAddon** — it does not run as a standalone Bukkit/Paper plugin. The runtime entry point Paper sees is `StrangerRealmsPladdon` (a `Pladdon`), which constructs and returns the real addon, `StrangerRealms`. `plugin.yml` points Paper at the Pladdon; `addon.yml` points BentoBox at `StrangerRealms`. When changing entry points or lifecycle, both files matter.

### Addon lifecycle (StrangerRealms.java)

BentoBox calls these in order — keep work in the right phase:

1. `onLoad()` — load `Settings` from `config.yml`, construct the `NetherChunkMaker` (it's needed during world creation), and register player/admin command trees (`ClaimCommand`, `UnclaimCommand`, `SpawnCommand`, `WorldBorderCommand`).
2. `createWorlds()` — creates the overworld, nether (Upside Down), and end via `WorldCreator`. The nether uses the custom `NetherChunks` ChunkGenerator + `NetherBiomeProvider`. If `Bukkit.getWorld(worldName + "_nether")` is missing at this point the chunks database is cleared — i.e., deleting the nether world folder forces full regeneration.
3. `onEnable()` — registers listeners (`PlayerListener`, `NetherChunkMaker`, `TeamListener`, `NetherRedstoneListener`), registers the Warped Compass recipe, and ensures a spawn `Island` exists.
4. `allLoaded()` — persists settings.

`Settings` implements BentoBox's `WorldSettings`, so it doubles as the world configuration object returned from `getWorldSettings()`. It is serialized via `@ConfigEntry` annotations and stored at `addons/StrangerRealms/config.yml`. Adding a new option means adding a field + annotations here, not editing `config.yml` directly (the file is regenerated from the class).

### Domain vocabulary

In user-facing strings and commands the unit of ownership is a **claim**, but in code (and in BentoBox APIs) it is an **`Island`**. They are the same thing — `ClaimCommand` reuses BentoBox's island-create flow and even pulls locale keys from the core "island" namespace. Don't try to rename `Island` to `Claim` in code; treat them as synonyms.

### Upside Down generation (generator/)

The nether is a darkened, distressed mirror of the Overworld, generated lazily:

- `NetherChunks` (ChunkGenerator) lays down the base terrain — a sculk-laced deep-dark floor below `NETHER_FLOOR`. Above that, the chunk is mostly air.
- `NetherBiomeProvider` assigns biomes; `NetherChunkMaker.BIOME_MAPPING` maps overworld biomes → crimson/warped/basalt variants.
- `NetherChunkMaker` (Listener) is the interesting piece: on `ChunkLoadEvent` in the nether world, it copies the corresponding overworld chunk's blocks into the nether chunk (with attrition / corruption controlled by `Settings.attrition`). The set of chunks it has already processed is persisted via `NetherChunksMade` (a BentoBox `DataObject` table named `StrangerChunks`) so generation only happens once per chunk.
- The **Warped Compass** (recipe registered in `StrangerRealms#registerWarpedCompassRecipe`) is consumed during nether portal travel to force re-generation of surrounding chunks — that is, to clear them from `NetherChunksMade` so they get rebuilt from the current overworld state.
- `NetherRedstoneListener` is the "Glimmer": with probability `Settings.redstoneChance`, button/lever interactions in the Upside Down replay at the same coordinates in the overworld.

### Border system (border/)

StrangerRealms ships its own world-border implementation and **conflicts with BentoBox's `Border` addon** — `onEnable()` warns if `Border` is loaded. There are two strategies behind the `BorderShower` interface:

- `ShowBarrier` — custom client-side barrier-block visualisation.
- `ShowWorldBorder` — uses the vanilla world-border packet.

`PerPlayerBorderProxy` is the entry point used by the rest of the addon; it currently fans calls out to *both* strategies (so each player sees both). Per-player border *type* selection (`BorderType` enum) is the intended extension point if you need to vary by player.

The world border size is dynamic: `StrangerRealms#getBorderSize()` returns `barrierIncreaseBlocks × onlinePlayers` unless `manualBorderSize` is set. Shrinks are animated gradually via a Bukkit scheduled task at `barrierReductionSpeed` seconds per block — there is at most one such task; calling `getBorderSize()` again replaces it.

### Resources packaged into the jar (pom.xml `<resources>`)

- `src/main/resources/config.yml`, `addon.yml`, `plugin.yml` — filtered (Maven replaces `${...}` placeholders).
- `src/main/resources/locales/*.yml` → `./locales/` — **not filtered**. ~22 translations live here.
- `src/main/resources/blueprints/*.{blu,json}` → `./blueprints/` — BentoBox blueprint bundle for new claim layouts.
- `src/main/resources/structures/*.nbt` → `./structures/` — directory may not exist yet; the resource entry is preconfigured.

## Testing patterns

Tests use JUnit 5 + Mockito + MockBukkit. Two reusable base classes live in `src/test/java/world/bentobox/stranger/`:

- `CommonTestSetup` — sets up `MockBukkit.mock()`, a static `Bukkit` mock, a mocked `BentoBox` plugin (injected via `WhiteBox` reflection into `BentoBox.instance`), and a mocked `IslandsManager` / `IslandWorldManager` with one default island. Extend this for most listener/command tests.
- `RanksManagerTestSetup` — adds rank-manager setup on top of `CommonTestSetup`.
- `WhiteBox` — utility for setting private static fields (e.g., the BentoBox singleton).
- `TestWorldSettings` — minimal `WorldSettings` returned from `iwm.getWorldSettings()`.

Always call `super.setUp()` / `super.tearDown()` from subclasses — the base class is responsible for closing the static `Bukkit` mock and calling `MockBukkit.unmock()`, and forgetting either causes test interference and leaked database directories.

`StrangerRealmsTest` exercises full addon loading; it builds a temporary `addon.jar` from `src/main/resources/config.yml` and feeds it through BentoBox's addon manager. Use it as the reference pattern when you need to test addon-level wiring rather than a single class.

## Companion addons & integration notes

- **Do not** install BentoBox's `Border` addon alongside this — they fight. `InvSwitcher` is recommended (warned about on enable if missing). Real users typically also run `Warps`.
- Spigot NMS is a required repository (`<repository id="nms-repo">`) — it's used for world regeneration. Don't remove it from `pom.xml`.
