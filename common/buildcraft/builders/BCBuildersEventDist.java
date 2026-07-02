/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.builders;

import java.lang.ref.WeakReference;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import net.minecraftforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.event.level.GetCollisionBoxesEvent;
// TODO (Phase 8): net.minecraftforge.fml.client.config.GuiUtils removed in NeoForge 1.21.1;
// drawGradientRect now lives on GuiGraphics. Callers below need to be rewritten to use a
// GuiGraphics instance instead of static GuiUtils calls.
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.api.schematics.ISchematicBlock;

import buildcraft.builders.client.ClientArchitectTables;
import buildcraft.builders.item.ItemSchematicSingle;
import buildcraft.builders.snapshot.Blueprint;
import buildcraft.builders.snapshot.ClientSnapshots;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.snapshot.Snapshot.Header;
import buildcraft.builders.tile.TileQuarry;

public enum BCBuildersEventDist {
    INSTANCE;

    private static final UUID UUID_SINGLE_SCHEMATIC = new UUID(0xfd3b8c59b0a8b191L, 0x772ec006c1b0ffaaL);
    private final Map<Level, Deque<WeakReference<TileQuarry>>> allQuarries = new WeakHashMap<>();

    public synchronized void validateQuarry(TileQuarry quarry) {
        Deque<WeakReference<TileQuarry>> quarries =
            allQuarries.computeIfAbsent(quarry.getWorld(), k -> new LinkedList<>());
        quarries.add(new WeakReference<>(quarry));
    }

    public synchronized void invalidateQuarry(TileQuarry quarry) {
        Deque<WeakReference<TileQuarry>> quarries = allQuarries.get(quarry.getWorld());
        if (quarries == null) {
            // Odd.
            return;
        }
        Iterator<WeakReference<TileQuarry>> iter = quarries.iterator();
        while (iter.hasNext()) {
            WeakReference<TileQuarry> ref = iter.next();
            TileQuarry pos = ref.get();
            if (pos == null || pos == quarry) {
                iter.remove();
            }
        }
    }

    @SubscribeEvent
    public synchronized void onGetCollisionBoxesForQuarry(GetCollisionBoxesEvent event) {
        Deque<WeakReference<TileQuarry>> quarries = allQuarries.get(event.getWorld());
        if (quarries == null) {
            // No quarries in the target world
            return;
        }
        Iterator<WeakReference<TileQuarry>> iter = quarries.iterator();
        while (iter.hasNext()) {
            WeakReference<TileQuarry> ref = iter.next();
            TileQuarry quarry = ref.get();
            if (quarry == null) {
                iter.remove();
                continue;
            }
            for (AABB aabb : quarry.getCollisionBoxes()) {
                if (event.getAabb().intersects(aabb)) {
                    event.getCollisionBoxesList().add(aabb);
                }
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onRenderTooltipPostText(RenderTooltipEvent.PostText event) {
        Snapshot snapshot = null;
        ItemStack stack = event.getStack();
        Header header = BCBuildersItems.snapshot != null ? BCBuildersItems.snapshot.getHeader(stack) : null;
        if (header != null) {
            snapshot = ClientSnapshots.INSTANCE.getSnapshot(header.key);
        } else if (BCBuildersItems.schematicSingle != null) {
            ISchematicBlock schematicBlock = ItemSchematicSingle.getSchematicSafe(stack);
            if (schematicBlock != null) {
                Blueprint blueprint = new Blueprint();
                blueprint.size = new BlockPos(1, 1, 1);
                blueprint.offset = BlockPos.ORIGIN;
                blueprint.data = new int[] { 0 };
                blueprint.palette.add(schematicBlock);
                blueprint.computeKey();
                snapshot = blueprint;
            }
        }

        if (snapshot != null) {
            int pX = event.getX();
            int pY = event.getY() + event.getHeight() + 10;
            int sX = 100;
            int sY = 100;

            // Copy from GuiUtils#drawHoveringText
            // TODO (Phase 8): GuiUtils removed in NeoForge 1.21.1; drawGradientRect now lives
            // on GuiGraphics. This tooltip-style background box needs to be redrawn using a
            // GuiGraphics instance obtained from the current screen/render context.
            int zLevel = 300;
            int backgroundColor = 0xF0100010;
            int borderColorStart = 0x505000FF;
            int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;

            ClientSnapshots.INSTANCE.renderSnapshot(snapshot, pX, pY, sX, sY);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onTickClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !Minecraft.getInstance().isGamePaused()) {
            ClientArchitectTables.tick();
        }
    }
}
