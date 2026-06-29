# BuildCraft — NeoForge 1.21.1 Port

> **This is a community fork** porting BuildCraft to **Minecraft 1.21.1 + NeoForge 21.1.x**,
> targeting compatibility with the **All the Mods 10** modpack.
>
> Original project: [BuildCraft/BuildCraft](https://github.com/BuildCraft/BuildCraft)
> Fork maintainer: [EvilBob01](https://github.com/EvilBob01)

---

## What is BuildCraft?

BuildCraft extends Minecraft with automation, transportation, and construction tools:

- **Quarries** — automated mining machines that dig out large areas
- **Pipes** — item, fluid, and power transport networks
- **Engines** — Redstone, Stone, Iron, and Combustion engines providing Minecraft Joules (MJ)
- **Builders & Fillers** — automated construction and filling of large volumes
- **Robots** — programmable worker bots for complex tasks
- **Gates & Wires** — Redstone-like logic built into pipes
- **Assembly & Integration Tables** — advanced crafting with laser power
- **Tanks, Pumps, Flood Gates** — fluid management

---

## Current Status: Active Port (WIP)

The `8.0.x-1.21.1-neoforge` branch is under active development.
See the full [ROADMAP](ROADMAP.md) and [CHANGELOG](CHANGELOG.md) for details.

| Component | Status |
|-----------|--------|
| Build system (NeoGradle 7, Gradle 8.8, Java 21) | ✅ Complete |
| Mod metadata (`neoforge.mods.toml`) | ✅ Complete |
| Package/class/method bulk renames (~1,315 files) | ✅ Complete |
| `@Mod` entry points (NeoForge event bus) | ✅ Complete |
| Block API (`Block.Properties`, `BlockState`) | ✅ Complete |
| Registry system (`RegisterEvent` on mod bus) | ✅ Complete |
| Networking (`SimpleNetworkWrapper` → `CustomPacketPayload`) | 🔄 In progress |
| Capability system (NeoForge 1.21.1 caps) | 🔄 In progress |
| Rendering (`BlockEntityRenderer`, `RenderSystem`) | 🔄 In progress |
| Config system & Tags (replacing OreDictionary) | 🔄 In progress |
| Compilation clean (zero errors) | ⏳ Pending |
| In-game testing | ⏳ Pending |

---

## Building

### Requirements

- **Java 21** (JDK 21+)
- **Git** with submodule support
- Internet connection for first build (downloads NeoForge and Minecraft)

### Steps

```bash
# 1. Clone the fork
git clone https://github.com/EvilBob01/BuildCraft.git
cd BuildCraft

# 2. Checkout the porting branch
git checkout 8.0.x-1.21.1-neoforge

# 3. Initialize submodules
git submodule update --init

# 4. Build (Linux/macOS)
./gradlew build

# 4. Build (Windows)
gradlew.bat build
```

The output jar will be in `build/libs/`.

### Development setup (IntelliJ / Eclipse)

```bash
# Generate IDE run configurations
./gradlew genIntellijRuns   # IntelliJ IDEA
./gradlew genEclipseRuns    # Eclipse
```

### Directory structure

```
BuildCraft/
├── common/buildcraft/       # Main source — all 8 modules
│   ├── lib/                 # Shared library (BCLib)
│   ├── core/                # Core blocks & items (BCCore)
│   ├── builders/            # Quarry, Filler, Builder
│   ├── energy/              # Engines, oil, fuel
│   ├── factory/             # Pump, Tank, Mining Well
│   ├── silicon/             # Assembly Table, Gates, Facades
│   ├── transport/           # Pipes, Wires
│   └── robotics/            # Robots, Zone Planner
├── BuildCraftAPI/api/       # Public API (submodule)
├── buildcraft_resources/    # Assets, data packs, mod metadata
│   └── META-INF/
│       └── neoforge.mods.toml
├── sub_projects/expression/ # Math expression library
├── build.gradle             # NeoGradle 7 build script
├── settings.gradle          # Multi-project settings
└── gradle.properties        # NeoForge 21.1.172 / MC 1.21.1
```

---

## Modules

| Mod ID | Module | Description |
|--------|--------|-------------|
| `buildcraftlib` | Lib | Shared library, networking, rendering helpers |
| `buildcraftcore` | Core | Engines, markers, wrench, gears |
| `buildcraftbuilders` | Builders | Quarry, Filler, Architect, Builder, Replacer |
| `buildcraftenergy` | Energy | Combustion engine, oil springs, fuel refinery |
| `buildcraftfactory` | Factory | Pump, Tank, Mining Well, Chute, Heat Exchanger |
| `buildcraftsilicon` | Silicon | Assembly Table, Lasers, Gates, Facades |
| `buildcrafttransport` | Transport | Pipes, Pipe Wires, Filtered Buffer |
| `buildcraftrobotics` | Robotics | Robots, Zone Planner |

All modules ship in a single jar. You do not need to install them separately.

---

## Depending on BuildCraft (mod developers)

> Maven releases for 1.21.1 are not yet published. Once the port stabilizes, artifacts will be available.

For the 1.12.2 build (current stable release), add to your `build.gradle`:

```groovy
repositories {
    maven { url = "https://mod-buildcraft.com/maven" }
}
dependencies {
    // API only
    compileOnly "com.mod-buildcraft:buildcraft-api:8.0.1-pre.2"
}
```

---

## Contributing

Pull requests for bug fixes and compatibility improvements are welcome.

- **Bug reports** — open an issue with a crash log or reproduction steps
- **Feature requests** — discuss in Issues before submitting a PR
- **Port contributions** — check the [ROADMAP](ROADMAP.md) for areas needing work; all help is appreciated
- **Formatting-only PRs** — please avoid; they clutter history without value

If you'd like to help with the 1.21.1 port, the highest-priority areas are listed in [ROADMAP.md](ROADMAP.md).

---

## License

BuildCraft is licensed under the [Mozilla Public License 2.0](LICENSE).
The BuildCraft API submodule is licensed under the [MIT License](BuildCraftAPI/LICENSE).
