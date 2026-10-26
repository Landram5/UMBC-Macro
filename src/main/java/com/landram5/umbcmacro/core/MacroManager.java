package com.landram5.umbcmacro.core;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public class MacroManager {
   private final ConfigManager config;
   private final List<Macro> macros = new ArrayList<>();
   private final Set<String> activeCombos = new HashSet<>();
   private final Set<String> suppressedCombos = new HashSet<>();

   public MacroManager(ConfigManager config) {
      this.config = config;
   }

   public List<Macro> getMacros() {
      return this.macros;
   }

   public void load() {
      this.macros.clear();
      this.macros.addAll(this.config.loadOrDefault());
   }

   public void save() {
      this.config.save(this.macros);
   }

   public void suppressNextActivation(Modifier modifier, int keyCode) {
      if (keyCode != -1) {
         this.suppressedCombos.add(this.comboKey(modifier, keyCode));
      }
   }

   public void tick(Minecraft client) {
      if (client != null && client.player != null) {
         boolean ctrl = InputConstants.isKeyDown(InputConstants.KEY_LCONTROL) || InputConstants.isKeyDown(InputConstants.KEY_RCONTROL);
         boolean alt = InputConstants.isKeyDown(InputConstants.KEY_LALT) || InputConstants.isKeyDown(InputConstants.KEY_RALT);
         boolean shift = InputConstants.isKeyDown(InputConstants.KEY_LSHIFT) || InputConstants.isKeyDown(InputConstants.KEY_RSHIFT);

         for (Macro m : this.macros) {
            if (m.keyCode != -1) {
               boolean base = InputConstants.isKeyDown(m.keyCode);
               boolean mod = false;
               if (m.modifier == Modifier.NONE) {
                  mod = !ctrl && !alt && !shift;
               } else if (m.modifier == Modifier.CTRL) {
                  mod = ctrl && !alt && !shift;
               } else if (m.modifier == Modifier.ALT) {
                  mod = alt && !ctrl && !shift;
               } else if (m.modifier == Modifier.SHIFT) {
                  mod = shift && !ctrl && !alt;
               }

               String key = this.comboKey(m.modifier, m.keyCode);
               boolean pressed = base && mod;
               if (pressed) {
                  if (this.suppressedCombos.contains(key)) {
                     this.activeCombos.add(key);
                  } else if (!this.activeCombos.contains(key)) {
                     this.send(client, m.command);
                     this.activeCombos.add(key);
                  }
               } else {
                  this.activeCombos.remove(key);
                  this.suppressedCombos.remove(key);
               }
            }
         }
      }
   }

   private String comboKey(Modifier modifier, int keyCode) {
      return modifier + ":" + keyCode;
   }

   private void send(Minecraft client, String message) {
      if (message != null && !message.isEmpty()) {
         ClientPacketListener h = client.player.connection;
         if (h != null) {
            if (message.startsWith("/")) {
               String cmd = message.substring(1);
               h.sendCommand(cmd);
            } else {
               h.sendChat(message);
            }
         }
      }
   }
}
