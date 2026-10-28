package net.curxxed.dev.agent.transformer;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import net.curxxed.dev.agent.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   targets = {"net.minecraft.client.Minecraft"},
   remap = false
)
public class AgentMixinBootstrap {
   private static boolean agentInitialized = false;

   @Inject(
      method = {"startGame"},
      at = {@At("RETURN")},
      remap = false,
      require = 0
   )
   private void agentBootstrap(CallbackInfo ci) {
      if (!agentInitialized) {
         agentInitialized = true;
         String modList = System.getProperty("lunar.agent.bootstrap.mods");
         if (modList != null && !modList.isBlank()) {
            Util.print("First tick, bootstrapping mods..");
            ClassLoader genesisLoader = this.getClass().getClassLoader();
            ClassLoader modLoader = buildModLoader(genesisLoader);

            for (String entry : modList.split(",")) {
               String[] parts = entry.split("\\|", -1);
               if (parts.length >= 1 && !parts[0].isBlank()) {
                  String className = parts[0];
                  String initMethod = parts.length > 1 ? parts[1] : "";
                  String property = parts.length > 2 ? parts[2] : "";

                  try {
                     initMod(className, initMethod, property, modLoader);
                  } catch (Throwable var14) {
                     Util.print("Failed to init mod: " + className + " - " + var14);
                     var14.printStackTrace();
                  }
               }
            }
         } else {
            Util.print("No mods to bootstrap.");
         }
      }
   }

   private static ClassLoader buildModLoader(ClassLoader parent) {
      Util.print("buildModLoader called, jarPaths: " + System.getProperty("lunar.agent.bootstrap.jar.paths"));
      String jarPathsProp = System.getProperty("lunar.agent.bootstrap.jar.paths");
      if (jarPathsProp != null && !jarPathsProp.isBlank()) {
         try {
            String[] paths = jarPathsProp.split("::");
            URL[] urls = new URL[paths.length];

            for (int i = 0; i < paths.length; i++) {
               urls[i] = new File(paths[i]).toURI().toURL();
               Util.print("Mod loader URL: " + urls[i]);
            }

            return new URLClassLoader(urls, parent);
         } catch (Exception var5) {
            Util.print("Failed to build mod loader: " + var5);
            return parent;
         }
      } else {
         Util.print("No JAR paths registered, mod loading will fail.");
         return parent;
      }
   }

   private static void initMod(String className, String initMethod, String property, ClassLoader modLoader) throws Exception {
      if (!property.isBlank() && !Boolean.getBoolean(property)) {
         Util.print("Skipping " + className + ", not in injected environment.");
      } else {
         String dotName = className.replace('/', '.');
         Class<?> modClass = Class.forName(dotName, true, modLoader);
         Util.print("Loaded: " + dotName + " via " + modClass.getClassLoader());
         Object instance = modClass.getDeclaredConstructor().newInstance();
         Util.print("Instantiated: " + dotName);
         if (!initMethod.isBlank()) {
            Class<?> eventClass = Class.forName("net.minecraftforge.fml.common.event.FMLInitializationEvent", true, modLoader);
            Method m = modClass.getDeclaredMethod(initMethod, eventClass);
            m.setAccessible(true);
            m.invoke(instance, null);
            Util.print("Called init: " + initMethod + " on " + dotName);
         } else {
            Util.print("No init method, constructor-only init for " + dotName);
         }
      }
   }
}
