# Changelog

All notable changes to this BuildCraft fork are documented here.

Format: `[Version] — Date — Description`

---

## [8.0.1-1.21.1] — 2026-06-30 — First Successful `compileJava` Invocation (WIP)

This session got `./gradlew compileJava` past Gradle configuration and into real `javac` compilation for
the first time, then used the resulting error census to correct course.

### Build script fixes (5 iterations to get `javac` to actually run)

- Gradle wrapper 8.8 → **8.13**: NeoGradle 7.0.171 requires Gradle 8.10+ (its plugin variant declares `org.gradle.plugin.api-version = 8.10`)
- Reordered `build.gradle` so `dependencies { implementation "net.neoforged:neoforge:..." }` is declared **before** the `runs {}` block — NeoGradle can't resolve `client()`/`server()` run-type methods without the NeoForge dependency already configured
- Removed the `data` run block — NeoGradle 7.0.171's datagen run-type API (`data()` method, `type =` property) didn't match either attempted form; not required for `compileJava`, revisit later
- Fixed `sub_projects/expression/build.gradle`: an explicit `resources { srcDir 'src/generator/resources' }` duplicated Gradle's own sourceSet convention for a custom `generator` sourceSet, causing `processGeneratorResources` to fail with "duplicate entry, no duplicates strategy set"
- Raised `-Xmaxerrs` from 2000 to 100000 so the true error count could be measured in one pass instead of guessing from a truncated list

### Root-caused and fixed: UTF-8 BOM corruption

The PowerShell bulk-edit scripts used in the initial porting pass wrote files via
`[System.Text.Encoding]::UTF8`, which in .NET prepends a byte-order-mark (`﻿`) to every file it
touches. `javac` treats a leading BOM as an illegal character, and because it appears before the
`package` declaration, it cascades into "class, interface, enum, or record expected" errors for
**every line in the file** — this alone was responsible for inflating the error count past the
2000-error cap and made the real problems impossible to see.

- Stripped BOM from all 968 affected `.java` files across `common/`, `BuildCraftAPI/api/`, and `sub_projects/expression/`
- This single fix dropped the error count from 2,000+ (capped, effectively unbounded) to 82

### Fixed broken "removed method" comment-out artifacts (10 files)

Several earlier bulk regex replacements (e.g. `\.setRegistryName\(([^)]+)\)` → a comment) used a
`[^)]+` capture group that stops at the first `)`, so any call with nested parentheses left dangling
`))` or trailing `;` fragments — genuine syntax errors, not cosmetic. Fixed by hand in:
`BCCoreRecipes.java`, `BCEnergyFluids.java`, `FluidManager.java`, `IItemBuildCraft.java`,
`RecipeBuilderShaped.java`, `BCSiliconRecipes.java`, `FacadeAssemblyRecipes.java`, `BCTransportRecipes.java`,
`ListMatchHandlerOreDictionary.java`, `StackUtil.java`, `ChangingItemStack.java`.

Most of these turned out to depend on 1.12.2-only recipe registration (`ShapedOreRecipe`,
`ShapelessOreRecipe`, `ForgeRegistries.RECIPES`, `OreDictionary`) that has no direct 1.21.1 equivalent —
recipes are now data-driven JSON. Rather than guess at a JSON rewrite mid-syntax-fix, these call sites
are now no-ops with `TODO (Phase 8 — see ROADMAP.md)` markers, preserving compileable method signatures
for their callers.

- **`AssemblyRecipe`** (`BuildCraftAPI/api/buildcraft/api/recipes/AssemblyRecipe.java`): removed
  `implements IForgeRegistryEntry<AssemblyRecipe>` (Forge-only interface, gone in NeoForge) — it was
  really just a self-contained object with its own `name` field, so `setRegistryName`/`getRegistryName`
  are now plain (non-`@Override`) methods and `getRegistryType()` was dropped
- **`AssemblyRecipeRegistry.register()`**: fixed to call `recipe.getRegistryName()` instead of a bogus
  `recipe.builtInRegistryHolder().key().location()` call left by an over-broad earlier regex pass
  (`AssemblyRecipe` was never a real Minecraft registry object, so that accessor never existed on it)
- Deleted `BCRecipeShaped.java` / `BCRecipeShapeless.java` (dead code — extended the now-nonexistent
  `ShapedOreRecipe`/`ShapelessOreRecipe`, and nothing else in the codebase referenced them)

### Added back missing plain-Java dependencies (no code changes needed)

The compile census showed ~200 errors from `javax.vecmath.*` and `gnu.trove.*` — these are general-purpose
Java libraries with zero Minecraft/Forge coupling that simply weren't on the classpath anymore, not APIs
that were removed. Re-added as Gradle dependencies instead of rewriting ~150+ call sites:

```groovy
implementation "net.sf.trove4j:trove4j:3.0.3"
implementation "javax.vecmath:vecmath:1.5.2"
```

### Real error census (post-fixes): ~21,800 errors

With BOM and syntax issues cleared, `-Xmaxerrs 100000` surfaced the actual remaining error count. See
`ROADMAP.md` → "First real compile: what we learned" for the full frequency breakdown. Summary: the
remaining work is concentrated in the GUI/rendering subsystem (`GuiScreen`, `GlStateManager`,
`Tessellator`, vertex format classes — Phase 7), the container/menu subsystem (`Container`, `IInventory`,
`InventoryCrafting` — new Phase 6.5), old networking (Phase 5), and old config/capability systems
(Phases 6, 8). This is not new scope — it's the same work already outlined in the roadmap, now backed by
real numbers instead of estimates.

---

## [8.0.1-1.21.1] — 2026-06-29 — Initial NeoForge 1.21.1 Port (WIP)

This is the first commit of the NeoForge 1.21.1 community port, branched from
`8.0.x-1.12.2` (the most recent working upstream code; the `8.0.x-1.20.1` branch
upstream was an empty placeholder with no real porting work).

### Build System

- **Gradle**: 4.3.1 → 8.8
- **Java**: 8 → 21 (Gradle toolchain)
- **Build plugin**: ForgeGradle 2.3-SNAPSHOT → `net.neoforged.gradle.userdev:7.0.171`
- **Target**: NeoForge 21.1.172 for Minecraft 1.21.1
- Added `settings.gradle` `pluginManagement` block pointing at NeoForge maven (`https://maven.neoforged.net/releases`)
- Added `gradle.properties` with `neo_version`, `mc_version`, `mod_version`
- Updated `gradle/wrapper/gradle-wrapper.properties` (Gradle 8.8-bin)
- Updated `sub_projects/expression/build.gradle` to modern Gradle (plugins block, `java-library`, removed deprecated `compile`/`testCompile` configs)

### Mod Metadata

- **Added** `buildcraft_resources/META-INF/neoforge.mods.toml` — replaces `mcmod.info`
  - Declares all 8 mod IDs: `buildcraftlib`, `buildcraftcore`, `buildcraftbuilders`, `buildcraftenergy`, `buildcraftfactory`, `buildcraftsilicon`, `buildcrafttransport`, `buildcraftrobotics`
  - Correct inter-mod dependency ordering
- **Added** `buildcraft_resources/META-INF/accesstransformer.cfg` — stub for future AT use

### Package & Import Migration (975 files modified)

All `net.minecraftforge.*` imports replaced with `net.neoforged.*` equivalents:

| Old | New |
|-----|-----|
| `net.minecraftforge.fml.common.Mod` | `net.neoforged.fml.common.Mod` |
| `net.minecraftforge.fml.relauncher.SideOnly` | `net.neoforged.api.distmarker.OnlyIn` |
| `net.minecraftforge.fml.relauncher.Side` | `net.neoforged.api.distmarker.Dist` |
| `net.minecraftforge.common.MinecraftForge` | `net.neoforged.neoforge.common.NeoForge` |
| `net.minecraftforge.fml.common.eventhandler.SubscribeEvent` | `net.neoforged.bus.api.SubscribeEvent` |
| `net.minecraftforge.energy.IEnergyStorage` | `net.neoforged.neoforge.energy.IEnergyStorage` |
| `net.minecraftforge.fluids.FluidStack` | `net.neoforged.neoforge.fluids.FluidStack` |
| `net.minecraftforge.items.IItemHandler` | `net.neoforged.neoforge.items.IItemHandler` |

All `net.minecraft.*` imports updated:

| Old | New |
|-----|-----|
| `net.minecraft.tileentity.TileEntity` | `net.minecraft.world.level.block.entity.BlockEntity` |
| `net.minecraft.nbt.NBTTagCompound` | `net.minecraft.nbt.CompoundTag` |
| `net.minecraft.world.World` | `net.minecraft.world.level.Level` |
| `net.minecraft.world.WorldServer` | `net.minecraft.server.level.ServerLevel` |
| `net.minecraft.block.state.IBlockState` | `net.minecraft.world.level.block.state.BlockState` |
| `net.minecraft.util.EnumFacing` | `net.minecraft.core.Direction` |
| `net.minecraft.util.math.BlockPos` | `net.minecraft.core.BlockPos` |
| `net.minecraft.util.math.AxisAlignedBB` | `net.minecraft.world.phys.AABB` |
| `net.minecraft.util.math.Vec3d` | `net.minecraft.world.phys.Vec3` |
| `net.minecraft.util.math.MathHelper` | `net.minecraft.util.Mth` |
| `net.minecraft.util.ResourceLocation` | `net.minecraft.resources.ResourceLocation` |
| `net.minecraft.util.text.ITextComponent` | `net.minecraft.network.chat.Component` |
| `net.minecraft.entity.player.EntityPlayer` | `net.minecraft.world.entity.player.Player` |
| `net.minecraft.entity.player.EntityPlayerMP` | `net.minecraft.server.level.ServerPlayer` |
| `net.minecraft.init.Blocks` | `net.minecraft.world.level.block.Blocks` |
| `net.minecraft.init.Items` | `net.minecraft.world.item.Items` |
| `net.minecraft.network.PacketBuffer` | `net.minecraft.network.FriendlyByteBuf` |
| `net.minecraft.creativetab.CreativeTabs` | `net.minecraft.world.item.CreativeModeTab` |

### Class & Method Renames (676 + 300 files)

**Class renames:**

| Old | New |
|-----|-----|
| `TileEntity` | `BlockEntity` |
| `TileEntityType` | `BlockEntityType` |
| `NBTTagCompound` | `CompoundTag` |
| `NBTTagList` | `ListTag` |
| `NBTTagByte/Int/Long/Short/Float/Double/String` | `ByteTag/IntTag/LongTag/ShortTag/FloatTag/DoubleTag/StringTag` |
| `NBTBase` | `Tag` |
| `IBlockState` | `BlockState` |
| `EnumFacing` | `Direction` |
| `EnumHand` | `InteractionHand` |
| `EnumActionResult` | `InteractionResult` |
| `EntityPlayer` | `Player` |
| `EntityPlayerMP` | `ServerPlayer` |
| `AxisAlignedBB` | `AABB` |
| `Vec3d` | `Vec3` |
| `MathHelper` | `Mth` |
| `ITextComponent` | `Component` |
| `TextFormatting` | `ChatFormatting` |
| `SideOnly` (annotation) | `OnlyIn` |
| `ItemBlock` | `BlockItem` |
| `WorldServer` | `ServerLevel` |
| `EntityLivingBase` | `LivingEntity` |

**Method renames:**

| Old | New |
|-----|-----|
| `writeToNBT(CompoundTag)` | `saveAdditional(CompoundTag, ...)` |
| `readFromNBT(CompoundTag)` | `loadAdditional(CompoundTag, ...)` |
| `.markDirty()` | `.setChanged()` |
| `world.getTileEntity(pos)` | `level.getBlockEntity(pos)` |
| `world.setBlockState(pos, state)` | `level.setBlock(pos, state, flags)` |
| `world.isRemote` | `level.isClientSide` |
| `Direction.getFront(int)` | `Direction.from3DDataValue(int)` |
| `Loader.isModLoaded()` | `ModList.get().isLoaded()` |

### Material Removal (80 files)

`net.minecraft.block.material.Material` was removed in Minecraft 1.20.4.

- All `Material.IRON`, `Material.CIRCUITS`, `Material.ROCK`, etc. replaced with `Block.Properties.of()`
- Old Block setter methods stubbed out: `setHardness()`, `setResistance()`, `setSoundType()`, `setUnlocalizedName()`, `setRegistryName()`, `setCreativeTab()`

### @Mod Entry Points Rewritten (8 classes)

All module main classes rewritten for NeoForge's constructor-injection pattern:

```java
// Old (1.12.2 Forge)
@Mod(modid = "buildcraftlib", name = "BuildCraft Lib", version = "...", ...)
public class BCLib {
    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent evt) { ... }
}

// New (NeoForge 1.21.1)
@Mod("buildcraftlib")
public class BCLib {
    public BCLib(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.addListener(this::serverStarting);
    }
    private void commonSetup(FMLCommonSetupEvent event) { ... }
}
```

### Block API

- **`BlockBCBase_Neptune`** fully rewritten:
  - Constructor: `(Material, String)` → `(BlockBehaviour.Properties, String)`
  - `createBlockState()` → `createBlockStateDefinition(StateDefinition.Builder<Block, BlockState>)`
  - `getMetaFromState()` / `getStateFromMeta()` removed (metadata system gone since 1.13)
  - `getStateForPlacement(World, BlockPos, ...)` → `getStateForPlacement(BlockPlaceContext)`
  - `withRotation()` / `withMirror()` updated to use `state.setValue()` instead of `state.withProperty()`

### Registry System

- **`RegistrationHelper`** rewritten to use `RegisterEvent` on the mod event bus:
  - Blocks registered via `event.register(Registries.BLOCK, helper -> { ... })`
  - Items registered via `event.register(Registries.ITEM, helper -> { ... })`
  - `BlockEntityType` registered via `event.register(Registries.BLOCK_ENTITY_TYPE, helper -> { ... })`
  - Removed `GameRegistry.registerTileEntity()` (which no longer exists)
  - Registry names resolved from `TagManager` at registration time (no `setRegistryName()` on objects)

---

## Upstream History (original BuildCraft 8.0.x-1.12.2)

For the complete changelog of the original BuildCraft project on 1.12.2, see the
upstream repository at [BuildCraft/BuildCraft](https://github.com/BuildCraft/BuildCraft)
or the `8.0.x-1.12.2` branch in this fork.

Notable upstream milestone: `8.0.1-pre.2` — the last pre-release before the project
went dormant. This port picks up from that point.
