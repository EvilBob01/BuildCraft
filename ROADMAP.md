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
- ✅ **Gradle wrapper (2nd pass)** — 8.8 → 8.13 (NeoGradle 7.0.171 requires Gradle 8.10+)
- ✅ **runs {} block ordering** — dependencies must be declared before `runs {}` or NeoGradle can't resolve `client()`/`server()` run types
- ✅ **datagen run** — `data()`/`type=` run-type API in NeoGradle 7.0.171 didn't match docs; run removed for now (not needed for `compileJava`)
- ✅ **expression subproject duplicate resources** — removed a redundant `resources { srcDir 'src/generator/resources' }` that duplicated Gradle's own sourceSet convention and broke `processGeneratorResources`
- ✅ **`./gradlew compileJava` now reaches real Java compilation** — first successful run through NeoForge dependency resolution, MC decompile, and `javac` invocation

### First real compile: what we learned

Getting `compileJava` to actually invoke `javac` (rather than fail during Gradle configuration) was itself a milestone — it took 5 iterations to fix the build script. Once it did, two classes of bugs surfaced:

1. **UTF-8 BOM corruption (self-inflicted, now fixed).** The PowerShell bulk-edit scripts used during the initial porting pass wrote files with `[System.Text.Encoding]::UTF8`, which in .NET prepends a byte-order-mark. `javac` treats a BOM as an illegal character, which cascaded into "class expected" errors for the *entire rest of the file* — inflating the apparent error count into the thousands. Stripped from all 968 affected files; this alone took the error count from 2,000+ (capped) down to 82.
2. **Broken "commented-out removed method" artifacts.** Some of the earlier regex replacements (e.g. `.setRegistryName(...)` → a comment) used a `[^)]+` capture that stopped at the first `)`, leaving dangling `))` or `;` fragments in 10 files. All fixed by hand, mostly by stubbing now-impossible 1.12.2-era recipe registration (`ShapedOreRecipe`, `ForgeRegistries.RECIPES`, `OreDictionary`) with `// TODO (Phase 8)` markers rather than guessing at a JSON-recipe rewrite.

With those fixed, `-Xmaxerrs 100000` surfaced the **real** error count: **~21,800 errors**. Unlike the BOM noise, these are genuine — the codebase leans on entire 1.12.2-era subsystems that no longer exist at all in 1.21.1, not just renamed classes. Frequency breakdown of unresolved imports (see `compile9.log` for full detail):

| Broken import area | Approx. errors | Real cause |
|---|---|---|
| `net.minecraftforge.fml.common.network.simpleimpl.*` | 148+16+58 | Old networking (`IMessage`, `MessageContext`) — Phase 5 |
| `net.minecraft.client.gui.*` / `GuiScreen` / `GuiButton` | 100+ | Old `Screen`/`Button` API — Phase 7 |
| `net.minecraft.client.renderer.{GlStateManager,Tessellator,vertex.*}` | 150+ | Immediate-mode GL rendering, replaced by `PoseStack`/`BufferBuilder` — Phase 7 |
| `net.minecraft.inventory.{Container,IInventory,InventoryCrafting}` | 94+20 | Old container system, replaced by `AbstractContainerMenu` — not yet in roadmap, added below |
| `net.minecraft.block.Block*` (BlockDoor, BlockStairs, BlockChest, ...) | 98 | 1.12.2 required subclassing vanilla blocks per-type; modern MC constructs them directly — mostly dead imports to delete |
| `net.minecraftforge.common.util.Constants`(`.NBT`) | 82+48 | NBT tag-type byte constants moved/removed — mechanical, low-risk fix |
| `net.minecraftforge.common.config.*` | 34+9+5 | Old `Configuration`/`Property` — Phase 8 |
| `net.minecraft.entity.*` stragglers | 56 | A few classes my bulk pass missed (`EntityHanging`, `EntityList`, minecart entities) |
| `javax.vecmath.*`, `gnu.trove.*` | 118+84 | **Fixed for free** — these are plain Java libraries with no MC coupling; re-added as Maven dependencies (`javax.vecmath:vecmath:1.5.2`, `net.sf.trove4j:trove4j:3.0.3`) instead of rewriting every call site |
| `EnumDyeColor`, `BiomeDictionary`, misc single-class stragglers | <40 each | Simple renames my first bulk pass missed (`EnumDyeColor` → `DyeColor` moved package, etc.) |

**Takeaway:** the remaining work is exactly what Phases 4–10 below already describe — it is not new scope, but now has real numbers behind it instead of estimates. The GUI/rendering and container-menu rewrites are the single biggest chunks (250+ and 114+ errors respectively) and will need dedicated sessions, not bulk regex.

**Update (same day, later session):** Phase 5's core networking transport is now done and verified — see the Phase 5 section below. Along the way, several more mechanical stragglers were found and bulk-fixed with the same "verify via real compile" approach:
- `EnumDyeColor` → `DyeColor` (moved package in 1.13+), 77 files
- `Minecraft.getMinecraft()` → `Minecraft.getInstance()`, `.world` → `.level`, 75 files
- `net.minecraftforge.common.util.Constants.NBT.TAG_*` → `net.minecraft.nbt.Tag.TAG_*` (vanilla already defines the same tag-type byte constants, no new class needed), 27 files
- Remaining `IMessage`/`IMessageHandler`/`MessageContext` import stragglers across ~40 more files that reference these types without implementing `IMessage` themselves

Running error count: ~21,800 → ~19,404 (per `compileJava -Xmaxerrs 100000`). Each additional fix now yields smaller returns because most remaining files stack multiple *separate* legacy-API problems (e.g. a container class might need both the container/menu rewrite *and* a capability rewrite *and* a rendering fix before it compiles clean) — so a fix to one subsystem partially but doesn't fully unblock files that also depend on another unfinished subsystem. The next highest-leverage moves are the container/menu rewrite (Phase 6.5) and the capability rewrite (Phase 6), since those block the largest number of otherwise-close-to-compiling files.

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

## Phase 5 — Networking ✅ (core), 🔄 (call sites)

BuildCraft has a centralized network layer in `buildcraft.lib.net`.

- ✅ **MessageManager** — Rewritten on top of NeoForge's real `RegisterPayloadHandlersEvent` / `PayloadRegistrar` / `IPayloadContext` / `PacketDistributor`. Verified against the real NeoForge 21.1.172 jar via `compileJava` with zero errors.
- ✅ **Adapter interfaces** — Added local `buildcraft.lib.net.IMessage` / `IMessageHandler` / `MessageContext`, API-compatible with the removed Forge `simpleimpl` classes, so the 15 existing message classes kept their `toBytes`/`fromBytes` bodies unchanged and only needed import fixes
- ✅ **BCPayload** — A single `CustomPacketPayload` record (message-class-id + raw bytes) carries every legacy `IMessage`, avoiding a per-class `CustomPacketPayload` rewrite
- ✅ **Packet dispatch** — `sendToAll`/`sendTo`/`sendToServer` rewritten against `PacketDistributor`
- ✅ **12 of 15 message classes** compile cleanly; the other 3 (`MessageUtil`, `MessageDebugRequest/Response`, `MessageZoneMapRequest`, `MessageWireSystemsPowered`) had their own separate legacy breakage (`GameProfile.isComplete()` removed upstream, `writeUniqueId`→`writeUUID`, `Minecraft.getMinecraft()`→`getInstance()`) — all fixed
- ⏳ **sendToDimension** — Stubbed; dimensions are `ResourceKey<Level>` now, not `int`, and its only two callers (`WorldSavedDataVolumeBoxes`, `MarkerSubCache`) still reference the separately-broken `Level.provider` field
- 🔄 **MessageContainer's dispatch body** — Stubbed with a Phase 6.5 TODO; it dispatches into `ContainerBC_Neptune`, which still extends the removed 1.12.2 `Container` class
- 🔄 **~40 more call-site files** (tile entities overriding `receivePayload`, container classes, `PipeBehaviour`/`PipePluggable` API) had their `IMessage`/`MessageContext` imports fixed to point at the new local classes, but many still have unrelated errors from other subsystems (containers, capabilities) that block them from fully compiling

---

## Phase 6 — Capability System 🔄 (core ✅ compiler-verified, leaf call sites ⏳)

> ### ✅ The API/common split is RESOLVED (2026-07-30, verified by `./gradlew compileJava`)
>
> The capability *declaration* layer is now coherent across both `common/` and `BuildCraftAPI/`,
> and every file in it compiles with **zero errors**:
>
> | File | Errors before → after |
> |---|---|
> | `BuildCraftAPI/api/buildcraft/api/core/CapabilitiesHelper.java` | 56 → **0** |
> | `BuildCraftAPI/api/buildcraft/api/mj/MjCapabilityHelper.java` | 16 → **0** |
> | `BuildCraftAPI/api/buildcraft/api/mj/MjAPI.java` | 12 → **0** |
> | `BuildCraftAPI/api/buildcraft/api/tiles/TilesAPI.java` | 10 → **0** |
> | `BuildCraftAPI/api/buildcraft/api/transport/pipe/PipeApi.java` | 10 → **0** |
> | `common/buildcraft/lib/misc/CapUtil.java` | already 0 |
> | `common/buildcraft/lib/cap/CapabilityHelper.java` | already 0 |
>
> **The key change:** `CapabilitiesHelper` was the linchpin, not `MjAPI`. It was shared by `MjAPI`
> (5 caps), `TilesAPI` (4) and `PipeApi` (4), and was a Forge-specific reflection hack that read
> `CapabilityManager`'s private `providers` map. It now simply creates `BlockCapability` values via
> `BlockCapability.createSided(...)`, deriving a stable `ResourceLocation` from the class name
> (`IMjConnector` → `buildcraftapi:mj_connector`). Its `registerCapability(Class<T>)` signature was
> deliberately kept, so all 13 call sites needed no edits — only field types changed from
> `Capability<T>` to `BlockCapability<T, Direction>`.
>
> ### ✅ Leaf call sites also DONE — capability compile errors are now ZERO
>
> Follow-up passes cleared every remaining capability error: **18,858 → 18,502 total (−356), with zero
> files regressing at any step.** Grepping the compile log for `getCapability|hasCapability|CAP_|
> BlockCapability|ICapabilityProvider` now returns **0 errors**.
>
> Three things made this work:
> 1. **`CapUtil.getCapability(BlockEntity, cap, side)`** — `PipeExtensionManager` was already calling a
>    BlockEntity-first overload that didn't exist (the deleted agent's intent). Adding it turned the
>    hardest pattern into a one-line swap, since `BlockEntity` has no `getCapability` under NeoForge.
> 2. **Dropping `implements ICapabilityProvider`** from `IPipe`, `PipeBehaviour`, `PipeFlow`. NeoForge
>    kept the *name* but it is now a generic `ICapabilityProvider<O,C,T>` describing a registration-time
>    *factory*, not a poll-able interface — so ~22 concrete pipe classes were failing to implement an
>    abstract method that no longer meant what it used to.
> 3. **New `buildcraft.api.core.ICapabilityAccessor`** — BuildCraft genuinely needed the "composable
>    capability holder" role that NeoForge's interface no longer fills. Implemented by
>    `CapabilityHelper`, `MjCapabilityHelper`, `ItemHandlerManager` and `TileBC_Neptune`, so
>    `CapabilityHelper#addProvider` still composes them.
>
> ### ⏳ Genuinely remaining for Phase 6
>
> **Nothing registers any of this with the game yet.** It compiles, but a `RegisterCapabilitiesEvent`
> listener on the mod bus is still required before capabilities work at runtime — registering each
> block entity type against `CapUtil.CAP_ITEMS` / `CAP_ITEM_TRANSACTOR` / the MJ caps, delegating to the
> `getCapability(cap, side)` methods now present on those holders. Item caps likewise: see the TODO in
> `ItemFragileFluidContainer` for the exact `event.registerItem(...)` call needed.
>
> <details><summary>Historic: the original leaf-call-site list (now cleared)</summary>
>
> Fixing the declaration layer *surfaced* 52 previously-hidden errors in capability consumers —
> this is progress, not regression: javac previously could not resolve the capability types at all,
> so it failed earlier and reported less. Those files now produce actionable errors. Affected
> (errors surfaced): `PipeFlowPower` (10), `PipeExtensionManager` (8), `PipeBehaviourStripes` (6),
> `TriggerPower` (4), `StripesHandlerPipeWires` (4), then `Pipe`, `TileEngineBase_BC8`,
> `CoreActionProvider`, `PipeBehaviourWoodPower`, `TriggerMachine`, `PipeFlowItems`,
> `TilePipeHolder`, `CoreTriggerProvider`, `ItemTransactorHelper`, `ActionMachineControl` (2 each).
>
> Most need the same conversion: an old `provider.getCapability(cap, side)` call becomes a
> `level.getCapability(cap, pos, side)` query (see the helper at the bottom of `CapUtil.java`).
> </details>
>
> Still outstanding separately: `IPipeHolder`, `PipeBehaviour`, `PipeFlow` and `PipePluggable` in
> the API still declare method signatures taking the old `Capability<T>`; these need the same
> `BlockCapability<T, Direction>` swap.
>
> Also still true: **nothing registers these capabilities against block-entity types yet.** A
> `RegisterCapabilitiesEvent` listener on the mod bus is still required before any of this works at
> runtime, even once it all compiles.

NeoForge 1.21.1 overhauled the capability API.

- ⏳ **CapabilityHelper** — Central capability helper needs rewriting for new `ICapabilityProvider`
- ⏳ **Block capabilities** — `IBlockCapabilityProvider` replaces `getCapability(Capability, EnumFacing)`
- ⏳ **Item capabilities** — `IItemCapabilityProvider`
- ⏳ **Energy** — `IEnergyStorage` capability attachment (41 files affected total)
- ⏳ **Fluid handler** — `IFluidHandler` capability (156 files)
- ⏳ **Item handler** — `IItemHandler` capability (45 files)
- ⏳ **MjCapabilityHelper** — BuildCraft's custom MJ power system capability bridge

---

## Phase 6.5 — Containers / Menus (GUI backend) ⏳

Discovered via the Phase 1 compile census: 1.12.2's `Container`/`IInventory`/`InventoryCrafting`/`Slot`
system was fully replaced by `AbstractContainerMenu` in modern MC — this is architecturally distinct
from the client-side rendering rewrite in Phase 7, so it gets its own phase.

- ⏳ **Container → AbstractContainerMenu** — every `Container*` class in `buildcraft/*/container/` (Architect Table, Builder, Filler, Assembly Table, Gate, pipes, etc.)
- ⏳ **IInventory / InventoryBasic → Container / SimpleContainer** — internal inventory-holding classes
- ⏳ **Slot** — largely compatible but constructor signatures changed; audit each subclass
- ⏳ **MenuType registration** — containers must be registered via `DeferredRegister<MenuType<?>>` with a `MenuSupplier`
- ⏳ **Networking for menu data** — `ContainerData` / `DataSlot` replaces manual `detectAndSendChanges` sync patterns in some cases

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
