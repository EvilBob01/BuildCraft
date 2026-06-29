# Changelog

All notable changes to this BuildCraft fork are documented here.

Format: `[Version] — Date — Description`

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
