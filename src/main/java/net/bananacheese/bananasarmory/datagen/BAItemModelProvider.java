package net.bananacheese.bananasarmory.datagen;

import net.bananacheese.bananasarmory.BananasArmory;
import net.bananacheese.bananasarmory.item.BAItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

public class BAItemModelProvider extends ItemModelProvider {

    public BAItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, BananasArmory.MODID, existingFileHelper);
    }

    @Override
    public void registerModels() {

        handheldItem(BAItems.GEAR_FORGE_HAMMER);

        registerArmorComponent(BAItems.IRON_PAULDRONS.get(), "iron_pauldrons");
        registerArmorComponent(BAItems.IRON_GORGET.get(), "iron_gorget");
        registerArmorComponent(BAItems.IRON_FAULD.get(), "iron_fauld");
        registerArmorComponent(BAItems.IRON_REINFORCEMENT.get(), "iron_reinforcement");
    }

    private void registerArmorComponent(Item item, String name) {
        withExistingParent(name, mcLoc("item/generated"))
                .texture("layer0", "item/armor/components/item/" + name);
    }

    private ItemModelBuilder handheldItem(DeferredItem<?> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/handheld")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(BananasArmory.MODID,"item/" + item.getId().getPath()));
    }
}
