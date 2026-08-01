package buildcraft.lib.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import buildcraft.api.core.ICapabilityAccessor;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.tiles.TilesAPI;
import buildcraft.api.transport.pipe.PipeApi;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.item.IItemBuildCraft;
import buildcraft.lib.item.ItemBlockBC_Neptune;
import buildcraft.lib.registry.TagManager.EnumTagType;
import buildcraft.lib.registry.TagManager.EnumTagTypeMulti;

/** Handles registration of blocks, items, and block entities for all BuildCraft modules. */
public final class RegistrationHelper {

    private final List<Block> blocks = new ArrayList<>();
    private final List<Item> items = new ArrayList<>();
    private final List<BlockEntityEntry<?>> blockEntities = new ArrayList<>();

    private static IEventBus MOD_BUS;

    public static void init(IEventBus modEventBus) {
        MOD_BUS = modEventBus;
    }

    public RegistrationHelper() {
        if (MOD_BUS != null) {
            MOD_BUS.addListener(this::onRegisterBlocks);
            MOD_BUS.addListener(this::onRegisterItems);
            MOD_BUS.addListener(this::onRegisterBlockEntities);
            MOD_BUS.addListener(this::onRegisterCapabilities);
        }
    }

    public static void registerTagEntries() {
        // Tags replace OreDictionary; handled via data packs
    }

    private void onRegisterBlocks(RegisterEvent event) {
        event.register(Registries.BLOCK, helper -> {
            for (Block block : blocks) {
                if (block instanceof BlockBCBase_Neptune bcBlock && !bcBlock.id.isEmpty()) {
                    String regName = TagManager.get(bcBlock.id, EnumTagType.REGISTRY_NAME);
                    helper.register(ResourceLocation.parse(regName), block);
                }
            }
        });
    }

    private void onRegisterItems(RegisterEvent event) {
        event.register(Registries.ITEM, helper -> {
            for (Item item : items) {
                if (item instanceof IItemBuildCraft bcItem && !bcItem.id().isEmpty()) {
                    String regName = TagManager.get(bcItem.id(), EnumTagType.REGISTRY_NAME);
                    helper.register(ResourceLocation.parse(regName), item);
                }
            }
        });
    }

    private void onRegisterBlockEntities(RegisterEvent event) {
        event.register(Registries.BLOCK_ENTITY_TYPE, helper -> {
            for (BlockEntityEntry<?> entry : blockEntities) {
                helper.register(entry.registryName, buildAndRetain(entry));
            }
        });
    }

    /** Builds the type and keeps a reference, so {@link #onRegisterCapabilities} can attach capabilities to it. */
    private static <T extends BlockEntity> BlockEntityType<T> buildAndRetain(BlockEntityEntry<T> entry) {
        BlockEntityType<T> type = entry.buildType();
        entry.type = type;
        return type;
    }

    /** Every {@link BlockCapability} BuildCraft exposes on blocks.
     * <p>
     * These are attached generically to every BuildCraft block entity rather than listed per-tile, because
     * {@code TileBC_Neptune} implements {@link ICapabilityAccessor} and delegates to the {@code CapabilityHelper}
     * that each tile populates in its own constructor. A tile that never registered a given capability simply
     * returns null for it, which is exactly what NeoForge expects — so a blanket registration is correct here and
     * saves maintaining a parallel list that would silently drift out of date. */
    private static final List<BlockCapability<?, Direction>> BC_BLOCK_CAPABILITIES = List.of(
        CapUtil.CAP_ITEMS,
        CapUtil.CAP_FLUIDS,
        CapUtil.CAP_ITEM_TRANSACTOR,
        Capabilities.EnergyStorage.BLOCK,
        MjAPI.CAP_CONNECTOR,
        MjAPI.CAP_RECEIVER,
        MjAPI.CAP_REDSTONE_RECEIVER,
        MjAPI.CAP_READABLE,
        MjAPI.CAP_PASSIVE_PROVIDER,
        TilesAPI.CAP_CONTROLLABLE,
        TilesAPI.CAP_HAS_WORK,
        TilesAPI.CAP_HEATABLE,
        TilesAPI.CAP_TILE_AREA_PROVIDER,
        PipeApi.CAP_PIPE,
        PipeApi.CAP_PLUG,
        PipeApi.CAP_PIPE_HOLDER,
        PipeApi.CAP_INJECTABLE
    );

    /** Attaches BuildCraft's capabilities to every block entity type this helper registered.
     * <p>
     * This is the piece that makes capabilities actually work at runtime: NeoForge no longer polls the block entity,
     * so without this listener every {@code level.getCapability(...)} would return null no matter what the tile
     * exposes internally. */
    private void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        for (BlockEntityEntry<?> entry : blockEntities) {
            if (entry.type == null || !ICapabilityAccessor.class.isAssignableFrom(entry.clazz)) {
                // Not registered (disabled by config), or a tile that doesn't expose capabilities at all.
                continue;
            }
            for (BlockCapability<?, Direction> cap : BC_BLOCK_CAPABILITIES) {
                registerAccessorCapability(event, cap, entry.type);
            }
        }
    }

    /** Separate generic method so the {@code BlockCapability<?, Direction>} wildcard is captured into a concrete
     * type variable, letting the provider lambda typecheck. */
    private static <C, BE extends BlockEntity> void registerAccessorCapability(
        RegisterCapabilitiesEvent event, BlockCapability<C, Direction> capability, BlockEntityType<BE> type
    ) {
        event.registerBlockEntity(capability, type, (blockEntity, side) -> {
            if (blockEntity instanceof ICapabilityAccessor accessor) {
                return accessor.getCapability(capability, side);
            }
            return null;
        });
    }

    @Nullable
    public <I extends Item> I addItem(I item) { return addItem(item, false); }

    @Nullable
    public <I extends Item> I addItem(I item, boolean force) {
        if (force || RegistryConfig.isEnabled(item)) return addForcedItem(item);
        return null;
    }

    public <I extends Item> I addForcedItem(I item) {
        items.add(item);
        if (item instanceof IItemBuildCraft bcItem && !bcItem.id().isEmpty()) {
            String[] old = TagManager.getMultiTag(bcItem.id(), EnumTagTypeMulti.OLD_REGISTRY_NAME);
            MigrationManager.INSTANCE.addItemMigration(item, old);
        }
        return item;
    }

    @Nullable
    public <B extends Block> B addBlock(B block) { return addBlock(block, false); }

    @Nullable
    public <B extends Block> B addBlock(B block, boolean force) {
        if (force || RegistryConfig.isEnabled(block)) return addForcedBlock(block);
        return null;
    }

    public <B extends Block> B addForcedBlock(B block) {
        blocks.add(block);
        if (block instanceof BlockBCBase_Neptune bc && !bc.id.isEmpty()) {
            String[] old = TagManager.getMultiTag(bc.id, EnumTagTypeMulti.OLD_REGISTRY_NAME);
            MigrationManager.INSTANCE.addBlockMigration(block, old);
        }
        return block;
    }

    @Nullable
    public <B extends BlockBCBase_Neptune> B addBlockAndItem(B block) {
        return addBlockAndItem(block, false, ItemBlockBC_Neptune::new);
    }

    @Nullable
    public <B extends BlockBCBase_Neptune> B addBlockAndItem(B block, boolean force) {
        return addBlockAndItem(block, false, ItemBlockBC_Neptune::new);
    }

    @Nullable
    public <B extends BlockBCBase_Neptune, I extends Item & IItemBuildCraft> B addBlockAndItem(
            B block, Function<B, I> itemCtor) {
        return addBlockAndItem(block, false, itemCtor);
    }

    public <B extends BlockBCBase_Neptune, I extends Item & IItemBuildCraft> B addBlockAndItem(
            B block, boolean force, Function<B, I> itemCtor) {
        B added = addBlock(block, force);
        if (added != null) {
            addForcedItem(itemCtor.apply(added));
        } else {
            RegistryConfig.setDisabled("items", block.id);
        }
        return added;
    }

    public <T extends BlockEntity> void registerTile(Class<T> clazz, String id, Block... validBlocks) {
        String regName = TagManager.get(id, EnumTagType.REGISTRY_NAME);
        blockEntities.add(new BlockEntityEntry<>(ResourceLocation.parse(regName), clazz, validBlocks));
    }

    private static final class BlockEntityEntry<T extends BlockEntity> {
        final ResourceLocation registryName;
        final Class<T> clazz;
        final Block[] validBlocks;

        /** Retained after registration so {@link RegisterCapabilitiesEvent} can attach capabilities to this type.
         * Null until {@link #buildType()} has run. */
        @Nullable
        BlockEntityType<T> type;

        BlockEntityEntry(ResourceLocation name, Class<T> clazz, Block[] blocks) {
            this.registryName = name;
            this.clazz = clazz;
            this.validBlocks = blocks;
        }

        BlockEntityType<T> buildType() {
            return BlockEntityType.Builder.<T>of(
                (pos, state) -> {
                    try {
                        return clazz.getConstructor(BlockPos.class, BlockState.class).newInstance(pos, state);
                    } catch (Exception e) {
                        throw new RuntimeException("Cannot instantiate " + clazz.getName(), e);
                    }
                },
                validBlocks
            ).build(null);
        }
    }
}
