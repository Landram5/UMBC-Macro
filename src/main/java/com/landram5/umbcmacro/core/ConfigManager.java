package com.landram5.umbcmacro.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
   private final Path configDir;
   private final Path file;
   private final Gson gson;

   public ConfigManager(Path configDir) {
      this.configDir = configDir;
      this.file = configDir.resolve("umbcmacro.json");
      this.gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().setPrettyPrinting().create();
   }

   public List<Macro> loadOrDefault() {
      if (Files.exists(this.file)) {
         try (Reader r = Files.newBufferedReader(this.file)) {
            Type t = (new TypeToken<List<Macro>>() {}).getType();
            List<Macro> list = this.gson.fromJson(r, t);
            if (list != null) {
               return list;
            }
         } catch (IOException var7) {
         }
      }

      List<Macro> list = new ArrayList<>();
      list.add(new Macro(Modifier.NONE, -1, ""));
      list.add(new Macro(Modifier.NONE, -1, ""));
      return list;
   }

   public void save(List<Macro> macros) {
      try {
         if (!Files.exists(this.configDir)) {
            Files.createDirectories(this.configDir);
         }

         try (Writer w = Files.newBufferedWriter(this.file)) {
            this.gson.toJson(macros, w);
         }
      } catch (IOException var7) {
      }
   }
}
