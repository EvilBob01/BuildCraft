# BuildCraft NeoForge 1.21.1 — Porting Roadmap

This document tracks the work needed to complete the BuildCraft port from
**Minecraft 1.12.2 (Forge)** to **Minecraft 1.21.1 (NeoForge 21.1.172)**.

---

## Legend

| Symbol | Meaning |
|--------|---------|
| ✅ | Complete |
| 🔄 | In progress |
| ⏳ | Not yet started |
| ❌ | Blocked / needs investigation |

---

## Phase 1 — Build System & Metadata ✅

The foundational infrastructure needed to compile at all.

- ✅ **Gradle wrapper** — Gradle 4.3 → 8.8
- ✅ **Build plugin** — ForgeGradle 2.x → NeoGradle 7.0.171 (`net.neoforged.gradle.userdev`)
- ✅ **Java version** — Java 8 → Java 21 (toolchain)
- ✅ **settings.gradle** — Add `pluginManagement` block with NeoForge maven
- ✅ **gradle.properties** — NeoForge 21.1.172, MC 1.21.1
- ✅ **neoforge.mods.toml** — Replaces `mcmod.info`; all 8 modules declared
- ✅ **accesstransformer.cfg** — Stub AT file for future use
- ✅ **expression subproject** — Updated to Java 21, removed deprecated Gradle APIs

---

## Phase 2 — Bulk API Migration ✅

Mechanical text-replacement across 1,315 Java files.

- ✅ **Import paths** — `net.minecraftforge.*` → `net.neoforged.neoforge.*` (975 files)
- ✅ **NBT classes** — `NBTTagCompound` → `CompoundTag`, `NBTTagList` → `ListTag`, etc. (676 files)
- ✅ **World/Level** — `World` → `Level`, `WorldServer` → `ServerLevel`, `IBlockAccess` → `BlockGetter`
- ✅ **Block state** — `IBlockState` → `BlockState`, `BlockStateContainer` → `StateDefinition`
- ✅ **Entity names** — `EntityPlayer` → `Player`, `EntityPlayerMP` → `ServerPlayer`, `EntityItem` → `ItemEntity`
- ✅ **Direction** — `EnumFacing` → `Direction`, `EnumHand` → `InteractionHand`
- ✅ **Utility renames** — `AxisAlignedBB` → `AABB`, `Vec3d` → `Vec3`, `MathHelper` → `Mth`
- ✅ **BlockEntity** — `TileEntity` → `BlockEntity`, `TileEntityType` → `BlockEntityType`
- ✅ **Material removed** — `Material.*` → `Block.Properties.of()` (Material was removed in MC 1.20.4)
- ✅ **Method renames** — `writeToNBT` → `saveAdditional`, `isRemote` → `isClientSide`, `markDirty` → `setChanged`, `getTileEntity` → `getBlockEntity`, `setBlockState` → `setBlock`
- ✅ **Annotations** — `@SideOnly` → `@OnlyIn`, `Side.CLIENT` → `Dist.CLIENT`
- ✅ **Text components** — `ITextComponent` → `Component`, `TextFormatting` → `ChatFormatting`
- ✅ **Networking imports** — `SimpleNetworkWrapper` import stubs replaced

---

## Phase 3 — Entry Points & Event System ✅

How the mod bootstraps itself in NeoForge.

- ✅ **BCLib** — Rewritten: constructor takes `IEventBus modEventBus`, subscribes via `modEventBus.addListener()`
- ✅ **BCCore** — Rewritten with NeoForge constructor pattern
- ✅ **BCBuilders** — Rewritten
- ✅ **BCEnergy** — Rewritten
- ✅ **BCFactory** — Rewritten
- ✅ **BCSilicon** — Rewritten
- ✅ **BCTransport** — Rewritten
- ✅ **BCRobotics** — Rewritten
- ✅ **FML lifecycle** — `FMLPreInitializationEvent/FMLInitializationEvent/FMLPostInitializationEvent` → `FMLCommonSetupEvent` / `FMLLoadCompleteEvent`
- ✅ **Server events** — `FMLServerStartingEvent` → `NeoForge.EVENT_BUS.addListener(ServerStartingEvent)`

---

## Phase 4 — Block & Registry System ✅

How blocks, items, and block entities are registered.

- ✅ **BlockBCBase_Neptune** — Rewritten for 1.21.1: `Block.Properties` constructor, `createBlockStateDefinition()`, `BlockPlaceContext`, removed metadata system
- ✅ **RegistrationHelper** — Rewritten to use `RegisterEvent` on the mod bus instead of `RegistryEvent.Register`
- ✅ **BlockEntityType registration** — Moved to `RegisterEvent` with reflection-based instantiation
- ⏳ **BlockBC constructors** — ~40 individual block classes still need constructor signature updates (`Material` arg pattern)
- ⏳ **ItemBCBase** — Item base class needs porting (constructor, `Item.Properties` instead of `Item.ToolMaterial`)
- ⏳ **Block.Properties** — Each block should declare correct strength/sound/mapColor in `Block.Properties.of()`
- ⏳ **Creative tabs** — `CreativeModeTab` builder pattern (old `setCreativeTab()` removed)

---

## Phase 5 — Networking 🔄

BuildCraft has a centralized network layer in `buildcraft.lib.net`.

- ⏳ **MessageManager** — Rewrite `SimpleNetworkWrapper` → NeoForge `SimpleChannel` or `PayloadRegistrar` (1 file is the hub)
- ⏳ **Packet classes (~25 files)** — Each class implementing `IMessage` needs to implement `CustomPacketPayload` instead
  - Key packets: `MessageUpdateTile`, `MessageMarker`, `MessageContainer`, `MessageMultiPipeItem`, `MessageWireSystems`, `MessageVolumeBoxes`, `MessageSnapshotRequest/Response`
- ⏳ **Packet dispatch** — Replace `INSTANCE.sendToServer()` / `sendTo()` / `sendToAllAround()` with `PacketDistributor`

---

## Phase 6 — Capability System 🔄

NeoForge 1.21.1 overhauled the capability API.

- ⏳ **CapabilityHelper** — Central capability helper needs rewriting for new `ICapabilityProvider`
- ⏳ **Block capabilities** — `IBlockCapabilityProvider` replaces `getCapability(Capability, EnumFacing)`
- ⏳ **Item capabilities** — `IItemCapabilityProvider`
- ⏳ **Energy** — `IEnergyStorage` capability attachment (41 files affected total)
- ⏳ **Fluid handler** — `IFluidHandler` capability (156 files)
- ⏳ **Item handler** — `IItemHandler` capability (45 files)
- ⏳ **MjCapabilityHelper** — BuildCraft's custom MJ power system capability bridge

---

## Phase 7 — Rendering 🔄

The rendering system changed fundamentally across multiple MC versions.

- ⏳ **BlockEntityRenderer** — Replace all `TileEntitySpecialRenderer` (TESR) subclasses
- ⏳ **PoseStack** — Replace `GL11` / `GlStateManager` matrix calls with `PoseStack`
- ⏳ **RenderSystem** — Replace `GlStateManager` state calls with `RenderSystem.*`
- ⏳ **Model loading** — Update `ModelLoader` → `ModelEvent.RegisterGeometryLoaders` / `IUnbakedModel`
- ⏳ **Item rendering** — `IItemRenderer` → `ItemRenderer` hooks
- ⏳ **Particle system** — Update particle factories and registration
- ⏳ **GUI rendering** — `GuiScreen` → `Screen`, update all GUI classes
- ⏳ **Client proxy** — Replace `@SideOnly` proxy pattern with `DistExecutor.safeRunWhenOn(Dist.CLIENT, ...)`

---

## Phase 8 — Config, Tags, Fluids 🔄

- ⏳ **Config system** — Port from old `Configuration` / `ConfigCategory` to NeoForge `ModConfigSpec`
- ⏳ **OreDictionary → Tags** — Replace `OreDictionary.registerOre()` calls with tag JSON data files; update recipe ingredients to use `TagIngredient`
- ⏳ **Fluid system** — Port `Fluid` / `FluidRegistry` to NeoForge `FluidType` + `FluidStack`; update `IFluidTank`, `FluidTank` usages
- ⏳ **World generation** — `IWorldGenerator` → `BiomeModifier` / `FeaturesPlacedFeature` (oil springs, biomes)
- ⏳ **Biome system** — `Biome` registration changed; oil biomes need updating
- ⏳ **Chunk loading** — `ForgeChunkManager` removed; replace with `TicketHelper` / `LoadingValidationCallback`

---

## Phase 9 — Compilation & Testing ⏳

- ⏳ **Zero compile errors** — Run `./gradlew compileJava`, fix all remaining errors iteratively
- ⏳ **Basic load test** — Mod loads without crash in a plain MC 1.21.1 + NeoForge instance
- ⏳ **Core feature test** — Quarry, pipes, engines function correctly
- ⏳ **ATM10 compatibility** — Load inside All the Mods 10 modpack, verify no conflicts
- ⏳ **Regression testing** — All pipe types transport items/fluids correctly; gates trigger; robots path-find

---

## Phase 10 — Polish & Release ⏳

- ⏳ **Maven publication** — Publish `buildcraft-api` and `buildcraft-lib` artifacts
- ⏳ **CurseForge / Modrinth release** — Upload jar to mod distribution platforms
- ⏳ **Localization** — Verify all lang files work with 1.21.1 lang format (`.json`)
- ⏳ **Recipe data** — Convert `.json` recipes to 1.21.1 data pack format where needed
- ⏳ **Loot tables** — Update block loot tables to 1.21.1 format
- ⏳ **Documentation** — Update wiki / guide book content

---

## Known Issues & Blockers

| Issue | Severity | Notes |
|-------|----------|-------|
| `BlockEntity` constructors must take `(BlockPos, BlockState)` — many BC tiles still have old signatures | High | Affects all ~60 tile classes |
| Capability system completely redesigned in NeoForge 1.20.x+ | High | 41 files; MJ power especially tricky |
| Fluid API differs significantly (FluidType vs Fluid) | High | 156 files affected |
| Chunk loader (`ForgeChunkManager`) removed with no direct replacement | Medium | Quarry relies on this |
| Robot AI / pathfinding may need significant work with entity system changes | Medium | |
| Guide book rendering uses old GL calls | Low | Can be deferred |

---

## How to Help

The most impactful areas for community contributions right now:

1. **Fix compilation errors** — Run `./gradlew compileJava 2>&1 | tee errors.txt` and pick a class to fix
2. **Port a packet class** — Pick any file in `common/buildcraft/lib/net/` and port `IMessage` → `CustomPacketPayload`
3. **Port a BlockEntity** — Update constructor signature to `(BlockPos pos, BlockState state)` and fix `saveAdditional`/`loadAdditional`
4. **Write tag JSON files** — Replace `OreDictionary` ore names with proper tag files in `buildcraft_resources/data/`

Open a PR against the `8.0.x-1.21.1-neoforge` branch.
