/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import java.util.function.Function;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import buildcraft.api.core.render.ISprite;

import buildcraft.lib.gui.json.BuildCraftJsonGui;
import buildcraft.lib.gui.json.InventorySlotHolder;
import buildcraft.lib.gui.ledger.LedgerHelp;
import buildcraft.lib.gui.ledger.LedgerOwnership;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.GuiUtil;

/** Future rename: "GuiContainerBuildCraft" */
public abstract class GuiBC8<C extends ContainerBC_Neptune> extends AbstractContainerScreen<C> {
    public final BuildCraftGui mainGui;
    public final C container;

    /** The {@link GuiGraphics} that is currently being used to render this screen. Only valid while inside one of the
     * render callbacks (ie between {@link #render(GuiGraphics, int, int, float)} being called, and it returning). */
    protected GuiGraphics graphics;

    public GuiBC8(C container) {
        this(container, g -> new BuildCraftGui(g, createWindowedArea(g)));
    }

    /** Creates a new {@link IGuiArea} that takes its bounds from the given {@link GuiBC8}'s size. */
    public static IGuiArea createWindowedArea(GuiBC8<?> gui) {
        return IGuiArea.create(() -> (double) gui.leftPos, () -> (double) gui.topPos, () -> (double) gui.imageWidth,
            () -> (double) gui.imageHeight);
    }

    public GuiBC8(C container, Function<GuiBC8<?>, BuildCraftGui> constructor) {
        super(container, container.player.getInventory(), Component.empty());
        this.container = container;
        this.mainGui = constructor.apply(this);
        standardLedgerInit();
    }

    public GuiBC8(C container, ResourceLocation jsonGuiDef) {
        super(container, container.player.getInventory(), Component.empty());
        this.container = container;
        BuildCraftJsonGui jsonGui = new BuildCraftJsonGui(this, createWindowedArea(this), jsonGuiDef);
        jsonGui.properties.put("player.getInventory()", new InventorySlotHolder(container, container.player.getInventory()));
        this.mainGui = jsonGui;
        standardLedgerInit();
        // Force subclasses to set this themselves after calling jsonGui.load
        imageWidth = 10;
        imageHeight = 10;
    }

    private final void standardLedgerInit() {
        if (container instanceof ContainerBCTile<?>) {
            mainGui.shownElements.add(new LedgerOwnership(mainGui, ((ContainerBCTile<?>) container).tile, true));
        }
        if (shouldAddHelpLedger()) {
            mainGui.shownElements.add(new LedgerHelp(mainGui, false));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.graphics = graphics;
        super.render(graphics, mouseX, mouseY, partialTicks);
        if (mainGui.currentMenu == null || !mainGui.currentMenu.shouldFullyOverride()) {
            this.renderTooltip(graphics, mouseX, mouseY);
        }
    }

    protected boolean shouldAddHelpLedger() {
        return true;
    }

    // Protected -> Public

    public void drawGradientRect(IGuiArea area, int startColor, int endColor) {
        int left = (int) area.getX();
        int right = (int) area.getEndX();
        int top = (int) area.getY();
        int bottom = (int) area.getEndY();
        drawGradientRect(left, top, right, bottom, startColor, endColor);
    }

    public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        if (graphics != null) {
            graphics.fillGradient(left, top, right, bottom, startColor, endColor);
        }
    }

    // Gui -- double -> int

    public void drawTexturedModalRect(double posX, double posY, double textureX, double textureY, double width,
        double height) {
        int x = Mth.floor(posX);
        int y = Mth.floor(posY);
        int u = Mth.floor(textureX);
        int v = Mth.floor(textureY);
        int w = Mth.floor(width);
        int h = Mth.floor(height);
        // TODO Phase 7: there is no direct "blit from the currently-bound texture" equivalent of the old
        // Gui#drawTexturedModalRect using GuiGraphics -- callers need to be updated to use
        // GuiGraphics#blit(ResourceLocation, ...) with an explicit texture location instead.
    }

    public void drawString(Font font, String text, double x, double y, int colour) {
        drawString(font, text, x, y, colour, true);
    }

    public void drawString(Font font, String text, double x, double y, int colour, boolean shadow) {
        if (graphics != null) {
            graphics.drawString(font, text, (int) x, (int) y, colour, shadow);
        }
    }

    // Other

    /** @deprecated Use {@link GuiUtil#drawItemStackAt(ItemStack,int,int)} instead */
    @Deprecated
    public static void drawItemStackAt(ItemStack stack, int x, int y) {
        GuiUtil.drawItemStackAt(stack, x, y);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        mainGui.tick();
    }

    @Override
    protected final void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        mainGui.drawBackgroundLayer(graphics, partialTicks, mouseX, mouseY, () -> {});
        drawBackgroundLayer(partialTicks);
        mainGui.drawElementBackgrounds(graphics);
    }

    @Override
    protected final void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        mainGui.preDrawForeground(graphics);

        drawForegroundLayer();
        mainGui.drawElementForegrounds(graphics, () -> this.renderTransparentBackground(graphics));
        drawForegroundLayerAboveElements();

        mainGui.postDrawForeground(graphics);
    }

    public void drawProgress(GuiRectangle rect, GuiIcon icon, double widthPercent, double heightPercent) {
        double nWidth = rect.width * Math.abs(widthPercent);
        double nHeight = rect.height * Math.abs(heightPercent);
        ISprite sprite = GuiUtil.subRelative(icon.sprite, 0, 0, widthPercent, heightPercent);
        double x = rect.x + mainGui.rootElement.getX();
        double y = rect.y + mainGui.rootElement.getY();
        GuiIcon.draw(sprite, x, y, x + nWidth, y + nHeight);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        boolean result = super.mouseClicked(mouseX, mouseY, mouseButton);
        mainGui.onMouseClicked(mouseX, mouseY, mouseButton);
        return result;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double dragX, double dragY) {
        boolean result = super.mouseDragged(mouseX, mouseY, clickedMouseButton, dragX, dragY);
        mainGui.onMouseDragged(mouseX, mouseY, clickedMouseButton, dragX, dragY);
        return result;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        boolean result = super.mouseReleased(mouseX, mouseY, state);
        mainGui.onMouseReleased(mouseX, mouseY, state);
        return result;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (mainGui.onKeyTyped((char) 0, keyCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (mainGui.onKeyTyped(codePoint, -1)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    protected void drawBackgroundLayer(float partialTicks) {}

    protected void drawForegroundLayer() {}

    /** Like {@link #drawForegroundLayer()}, but is called after all {@link IGuiElement}'s have been drawn. */
    protected void drawForegroundLayerAboveElements() {}
}
