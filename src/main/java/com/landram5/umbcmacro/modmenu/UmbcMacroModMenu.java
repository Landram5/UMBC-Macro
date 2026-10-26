package com.landram5.umbcmacro.modmenu;

import com.landram5.umbcmacro.UmbcMacroClient;
import com.landram5.umbcmacro.gui.UmbcMacroScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class UmbcMacroModMenu implements ModMenuApi {
   public ConfigScreenFactory<?> getModConfigScreenFactory() {
      return parent -> new UmbcMacroScreen(parent, UmbcMacroClient.getMacroManager());
   }
}
