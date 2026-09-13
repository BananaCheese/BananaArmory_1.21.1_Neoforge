package net.bananacheese.bananasarmory;

import net.bananacheese.bananasarmory.item.custom.ArmorComponentItem;
import net.bananacheese.bananasarmory.item.custom.ArmorFrameItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

public class BananaArmoryTooltips {
    public static void onItemTooltip(ItemTooltipEvent event) {
        var stack = event.getItemStack();
        List<Component> lines = event.getToolTip();

        if (stack.getItem() instanceof ArmorFrameItem frameItem) {
            addArmorFrameTooltip(stack, frameItem, lines);
        } else if (stack.getItem() instanceof ArmorComponentItem component) {
            addArmorComponentTooltip(component, lines);
        }
    }

    private static void addArmorFrameTooltip(net.minecraft.world.item.ItemStack stack,
                                             ArmorFrameItem frameItem, List<Component> lines) {
        lines.add(Component.literal(frameItem.getFrameType().getDisplayName() + " Frame")
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("Place in Gear Forge to customize").withStyle(ChatFormatting.DARK_GRAY));

        int totalDefense = ArmorFrameItem.getTotalDefense(stack);
        int totalDurability = ArmorFrameItem.getTotalDurability(stack);
        double totalToughness = ArmorFrameItem.getTotalToughness(stack);

        lines.add(Component.literal(""));
        lines.add(Component.literal("Defense: " + totalDefense).withStyle(ChatFormatting.BLUE));
        lines.add(Component.literal("Toughness: " + String.format("%.1f", totalToughness)).withStyle(ChatFormatting.AQUA));
        lines.add(Component.literal("Durability: " + totalDurability).withStyle(ChatFormatting.GREEN));

        List<ArmorFrameItem.ComponentData> components = ArmorFrameItem.getComponents(stack);
        if (!components.isEmpty()) {
            lines.add(Component.literal(""));
            lines.add(Component.literal("Attached Components:").withStyle(ChatFormatting.GOLD));
            for (ArmorFrameItem.ComponentData comp : components) {
                String componentName = comp.id().substring(comp.id().lastIndexOf(':') + 1)
                        .replace('_', ' ');

                StringBuilder compDesc = new StringBuilder("  \u2022 " + componentName);
                if (comp.defenseBonus() > 0 || comp.toughnessBonus() > 0 || comp.durabilityBonus() > 0) {
                    compDesc.append(" (");
                    boolean first = true;
                    if (comp.defenseBonus() > 0) {
                        compDesc.append("+").append(comp.defenseBonus()).append(" def");
                        first = false;
                    }
                    if (comp.toughnessBonus() > 0) {
                        if (!first) compDesc.append(", ");
                        compDesc.append("+").append(String.format("%.1f", comp.toughnessBonus())).append(" tough");
                        first = false;
                    }
                    if (comp.durabilityBonus() > 0) {
                        if (!first) compDesc.append(", ");
                        compDesc.append("+").append(comp.durabilityBonus()).append(" dur");
                    }
                    compDesc.append(")");
                }

                lines.add(Component.literal(compDesc.toString()).withStyle(ChatFormatting.GRAY));
            }
        }

        lines.add(Component.literal(""));
        lines.add(Component.literal(ArmorFrameItem.getComponentCount(stack) + "/6 Component Slots")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    private static void addArmorComponentTooltip(ArmorComponentItem component, List<Component> lines) {
        lines.add(Component.literal(component.getComponentType().getDisplayName()).withStyle(ChatFormatting.BLUE));

        if (component.getDefenseBonus() > 0) {
            lines.add(Component.literal("+" + component.getDefenseBonus() + " Defense").withStyle(ChatFormatting.GREEN));
        }

        if (component.getToughnessBonus() > 0) {
            lines.add(Component.literal("+" + String.format("%.1f", component.getToughnessBonus()) + " Toughness")
                    .withStyle(ChatFormatting.AQUA));
        }

        if (component.getDurabilityBonus() > 0) {
            lines.add(Component.literal("+" + component.getDurabilityBonus() + " Durability").withStyle(ChatFormatting.GREEN));
        }

        lines.add(Component.literal("Compatible: " + component.getComponentType().getCompatibility())
                .withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.literal("Group: " + component.getComponentGroup()).withStyle(ChatFormatting.DARK_GRAY));
    }

    private BananaArmoryTooltips() {
    }
}
