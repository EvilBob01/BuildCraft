/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.client.gui;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.ledger.LedgerEngine;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.LocaleUtil;

import buildcraft.energy.container.ContainerEngineStone_BC8;

public class GuiEngineStone_BC8 extends GuiBC8<ContainerEngineStone_BC8> {
    private static final ResourceLocation TEXTURE_BASE = ResourceLocation.parse("buildcraftenergy:textures/gui/steam_engine_gui.png");
    private static final int SIZE_X = 176, SIZE_Y = 166;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);

    private final IGuiArea flameRect = new GuiRectangle(81, 25, 14, 14).offset(mainGui.rootElement);
    private final IGuiArea fuelSlotRect = new GuiRectangle(78, 39, 20, 20).offset(mainGui.rootElement);
    private final ElementHelpInfo helpFlame, helpFuel;

    public GuiEngineStone_BC8(ContainerEngineStone_BC8 container) {
        super(container);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
        mainGui.shownElements.add(new LedgerEngine(mainGui, container.tile, true));
        helpFlame = new ElementHelpInfo("buildcraft.help.stone_engine.flame.title", 0xFF_FF_FF_1F, "buildcraft.help.stone_engine.flame");
        // TODO: Auto list of example fuels!
        helpFuel = new ElementHelpInfo("buildcraft.help.stone_engine.fuel.title", 0xFF_AA_33_33, "buildcraft.help.stone_engine.fuel");
    }

    @Override
    public void init() {
        super.init();
        mainGui.shownElements.add(new DummyHelpElement(flameRect.expand(2), helpFlame));
        mainGui.shownElements.add(new DummyHelpElement(fuelSlotRect, helpFuel));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        ICON_GUI.drawAt(mainGui.rootElement);

        double amount = container.tile.deltaFuelLeft.getDynamic(partialTicks) / 100;

        if (amount > 0) {
            int flameHeight = (int) Math.ceil(amount * flameRect.getHeight());

            guiGraphics.blit(TEXTURE_BASE, (int) flameRect.getX(), (int) (flameRect.getY() + flameRect.getHeight() - flameHeight), 176, 14 - flameHeight, 14, flameHeight + 2);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        String str = LocaleUtil.localize("tile.engineStone.name");
        int strWidth = font.width(str);
        double titleX = mainGui.rootElement.getCenterX() - strWidth / 2;
        double titleY = mainGui.rootElement.getY() + 6;
        guiGraphics.drawString(font, str, (int) titleX, (int) titleY, 0x404040);

        double invX = mainGui.rootElement.getX() + 8;
        double invY = mainGui.rootElement.getY() + SIZE_Y - 96;
        guiGraphics.drawString(font, LocaleUtil.localize("gui.inventory"), (int) invX, (int) invY, 0x404040);
    }
}
