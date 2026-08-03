package buildcraft.lib.gui;

import java.util.function.Function;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import buildcraft.lib.gui.json.BuildCraftJsonGui;
import buildcraft.lib.gui.ledger.LedgerHelp;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.GuiUtil;

/** Reference implementation for a gui that delegates to a {@link BuildCraftGui} for most of its functionality. */
public class GuiScreenBuildCraft extends Screen {

    public final BuildCraftGui mainGui;

    /** Creates a new {@link GuiScreenBuildCraft} that will occupy the entire screen. */
    public GuiScreenBuildCraft() {
        this(g -> new BuildCraftGui(g));
    }

    /** Creates a new {@link GuiScreenBuildCraft} that will occupy the given {@link IGuiArea} Call
     * {@link GuiUtil#moveAreaToCentre(IGuiArea)} if you want a centred gui. (Ignoring ledgers, which will display off
     * to the side) */
    public GuiScreenBuildCraft(IGuiArea area) {
        this(g -> new BuildCraftGui(g, area));
    }

    public GuiScreenBuildCraft(Function<GuiScreenBuildCraft, BuildCraftGui> constructor) {
        super(Component.empty());
        this.mainGui = constructor.apply(this);
        standardLedgerInit();
    }

    /** Creates a new gui that will load its elements from the given json resource. */
    public GuiScreenBuildCraft(ResourceLocation jsonGuiDef) {
        super(Component.empty());
        BuildCraftJsonGui jsonGui = new BuildCraftJsonGui(this, jsonGuiDef);
        this.mainGui = jsonGui;
        standardLedgerInit();
    }

    /** Creates a new gui that will load its elements from the given json resource. Like
     * {@link #GuiScreenBuildCraft(IGuiArea)} this will occupy only the given {@link IGuiArea} */
    public GuiScreenBuildCraft(ResourceLocation jsonGuiDef, IGuiArea area) {
        super(Component.empty());
        BuildCraftJsonGui jsonGui = new BuildCraftJsonGui(this, area, jsonGuiDef);
        this.mainGui = jsonGui;
        standardLedgerInit();
    }

    private final void standardLedgerInit() {
        if (shouldAddHelpLedger()) {
            mainGui.shownElements.add(new LedgerHelp(mainGui, false));
        }
    }

    protected boolean shouldAddHelpLedger() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        mainGui.tick();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        mainGui.drawBackgroundLayer(graphics, partialTicks, mouseX, mouseY, () -> this.renderTransparentBackground(graphics));
        mainGui.drawElementBackgrounds(graphics);
        mainGui.drawElementForegrounds(graphics, () -> this.renderTransparentBackground(graphics));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (!mainGui.onMouseClicked(mouseX, mouseY, mouseButton)) {
            return super.mouseClicked(mouseX, mouseY, mouseButton);
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        boolean result = super.mouseReleased(mouseX, mouseY, state);
        mainGui.onMouseReleased(mouseX, mouseY, state);
        return result;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double dragX, double dragY) {
        boolean result = super.mouseDragged(mouseX, mouseY, clickedMouseButton, dragX, dragY);
        mainGui.onMouseDragged(mouseX, mouseY, clickedMouseButton, dragX, dragY);
        return result;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!mainGui.onKeyTyped((char) 0, keyCode)) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        return true;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!mainGui.onKeyTyped(codePoint, -1)) {
            return super.charTyped(codePoint, modifiers);
        }
        return true;
    }
}
