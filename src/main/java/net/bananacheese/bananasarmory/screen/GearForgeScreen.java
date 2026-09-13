package net.bananacheese.bananasarmory.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Ported from GearForgeScreen. Renames: HandledScreen -> AbstractContainerScreen,
 * PlayerInventory -> Inventory, DrawContext -> GuiGraphics,
 * drawTexture -> blit, Identifier -> ResourceLocation,
 * drawBackground -> renderBg, drawMouseoverTooltip -> renderTooltip,
 * drawText -> drawString, Text -> Component.
 *
 * NOTE: the exact `blit` overload signature (which takes a RenderPipeline
 * argument, per the original's `RenderPipelines.GUI_TEXTURED`) has moved
 * around across 1.21.x point releases — verify the parameter order against
 * 1.21.1's GuiGraphics once you can compile locally. The simpler classic
 * overload `blit(ResourceLocation, x, y, u, v, width, height, textureWidth, textureHeight)`
 * is used below since it's the most stable one across versions.
 */
@OnlyIn(Dist.CLIENT)
public class GearForgeScreen extends AbstractContainerScreen<GearForgeMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("gearforge", "textures/gui/gear_forge.png");

    public GearForgeScreen(GearForgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageHeight = 169; // Same as loom
        this.inventoryLabelY = this.imageHeight - 93;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float delta, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);

        drawStatsPanel(guiGraphics, mouseX, mouseY);
    }

    private void drawStatsPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int panelX = x + 8;
        int panelY = y + 16;

        ItemStack frameItem = menu.getInventory().getItem(0);

        if (!frameItem.isEmpty()) {
            // TODO: Calculate actual stats from item and upgrades
            guiGraphics.drawString(font, Component.literal("Stats:"), panelX + 2, panelY + 2, 0x404040, false);
            guiGraphics.drawString(font, Component.literal("Defense: 5"), panelX + 2, panelY + 12, 0x404040, false);
            guiGraphics.drawString(font, Component.literal("Durability: 100"), panelX + 2, panelY + 22, 0x404040, false);

            guiGraphics.drawString(font, Component.literal("+2 Defense"), panelX + 2, panelY + 35, 0x00AA00, false);
            guiGraphics.drawString(font, Component.literal("+10 Dura"), panelX + 2, panelY + 45, 0x00AA00, false);
        } else {
            guiGraphics.drawString(font, Component.literal("Place gear"), panelX + 2, panelY + 2, 0x808080, false);
            guiGraphics.drawString(font, Component.literal("to modify"), panelX + 2, panelY + 12, 0x808080, false);
        }
    }
}
