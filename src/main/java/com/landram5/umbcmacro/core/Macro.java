package com.landram5.umbcmacro.core;

public class Macro {
   public Modifier modifier;
   public int keyCode;
   public String command;

   public Macro(Modifier modifier, int keyCode, String command) {
      this.modifier = modifier;
      this.keyCode = keyCode;
      this.command = command;
   }
}
