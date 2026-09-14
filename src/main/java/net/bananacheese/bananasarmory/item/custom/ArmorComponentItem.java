package net.bananacheese.bananasarmory.item.custom;

import net.minecraft.world.item.Item;

public class ArmorComponentItem extends Item {
    private final ComponentType componentType;
    private final String componentGroup;
    private final int defenseBonus;
    private final int durabilityBonus;
    private final double toughnessBonus;

    public ArmorComponentItem(Properties properties, ComponentType componentType, String componentGroup,
                               int defenseBonus, int durabilityBonus, double toughnessBonus) {
        super(properties);
        this.componentType = componentType;
        this.componentGroup = componentGroup;
        this.defenseBonus = defenseBonus;
        this.durabilityBonus = durabilityBonus;
        this.toughnessBonus = toughnessBonus;
    }

    public ArmorComponentItem(Properties properties, ComponentType componentType, String componentGroup,
                               int defenseBonus, int durabilityBonus) {
        this(properties, componentType, componentGroup, defenseBonus, durabilityBonus, 0.0);
    }

    public ComponentType getComponentType() {
        return componentType;
    }

    public String getComponentGroup() {
        return componentGroup;
    }

    public int getDefenseBonus() {
        return defenseBonus;
    }

    public int getDurabilityBonus() {
        return durabilityBonus;
    }

    public double getToughnessBonus() {
        return toughnessBonus;
    }

    public enum ComponentType {
        VISOR("Visor", "Helmet"),
        PLUME("Plume", "Helmet"),

        PAULDRONS("Pauldrons", "Chestplate"),
        GORGET("Gorget", "Chestplate"),
        FAULD("Fauld", "Chestplate"),

        TASSET("Tasset", "Leggings"),
        GREAVES("Greaves", "Leggings"),

        SPURS("Spurs", "Boots"),
        SABATONS("Sabatons", "Boots"),

        REINFORCEMENT("Reinforcement", "All");

        private final String displayName;
        private final String compatibility;

        ComponentType(String displayName, String compatibility) {
            this.displayName = displayName;
            this.compatibility = compatibility;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getCompatibility() {
            return compatibility;
        }

        public boolean isCompatibleWith(ArmorFrameItem.ArmorFrameType frameType) {
            if (compatibility.equals("All")) return true;
            return compatibility.equalsIgnoreCase(frameType.getDisplayName());
        }
    }
}
