package com.landram5.umbcmacro.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.landram5.umbcmacro.core.Macro;
import com.landram5.umbcmacro.core.MacroManager;
import com.landram5.umbcmacro.core.Modifier;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

public class UmbcMacroScreen extends Screen {
   private static final int PANEL_WIDTH = 620;
   private static final int ROW_HEIGHT = 52;
   private static final int ROW_SPACING = 8;
   private final Screen parent;
   private final MacroManager manager;
   private final List<UmbcMacroScreen.Row> rows = new ArrayList<>();
   private int panelLeft;
   private int panelTop;
   private int panelWidth;

   public UmbcMacroScreen(Screen parent, MacroManager manager) {
      super(Component.literal("UMBC Macro"));
      this.parent = parent;
      this.manager = manager;
   }

   protected void init() {
      this.rows.clear();
      this.panelWidth = Math.min(620, this.width - 40);
      this.panelLeft = (this.width - this.panelWidth) / 2;
      this.panelTop = 42;
      int y = this.panelTop + 22;

      for (Macro m : this.manager.getMacros()) {
         UmbcMacroScreen.Row r = new UmbcMacroScreen.Row(m);
         r.init(this, this.panelLeft + 10, y, this.panelWidth - 20);
         this.rows.add(r);
         y += 60;
      }

      Button add = Button.builder(Component.literal("Add Macro"), b -> {
         Macro m = new Macro(Modifier.NONE, -1, "");
         this.manager.getMacros().add(m);
         this.manager.save();
         Minecraft.getInstance().gui.setScreen(new UmbcMacroScreen(this.parent, this.manager));
      }).bounds(this.panelLeft, this.height - 30, 110, 20).build();
      this.addRenderableWidget(add);
      Button done = Button.builder(Component.literal("Done"), b -> {
         this.manager.save();
         Minecraft.getInstance().gui.setScreen(this.parent);
      }).bounds(this.panelLeft + this.panelWidth - 110, this.height - 30, 110, 20).build();
      this.addRenderableWidget(done);
      if (!this.rows.isEmpty()) {
         this.setInitialFocus(this.rows.get(0).command);
      }
   }

   public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
      this.extractTransparentBackground(extractor);
      extractor.fill(this.panelLeft - 6, this.panelTop - 10, this.panelLeft + this.panelWidth + 6, this.height - 42, -1341124592);
      extractor.outline(this.panelLeft - 6, this.panelTop - 10, this.panelWidth + 12, this.height - this.panelTop - 32, -7631989);
      extractor.centeredText(this.font, this.title, this.width / 2, 18, 16777215);
      extractor.text(this.font, Component.literal("Modifier"), this.panelLeft + 14, this.panelTop + 7, 10526880);
      extractor.text(this.font, Component.literal("Key"), this.panelLeft + 124, this.panelTop + 7, 10526880);
      extractor.text(this.font, Component.literal("Command"), this.panelLeft + 214, this.panelTop + 7, 10526880);
      extractor.text(
         this.font, Component.literal("Press ESC, Backspace, or Delete while selecting a key to clear it."), this.panelLeft, this.height - 46, 10526880
      );

      for (UmbcMacroScreen.Row r : this.rows) {
         r.render(extractor);
      }

      super.extractRenderState(extractor, mouseX, mouseY, delta);
   }

   public void tick() {
      for (UmbcMacroScreen.Row r : this.rows) {
         if (r.listening) {
            int code = this.detectPressedKey();
            if (code != -1) {
               r.macro.keyCode = code;
               this.manager.suppressNextActivation(r.macro.modifier, code);
               r.keyBtn.setMessage(Component.literal(r.shortNameOfKey(code)));
               r.listening = false;
               this.manager.save();
               break;
            }
         }
      }

      super.tick();
   }

   private int detectPressedKey() {
      // SDL scancodes below LCONTROL are regular keys; LCONTROL..RGUI are the modifiers
      for (int code = InputConstants.KEY_A; code < InputConstants.KEY_LCONTROL; code++) {
         if (InputConstants.isKeyDown(code)) {
            return code;
         }
      }

      return -1;
   }

   public boolean keyPressed(KeyEvent event) {
      for (UmbcMacroScreen.Row r : this.rows) {
         if (r.listening) {
            int keyCode = event.key();
            if (keyCode != InputConstants.KEY_ESCAPE && keyCode != InputConstants.KEY_BACKSPACE && keyCode != InputConstants.KEY_DELETE) {
               r.macro.keyCode = keyCode;
               this.manager.suppressNextActivation(r.macro.modifier, keyCode);
            } else {
               r.macro.keyCode = -1;
            }

            r.keyBtn.setMessage(Component.literal(r.shortNameOfKey(r.macro.keyCode)));
            r.listening = false;
            this.manager.save();
            return true;
         }
      }

      return super.keyPressed(event);
   }

   public void onClose() {
      this.manager.save();
      Minecraft.getInstance().gui.setScreen(this.parent);
   }

   private class Row {
      private final Macro macro;
      private EditBox command;
      private Button modSel;
      private Button keyBtn;
      private boolean listening;
      private int y;

      Row(Macro macro) {
         this.macro = macro;
      }

      void init(Screen screen, int x, int y, int width) {
         this.y = y;
         this.command = new EditBox(Minecraft.getInstance().font, x + 200, y + 16, width - 290, 20, Component.literal("Command"));
         this.command.setBordered(true);
         this.command.setMaxLength(Integer.MAX_VALUE);
         this.command.setValue(this.macro.command);
         this.command.moveCursorToEnd(false);
         this.command.setSuggestion("Type chat text or /command");
         this.command.setResponder(s -> {
            this.macro.command = s;
            UmbcMacroScreen.this.manager.save();
         });
         UmbcMacroScreen.this.addRenderableWidget(this.command);
         this.modSel = Button.builder(Component.literal(this.label(this.macro.modifier)), b -> {
            this.macro.modifier = this.next(this.macro.modifier);
            this.modSel.setMessage(Component.literal(this.label(this.macro.modifier)));
            UmbcMacroScreen.this.manager.save();
         }).bounds(x, y + 16, 100, 20).build();
         UmbcMacroScreen.this.addRenderableWidget(this.modSel);
         this.keyBtn = Button.builder(Component.literal(this.shortNameOfKey(this.macro.keyCode)), b -> {
            this.listening = true;
            this.keyBtn.setMessage(Component.literal("Press a key..."));
            UmbcMacroScreen.this.clearFocus();
            this.command.setFocused(false);
         }).bounds(x + 110, y + 16, 80, 20).build();
         UmbcMacroScreen.this.addRenderableWidget(this.keyBtn);
         Button del = Button.builder(Component.literal("Delete"), b -> {
            ((UmbcMacroScreen)screen).manager.getMacros().remove(this.macro);
            UmbcMacroScreen.this.manager.save();
            Minecraft.getInstance().gui.setScreen(new UmbcMacroScreen(((UmbcMacroScreen)screen).parent, ((UmbcMacroScreen)screen).manager));
         }).bounds(x + width - 90, y + 16, 80, 20).build();
         UmbcMacroScreen.this.addRenderableWidget(del);
      }

      void render(GuiGraphicsExtractor extractor) {
         int left = UmbcMacroScreen.this.panelLeft + 10;
         int top = this.y;
         int width = UmbcMacroScreen.this.panelWidth - 20;
         extractor.fill(left, top, left + width, top + 52, -1875890128);
         extractor.outline(left, top, width, 52, -10790053);
         extractor.text(UmbcMacroScreen.this.font, Component.literal("Modifier"), this.modSel.getX(), this.y + 5, 16777215);
         extractor.text(UmbcMacroScreen.this.font, Component.literal("Key"), this.keyBtn.getX(), this.y + 5, 16777215);
         extractor.text(UmbcMacroScreen.this.font, Component.literal("Command"), this.command.getX(), this.y + 5, 16777215);
      }

      private String label(Modifier m) {
         if (m == Modifier.NONE) {
            return "Mod: None";
         } else if (m == Modifier.CTRL) {
            return "Mod: Ctrl";
         } else {
            return m == Modifier.ALT ? "Mod: Alt" : "Mod: Shift";
         }
      }

      private Modifier next(Modifier m) {
         if (m == Modifier.NONE) {
            return Modifier.CTRL;
         } else if (m == Modifier.CTRL) {
            return Modifier.ALT;
         } else {
            return m == Modifier.ALT ? Modifier.SHIFT : Modifier.NONE;
         }
      }

      private String shortNameOfKey(int code) {
         if (code == -1) {
            return "Select Keybind";
         }

         return InputConstants.Type.KEYBOARD.getOrCreate(code).getDisplayName().getString();
      }
   }
}
