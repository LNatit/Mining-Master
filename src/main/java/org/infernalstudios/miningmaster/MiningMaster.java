/*
 * Copyright 2022 Infernal Studios
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.infernalstudios.miningmaster;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.infernalstudios.miningmaster.client.gui.screen.inventory.GemForgeScreen;
import org.infernalstudios.miningmaster.enchantments.GraceEnchantment;
import org.infernalstudios.miningmaster.enchantments.HeartfeltEnchantment;
import org.infernalstudios.miningmaster.enchantments.KnightJumpEnchantment;
import org.infernalstudios.miningmaster.enchantments.RunnerEnchantment;
import org.infernalstudios.miningmaster.enchantments.SnowpiercerEnchantment;
import org.infernalstudios.miningmaster.events.MiningMasterClientEvents;
import org.infernalstudios.miningmaster.events.MiningMasterEvents;
import org.infernalstudios.miningmaster.init.MMBlocks;
import org.infernalstudios.miningmaster.init.MMContainerTypes;
import org.infernalstudios.miningmaster.init.MMEnchantments;
import org.infernalstudios.miningmaster.init.MMFeatures;
import org.infernalstudios.miningmaster.init.MMItems;
import org.infernalstudios.miningmaster.init.MMLootModifiers;
import org.infernalstudios.miningmaster.init.MMRecipes;
import org.infernalstudios.miningmaster.init.MMSounds;
import org.infernalstudios.miningmaster.init.MMTileEntityTypes;
import org.infernalstudios.miningmaster.network.MMNetworkHandler;


@Mod(MiningMaster.MOD_ID)
public class MiningMaster {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "miningmaster";

    public MiningMaster() {
        final ModLoadingContext modLoadingContext = ModLoadingContext.get();
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        MMBlocks.register(modEventBus);
        MMItems.register(modEventBus);
        MMEnchantments.register(modEventBus);
        MMRecipes.register(modEventBus);
        MMContainerTypes.register(modEventBus);
        MMTileEntityTypes.register(modEventBus);
        MMSounds.register(modEventBus);
        MMLootModifiers.register(modEventBus);
        MMFeatures.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(new MiningMasterEvents());
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.register(new MiningMasterClientEvents()));
        MinecraftForge.EVENT_BUS.addListener(RunnerEnchantment::onItemAttributeModifierCalculate);
        MinecraftForge.EVENT_BUS.addListener(RunnerEnchantment::onLivingUpdate);
        MinecraftForge.EVENT_BUS.addListener(HeartfeltEnchantment::onItemAttributeModifierCalculate);
        MinecraftForge.EVENT_BUS.addListener(HeartfeltEnchantment::onItemUnequip);
        MinecraftForge.EVENT_BUS.addListener(SnowpiercerEnchantment::onLivingUpdate);
        MinecraftForge.EVENT_BUS.addListener(GraceEnchantment::onLivingUpdate);
        MinecraftForge.EVENT_BUS.addListener(KnightJumpEnchantment::onClientTick);

    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(MMNetworkHandler::register);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(MMItems.AIR_MALACHITE_BOW.get(), new ResourceLocation("pull"), (itemStack, clientWorld, livingEntity, entityId) -> {
                if (livingEntity == null) {
                    return 0.0F;
                } else {
                    return livingEntity.getUseItem() != itemStack ? 0.0F : (float) (itemStack.getUseDuration() - livingEntity.getUseItemRemainingTicks()) / 20.0F;
                }
            });

            ItemProperties.register(MMItems.AIR_MALACHITE_BOW.get(), new ResourceLocation("pulling"), (itemStack, clientWorld, livingEntity, entityId) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0F : 0.0F);

            MenuScreens.register(MMContainerTypes.GEM_FORGE_CONTAINER.get(), GemForgeScreen::new);
        });
    }

    public static final CreativeModeTab TAB = new CreativeModeTab("MiningMasterTab") {

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(MMItems.TAB_ITEM.get());
        }

    };
}
