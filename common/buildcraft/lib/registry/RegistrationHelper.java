package buildcraft.lib.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

import buildcraft.lib.block.BlockBCBase_Neptune;
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
        }
    }

    public static void registerTagEntries() {
        // Tags replace OreDictionary; handled via data packs
    }

    private void onRegisterBlocks(RegisterEvent event) {
        event.register(Registries.BLOCK, helper -> {
            for (Block block : blocks) {
                if (block instanceof BlockBCBase_Neptune bcBlock && !bcBlock.id.isEmpty()) {
                    String regName = TagManager.getTag(bcBlock.id, EnumTagType.REGISTRY_NAME);
                    helper.register(ResourceLocation.parse(regName), block);
                }
            }
        });
    }

    private void onRegisterItems(RegisterEvent event) {
        event.register(Registries.ITEM, helper -> {
            for (Item item : items) {
                if (item instanceof IItemBuildCraft bcItem && !bcItem.id().isEmpty()) {
                    String regName = TagManager.getTag(bcItem.id(), EnumTagType.REGISTRY_NAME);
                    helper.register(ResourceLocation.parse(regName), item);
                }
            }
        });
    }

    private void onRegisterBlockEntities(RegisterEvent event) {
        event.register(Registries.BLOCK_ENTITY_TYPE, helper -> {
            for (BlockEntityEntry<?> entry : blockEntities) {
                helper.register(entry.registryName, entry.buildType());
            }
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
        String regName = TagManager.getTag(id, EnumTagType.REGISTRY_NAME);
        blockEntities.add(new BlockEntityEntry<>(ResourceLocation.parse(regName), clazz, validBlocks));
    }

    private static final class BlockEntityEntry<T extends BlockEntity> {
        final ResourceLocation registryName;
        final Class<T> clazz;
        final Block[] validBlocks;

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
