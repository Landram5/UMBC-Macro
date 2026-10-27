package com.landram5.umbcmacro;

import com.mojang.blaze3d.platform.InputConstants;
import com.landram5.umbcmacro.core.ConfigManager;
import com.landram5.umbcmacro.core.MacroManager;
import com.landram5.umbcmacro.gui.UmbcMacroScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping.Category;

public class UmbcMacroClient implements ClientModInitializer {
   private static MacroManager macroManager;
   private static KeyMapping openScreenKey;

   public static MacroManager getMacroManager() {
      return macroManager;
   }

   private static void openScreen(Minecraft mc) {
      mc.gui.setScreen(new UmbcMacroScreen(mc.gui.screen(), macroManager));
   }

   public void onInitializeClient() {
      ConfigManager config = new ConfigManager(FabricLoader.getInstance().getConfigDir());
      macroManager = new MacroManager(config);
      macroManager.load();
      openScreenKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.umbcmacro.open_screen", InputConstants.KEY_J, Category.MISC));
      ClientCommandRegistrationCallback.EVENT
         .register(
            (dispatcher, registryAccess) -> dispatcher.register(
               ClientCommands.literal("umbcmacro").executes(ctx -> {
                  Minecraft mc = Minecraft.getInstance();
                  openScreen(mc);
                  return 1;
               })
            )
         );
      ClientTickEvents.END_CLIENT_TICK.register(client -> {
         while (openScreenKey.consumeClick()) {
            if (client.gui.screen() == null) {
               openScreen(client);
            }
         }

         macroManager.tick(client);
      });
   }
}
