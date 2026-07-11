package com.buuz135.dysoncubeproject.datagen;

import com.buuz135.dysoncubeproject.DCPContent;
import com.buuz135.dysoncubeproject.DysonCubeProject;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class DCPAdvancementProvider extends AdvancementProvider {

    public DCPAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, List.of(new DCPAdvancementGenerator()));
    }


    public static class DCPAdvancementGenerator implements AdvancementProvider.AdvancementGenerator {

        @Override
        public void generate(HolderLookup.Provider provider, Consumer<AdvancementHolder> consumer, ExistingFileHelper existingFileHelper) {
            AdvancementHolder root = Advancement.Builder.advancement()
                    .display(DCPContent.Blocks.EM_RAILEJECTOR_CONTROLLER,
                            advancementName("root.title"),
                            advancementName("root.description"),
                            ResourceLocation.parse("minecraft:textures/block/iron_block.png"),
                            AdvancementType.TASK, false, true, false)
                    .addCriterion("railejector", InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[]{}))
                    .save(consumer, getNameId("main/root"));
            var amounts = new int[]{5, 15, 25, 50, 75, 100};
            var parent = root;
            for (int i = 0; i < amounts.length; i++) {
                parent = getAdvancement(parent, amounts[i] < 50 ? DCPContent.Items.SOLAR_SAIL.get() : DCPContent.Items.SOLAR_SAIL_PACKAGE.get(),
                        "em_railejector_controller/sphere_percentage_" + amounts[i], amounts[i] < 50 ? AdvancementType.TASK : AdvancementType.CHALLENGE, true, true, false)
                        .save(consumer, getNameId("main/em_railejector_controller/" + amounts[i]));
            }
        }

        private Advancement.Builder getAdvancement(AdvancementHolder parent, ItemLike display, String name, AdvancementType frame, boolean showToast, boolean announceToChat, boolean hidden) {
            return Advancement.Builder.advancement().parent(parent)
                    .display(display,
                            advancementName(name + ".title"),
                            advancementName(name + ".description"),
                            null, frame, showToast, announceToChat, hidden)
                    .addCriterion("impossible", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()));
        }

        public Component advancementName(String name) {
            return Component.translatable("advancement." + DysonCubeProject.MODID + "." + name);
        }

        private String getNameId(String id) {
            return DysonCubeProject.MODID + ":" + id;
        }
    }


}
