package net.curxxed.dev.agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import net.curxxed.dev.agent.asm.AnnotationVisitor;
import net.curxxed.dev.agent.asm.ClassReader;
import net.curxxed.dev.agent.asm.ClassVisitor;
import net.curxxed.dev.agent.asm.ClassWriter;
import net.curxxed.dev.agent.asm.MethodVisitor;
import net.curxxed.dev.agent.asm.commons.ClassRemapper;
import net.curxxed.dev.agent.asm.commons.Remapper;
import net.curxxed.dev.agent.config.ModEntry;
import net.curxxed.dev.agent.transformer.AccessorConflictPatcher;
import net.curxxed.dev.agent.transformer.EventConstructorPatcher;
import net.curxxed.dev.agent.transformer.MixinAnnotationPatcher;
import net.curxxed.dev.agent.transformer.RuntimeRemapper;

public class AgentBootstrap {
   private static final String MOD_LIST_PROPERTY = "lunar.agent.bootstrap.mods";
   private static final String JAR_PATHS_PROPERTY = "lunar.agent.bootstrap.jar.paths";
   private static final String MIXIN_DESC = "Lorg/spongepowered/asm/mixin/Mixin;";
   private static final int MAX_PRIORITY = 100;

   public static void premain(String args, Instrumentation inst) throws Exception {
      try {
         Path cacheDir = Paths.get(System.getProperty("user.home"), ".lunarclient", "offline", "multiver", "cache");
         if (Files.exists(cacheDir)) {
            Files.walk(cacheDir).filter(p -> p.getFileName().toString().equals("bake.zip")).forEach(p -> {
               try {
                  Files.delete(p);
                  Util.print("Deleted bake cache: " + p);
               } catch (IOException var2x) {
                  Util.print("Failed to delete bake cache: " + p + " - " + var2x);
               }
            });
         }
      } catch (Exception var11) {
         System.out.println("[Mod-Agent] Error clearing bake cache: " + var11);
      }

      Util.print("premain fired! Config: " + args);
      File agentJar = new File(AgentBootstrap.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      Util.print("Agent JAR located at: " + agentJar);
      if (args != null && !args.isBlank()) {
         List<ModEntry> mods = parseConfig(args);
         if (mods.isEmpty()) {
            Util.print("No mods found in agent config.");
         } else {
            for (ModEntry mod : mods) {
               if (mod.property() != null && !mod.property().isBlank()) {
                  System.setProperty(mod.property(), "true");
                  Util.print("Set property: " + mod.property());
               }
            }

            String serialized = buildModListProperty(mods);
            if (!serialized.isBlank()) {
               System.setProperty("lunar.agent.bootstrap.mods", serialized);
               Util.print("Mod list property set: " + serialized);
            }

            StringBuilder jarPaths = new StringBuilder();

            for (ModEntry modx : mods) {
               File f = new File(modx.jar());
               if (f.exists()) {
                  if (!jarPaths.isEmpty()) {
                     jarPaths.append("::");
                  }

                  jarPaths.append(f.getAbsolutePath());
               }
            }

            if (!jarPaths.isEmpty()) {
               System.setProperty("lunar.agent.bootstrap.jar.paths", jarPaths.toString());
               Util.print("JAR paths property set: " + jarPaths);
            }

            Set<String> modClassNames = collectModClassNames(mods);
            Util.print("Tracking " + modClassNames.size() + " mod classes for transformation.");
            Map<String, String> methodMap = new HashMap<>();
            Map<String, String> fieldMap = new HashMap<>();
            loadMappings(methodMap, fieldMap);
            Map<String, String> accessorRenames = buildAccessorRenameMap(mods);
            Util.print("Accessor renames planned: " + accessorRenames.size());
            inst.addTransformer(new RuntimeRemapper(modClassNames, methodMap, fieldMap), true);
            inst.addTransformer(new MixinAnnotationPatcher(modClassNames), true);
            inst.addTransformer(new EventConstructorPatcher(modClassNames), true);
            inst.addTransformer(new AccessorConflictPatcher(modClassNames, accessorRenames), true);
            Util.print("Transformers registered.");
            Thread mixinRegistrar = new Thread(() -> {
               try {
                  Util.print("Waiting for MixinEnvironment..");
                  ClassLoader mixinLoader = null;

                  label243:
                  while (true) {
                     Thread.sleep(50L);
                     Class[] addURL = inst.getAllLoadedClasses();
                     int mixins = addURL.length;
                     int addConfig = 0;

                     while (true) {
                        if (addConfig < mixins) {
                           Class<?> c = addURL[addConfig];
                           if (!c.getName().equals("org.spongepowered.asm.mixin.MixinEnvironment") || c.getClassLoader() == null) {
                              addConfig++;
                              continue;
                           }

                           mixinLoader = c.getClassLoader();
                        }

                        if (mixinLoader != null) {
                           Util.print("Found Mixin loader: " + mixinLoader.getClass().getName());
                           Method addURLx = mixinLoader.getClass().getMethod("addURL", URL.class);
                           Class<?> mixinsx = Class.forName("org.spongepowered.asm.mixin.Mixins", true, mixinLoader);
                           Method addConfigx = mixinsx.getMethod("addConfiguration", String.class);
                           Path patchDir = Files.createTempDirectory("agent-mixin-patch-");
                           patchDir.toFile().deleteOnExit();

                           for (ModEntry modxx : mods) {
                              if (modxx.mixin() != null && !modxx.mixin().isBlank()) {
                                 File jarFile = new File(modxx.jar());
                                 if (jarFile.exists()) {
                                    try (JarFile jf = new JarFile(jarFile)) {
                                       JarEntry jsonEntry = jf.getJarEntry(modxx.mixin());
                                       if (jsonEntry != null) {
                                          try (InputStream is = jf.getInputStream(jsonEntry)) {
                                             String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                                             String patched = stripRefmapField(json);
                                             Files.writeString(patchDir.resolve(modxx.mixin()), patched, StandardCharsets.UTF_8);
                                             Util.print("Stripped refmap from mixin config: " + modxx.mixin());
                                          }
                                       } else {
                                          Util.print("Mixin JSON not found in JAR: " + modxx.mixin());
                                       }

                                       List<String> classPaths = resolveMixinClassPaths(jf, modxx.mixin());

                                       for (String classPath : classPaths) {
                                          JarEntry classEntry = jf.getJarEntry(classPath);
                                          if (classEntry == null) {
                                             Util.print("Mixin class not found in JAR: " + classPath);
                                          } else {
                                             try (InputStream is = jf.getInputStream(classEntry)) {
                                                byte[] original = is.readAllBytes();
                                                byte[] processed = prePatchMixinClass(original, methodMap, fieldMap, accessorRenames);
                                                Path outPath = patchDir.resolve(classPath);
                                                Files.createDirectories(outPath.getParent());
                                                Files.write(outPath, processed);
                                                Util.print("Pre-patched mixin class: " + classPath);
                                             }
                                          }
                                       }

                                       Set<String> alreadyPatched = new HashSet<>(classPaths);

                                       for (String renameKey : accessorRenames.keySet()) {
                                          String owner = renameKey.split("\n")[0];
                                          if (modClassNames.contains(owner)) {
                                             String classPath2 = owner + ".class";
                                             if (!alreadyPatched.contains(classPath2)) {
                                                alreadyPatched.add(classPath2);
                                                JarEntry classEntry = jf.getJarEntry(classPath2);
                                                if (classEntry != null) {
                                                   try (InputStream is = jf.getInputStream(classEntry)) {
                                                      byte[] original = is.readAllBytes();
                                                      byte[] processed = prePatchMixinClass(original, methodMap, fieldMap, accessorRenames);
                                                      Path outPath = patchDir.resolve(classPath2);
                                                      Files.createDirectories(outPath.getParent());
                                                      Files.write(outPath, processed);
                                                      Util.print("Pre-patched accessor interface: " + classPath2);
                                                   } catch (Exception var40) {
                                                      Util.print("Failed to pre-patch accessor: " + classPath2 + " - " + var40);
                                                   }
                                                }
                                             }
                                          }
                                       }
                                    } catch (IOException var42) {
                                       Util.print("Failed to pre-patch mod: " + modxx.jar() + " - " + var42);
                                    }
                                 }
                              }
                           }

                           try (JarFile agentJf = new JarFile(agentJar)) {
                              JarEntry e = agentJf.getJarEntry("mixins.agent.json");
                              if (e != null) {
                                 try (InputStream is = agentJf.getInputStream(e)) {
                                    String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                                    String patched = stripRefmapField(json);
                                    Files.writeString(patchDir.resolve("mixins.agent.json"), patched, StandardCharsets.UTF_8);
                                 }
                              }
                           } catch (Exception var36) {
                           }

                           addURLx.invoke(mixinLoader, patchDir.toUri().toURL());
                           Util.print("Pre-patch dir added first to IchorClassLoader: " + patchDir);
                           addURLx.invoke(mixinLoader, agentJar.toURI().toURL());
                           Util.print("Agent JAR added to IchorClassLoader.");

                           for (ModEntry modx : mods) {
                              File jarFile = new File(modx.jar());
                              if (!jarFile.exists()) {
                                 Util.print("JAR not found, skipping: " + modx.jar());
                              } else {
                                 addURLx.invoke(mixinLoader, jarFile.toURI().toURL());
                                 Util.print("Mod JAR added to IchorClassLoader: " + jarFile.getName());
                              }
                           }

                           addConfigx.invoke(null, "mixins.agent.json");
                           Util.print("Agent bootstrap mixin registered.");

                           for (ModEntry modxx : mods) {
                              if (modxx.mixin() != null && !modxx.mixin().isBlank()) {
                                 addConfigx.invoke(null, modxx.mixin());
                                 Util.print("Mixin config registered: " + modxx.mixin());
                              }
                           }
                           break label243;
                        }
                        break;
                     }
                  }
               } catch (Exception var43) {
                  Util.print("Error in mixin registrar: " + var43);
                  var43.printStackTrace();
               }
            });
            mixinRegistrar.setDaemon(true);
            mixinRegistrar.start();
         }
      } else {
         Util.print("No config file specified, unable to inject.");
      }
   }

   private static byte[] prePatchMixinClass(
      byte[] bytes, final Map<String, String> methodMap, final Map<String, String> fieldMap, Map<String, String> accessorRenames
   ) {
      if (containsSrgNames(bytes)) {
         ClassReader cr = new ClassReader(bytes);
         ClassWriter cw = new ClassWriter(cr, 1);
         cr.accept(new ClassRemapper(cw, new Remapper() {
            public String mapMethodName(String owner, String name, String desc) {
               String m = methodMap.get(name);
               return m != null ? m : name;
            }

            public String mapFieldName(String owner, String name, String desc) {
               String f = fieldMap.get(name);
               return f != null ? f : name;
            }
         }), 0);
         bytes = cw.toByteArray();
      }

      bytes = injectExplicitAccessorValues(bytes);
      if (!accessorRenames.isEmpty()) {
         bytes = AccessorConflictPatcher.applyRenames(bytes, accessorRenames);
      }

      return applyMixinAnnotationFixes(bytes);
   }

   private static byte[] injectExplicitAccessorValues(byte[] bytes) {
      String ACCESSOR = "Lorg/spongepowered/asm/mixin/gen/Accessor;";
      String INVOKER = "Lorg/spongepowered/asm/mixin/gen/Invoker;";
      if (!containsUtf8(bytes, "Accessor") && !containsUtf8(bytes, "Invoker")) {
         return bytes;
      } else {
         ClassReader cr = new ClassReader(bytes);
         ClassWriter cw = new ClassWriter(cr, 0);
         cr.accept(new ClassVisitor(589824, cw) {
            public MethodVisitor visitMethod(int access, final String name, String descriptor, String signature, String[] exceptions) {
               return new MethodVisitor(589824, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                     AnnotationVisitor av = super.visitAnnotation(desc, visible);
                     if ("Lorg/spongepowered/asm/mixin/gen/Accessor;".equals(desc)) {
                        String target = AgentBootstrap.inflectAccessor(name);
                        if (target != null) {
                           Util.print("Injecting @Accessor value=\"" + target + "\" for method: " + name);
                           return new AgentBootstrap.ValueInjectingAnnotationVisitor(av, target);
                        }
                     } else if ("Lorg/spongepowered/asm/mixin/gen/Invoker;".equals(desc)) {
                        String target = AgentBootstrap.inflectInvoker(name);
                        if (target != null) {
                           Util.print("Injecting @Invoker value=\"" + target + "\" for method: " + name);
                           return new AgentBootstrap.ValueInjectingAnnotationVisitor(av, target);
                        }
                     }

                     return av;
                  }
               };
            }
         }, 0);
         return cw.toByteArray();
      }
   }

   private static String inflectAccessor(String methodName) {
      for (String prefix : new String[]{"get", "set", "is"}) {
         if (methodName.startsWith(prefix) && methodName.length() > prefix.length()) {
            String s = methodName.substring(prefix.length());
            return Character.toLowerCase(s.charAt(0)) + s.substring(1);
         }
      }

      return null;
   }

   private static String inflectInvoker(String methodName) {
      for (String prefix : new String[]{"call", "invoke"}) {
         if (methodName.startsWith(prefix) && methodName.length() > prefix.length()) {
            String s = methodName.substring(prefix.length());
            return Character.toLowerCase(s.charAt(0)) + s.substring(1);
         }
      }

      return null;
   }

   private static byte[] applyMixinAnnotationFixes(byte[] bytes) {
      boolean hasMixin = false;

      try {
         ClassReader cr = new ClassReader(bytes);
         final boolean[] found = new boolean[]{false};
         cr.accept(new ClassVisitor(589824) {
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
               if ("Lorg/spongepowered/asm/mixin/Mixin;".equals(desc)) {
                  found[0] = true;
               }

               return null;
            }
         }, 5);
         hasMixin = found[0];
      } catch (Exception var5) {
         return bytes;
      }

      if (!hasMixin) {
         return bytes;
      } else {
         try {
            ClassReader cr = new ClassReader(bytes);
            ClassWriter cw = new ClassWriter(cr, 0);
            cr.accept(new ClassVisitor(589824, cw) {
               public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                  AnnotationVisitor av = super.visitAnnotation(desc, visible);
                  return !"Lorg/spongepowered/asm/mixin/Mixin;".equals(desc) ? av : new AnnotationVisitor(589824, av) {
                     boolean sawRemap = false;
                     boolean sawPriority = false;

                     public void visit(String name, Object value) {
                        if ("remap".equals(name)) {
                           this.sawRemap = true;
                           super.visit(name, false);
                        } else if ("priority".equals(name)) {
                           this.sawPriority = true;
                           int p = (Integer)value;
                           if (p > 100) {
                              Util.print("Pre-patch: clamping mixin priority " + p + " - 100");
                           }

                           super.visit(name, Math.min(p, 100));
                        } else {
                           super.visit(name, value);
                        }
                     }

                     public void visitEnd() {
                        if (!this.sawRemap) {
                           super.visit("remap", false);
                        }

                        if (!this.sawPriority) {
                           super.visit("priority", 100);
                        }

                        super.visitEnd();
                     }
                  };
               }
            }, 0);
            return cw.toByteArray();
         } catch (Exception var4) {
            Util.print("Failed to apply mixin annotation fixes: " + var4);
            return bytes;
         }
      }
   }

   private static List<String> resolveMixinClassPaths(JarFile jf, String mixinJsonName) {
      List<String> result = new ArrayList<>();
      JarEntry entry = jf.getJarEntry(mixinJsonName);
      if (entry == null) {
         return result;
      } else {
         try (InputStream is = jf.getInputStream(entry)) {
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            String pkg = extractJsonString(json, "package");
            String pkgPath = pkg != null ? pkg.replace('.', '/') : "";

            for (String arrayKey : new String[]{"mixins", "client", "server"}) {
               for (String name : extractJsonStringArray(json, arrayKey)) {
                  String namePath = name.replace('.', '/');
                  result.add((pkgPath.isEmpty() ? "" : pkgPath + "/") + namePath + ".class");
               }
            }
         } catch (IOException var17) {
            Util.print("Failed to resolve mixin class paths from: " + mixinJsonName + " - " + var17);
         }

         return result;
      }
   }

   private static Map<String, String> buildAccessorRenameMap(List<ModEntry> mods) {
      final Map<String, String> renames = new HashMap<>();
      String ACCESSOR = "Lorg/spongepowered/asm/mixin/gen/Accessor;";
      String INVOKER = "Lorg/spongepowered/asm/mixin/gen/Invoker;";
      String MIXIN = "Lorg/spongepowered/asm/mixin/Mixin;";

      for (ModEntry mod : mods) {
         File f = new File(mod.jar());
         if (f.exists()) {
            final String prefix = deriveModPrefix(mod.mixin());

            try (JarFile jf = new JarFile(f)) {
               for (JarEntry entry : Collections.list(jf.entries())) {
                  if (entry.getName().endsWith(".class")) {
                     try (InputStream is = jf.getInputStream(entry)) {
                        byte[] bytes = is.readAllBytes();
                        final String internalName = entry.getName().replace(".class", "");
                        ClassReader cr = new ClassReader(bytes);
                        final boolean[] isMixin = new boolean[]{false};
                        cr.accept(new ClassVisitor(589824) {
                           public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                              if ("Lorg/spongepowered/asm/mixin/Mixin;".equals(desc)) {
                                 isMixin[0] = true;
                              }

                              return null;
                           }
                        }, 5);
                        if (isMixin[0]) {
                           cr.accept(
                              new ClassVisitor(589824) {
                                 public MethodVisitor visitMethod(int access, final String name, final String descriptor, String sig, String[] ex) {
                                    return new MethodVisitor(589824) {
                                       public AnnotationVisitor visitAnnotation(String desc, boolean v) {
                                          if (!"Lorg/spongepowered/asm/mixin/gen/Accessor;".equals(desc)
                                             && !"Lorg/spongepowered/asm/mixin/gen/Invoker;".equals(desc)) {
                                             return null;
                                          } else {
                                             String newName = name + "_" + prefix;
                                             String interfaceKey = internalName + "\n" + name + "\n" + descriptor;
                                             renames.put(interfaceKey, newName);
                                             Util.print("Accessor rename planned: " + name + " - " + newName + " in " + internalName);
                                             return null;
                                          }
                                       }
                                    };
                                 }
                              },
                              5
                           );
                        }
                     } catch (Exception var20) {
                     }
                  }
               }
            } catch (IOException var22) {
               Util.print("Failed to scan JAR for accessors: " + mod.jar() + " -- " + var22);
            }
         }
      }

      return renames;
   }

   static String deriveModPrefix(String mixinConfig) {
      if (mixinConfig != null && !mixinConfig.isBlank()) {
         String s = mixinConfig;
         if (mixinConfig.startsWith("mixins.")) {
            s = mixinConfig.substring("mixins.".length());
         }

         if (s.endsWith(".json")) {
            s = s.substring(0, s.length() - ".json".length());
         }

         int dot = s.indexOf(46);
         return (dot > 0 ? s.substring(0, dot) : s).replaceAll("[^a-zA-Z0-9_]", "_");
      } else {
         return "mod";
      }
   }

   private static String stripRefmapField(String json) {
      String r = json.replaceAll(",\\s*\"refmap\"\\s*:\\s*\"[^\"]*\"", "");
      r = r.replaceAll("\"refmap\"\\s*:\\s*\"[^\"]*\"\\s*,", "");
      return r.replaceAll("\"refmap\"\\s*:\\s*\"[^\"]*\"", "");
   }

   private static boolean containsSrgNames(byte[] bytes) {
      for (int i = 0; i < bytes.length - 6; i++) {
         if (bytes[i] == 102) {
            if (bytes[i + 1] == 117 && bytes[i + 2] == 110 && bytes[i + 3] == 99 && bytes[i + 4] == 95) {
               return true;
            }

            if (bytes[i + 1] == 105 && bytes[i + 2] == 101 && bytes[i + 3] == 108 && bytes[i + 4] == 100 && bytes[i + 5] == 95) {
               return true;
            }
         }
      }

      return false;
   }

   private static boolean containsUtf8(byte[] bytes, String needle) {
      byte[] n = needle.getBytes(StandardCharsets.UTF_8);

      label24:
      for (int i = 0; i <= bytes.length - n.length; i++) {
         for (int j = 0; j < n.length; j++) {
            if (bytes[i + j] != n[j]) {
               continue label24;
            }
         }

         return true;
      }

      return false;
   }

   private static String extractJsonString(String json, String key) {
      String search = "\"" + key + "\"";
      int ki = json.indexOf(search);
      if (ki == -1) {
         return null;
      } else {
         int colon = json.indexOf(58, ki + search.length());
         if (colon == -1) {
            return null;
         } else {
            int start = json.indexOf(34, colon + 1);
            if (start == -1) {
               return null;
            } else {
               int end = json.indexOf(34, start + 1);
               return end == -1 ? null : json.substring(start + 1, end);
            }
         }
      }
   }

   private static List<String> extractJsonStringArray(String json, String key) {
      List<String> result = new ArrayList<>();
      String search = "\"" + key + "\"";
      int ki = json.indexOf(search);
      if (ki == -1) {
         return result;
      } else {
         int colon = json.indexOf(58, ki + search.length());
         if (colon == -1) {
            return result;
         } else {
            int arrayStart = json.indexOf(91, colon + 1);
            if (arrayStart == -1) {
               return result;
            } else {
               int arrayEnd = json.indexOf(93, arrayStart + 1);
               if (arrayEnd == -1) {
                  return result;
               } else {
                  for (String token : json.substring(arrayStart + 1, arrayEnd).split(",")) {
                     token = token.trim();
                     if (token.startsWith("\"") && token.endsWith("\"")) {
                        result.add(token.substring(1, token.length() - 1));
                     }
                  }

                  return result;
               }
            }
         }
      }
   }

   public static void agentmain(String args, Instrumentation inst) throws Exception {
      premain(args, inst);
   }

   private static void loadMappings(Map<String, String> methodMap, Map<String, String> fieldMap) {
      parseMappingCsv("/mappings/methods.csv", methodMap);
      parseMappingCsv("/mappings/fields.csv", fieldMap);
      Util.print("Loaded " + methodMap.size() + " method mappings, " + fieldMap.size() + " field mappings.");
   }

   private static void parseMappingCsv(String resource, Map<String, String> target) {
      try {
         label62: {
            try (InputStream is = AgentBootstrap.class.getResourceAsStream(resource)) {
               if (is != null) {
                  BufferedReader r = new BufferedReader(new InputStreamReader(is));
                  r.readLine();

                  String line;
                  while ((line = r.readLine()) != null) {
                     String[] parts = line.split(",");
                     if (parts.length >= 2) {
                        target.put(parts[0].trim(), parts[1].trim());
                     }
                  }
                  break label62;
               }

               Util.print("Mappings not found: " + resource);
            }

            return;
         }
      } catch (Exception var8) {
         Util.print("Failed to load mappings " + resource + ": " + var8);
      }
   }

   private static String buildModListProperty(List<ModEntry> mods) {
      StringBuilder sb = new StringBuilder();

      for (ModEntry mod : mods) {
         File f = new File(mod.jar());
         if (f.exists()) {
            try (JarFile jf = new JarFile(f)) {
               for (JarEntry entry : Collections.list(jf.entries())) {
                  if (entry.getName().endsWith(".class")) {
                     try (InputStream is = jf.getInputStream(entry)) {
                        byte[] bytes = is.readAllBytes();
                        ClassReader cr = new ClassReader(bytes);
                        String[] modClass = new String[]{null};
                        final String[] initMethod = new String[]{null};
                        boolean[] isAgentMod = new boolean[]{false};
                        cr.accept(new ClassVisitor(589824) {
                           String className;

                           public void visit(int v, int a, String name, String sig, String sup, String[] i) {
                              this.className = name;
                           }

                           public MethodVisitor visitMethod(int a, final String name, String desc, String sig, String[] ex) {
                              return !desc.contains("net/minecraftforge/fml/common/event/FMLInitializationEvent") ? null : new MethodVisitor(589824) {
                                 public AnnotationVisitor visitAnnotation(String d, boolean v) {
                                    if (d.contains("EventHandler")) {
                                       initMethod[0] = name;
                                    }

                                    return null;
                                 }
                              };
                           }
                        }, 5);
                        if (modClass[0] != null) {
                           if (!sb.isEmpty()) {
                              sb.append(",");
                           }

                           sb.append(modClass[0])
                              .append("|")
                              .append(initMethod[0] != null ? initMethod[0] : "")
                              .append("|")
                              .append(mod.property() != null ? mod.property() : "")
                              .append("|")
                              .append(isAgentMod[0] ? "1" : "0");
                           Util.print("Discovered " + (isAgentMod[0] ? "@AgentMod" : "@Mod") + ": " + modClass[0] + " (init: " + initMethod[0] + ")");
                        }
                     }
                  }
               }
            } catch (IOException var18) {
               Util.print("Failed to scan JAR: " + mod.jar() + " - " + var18);
            }
         }
      }

      return sb.toString();
   }

   private static Set<String> collectModClassNames(List<ModEntry> mods) {
      Set<String> names = new HashSet<>();

      for (ModEntry mod : mods) {
         File f = new File(mod.jar());
         if (f.exists()) {
            try (JarFile jf = new JarFile(f)) {
               jf.stream().filter(ex -> ex.getName().endsWith(".class")).map(ex -> ex.getName().replace(".class", "")).forEach(names::add);
            } catch (IOException var10) {
               Util.print("Failed to index JAR: " + mod.jar() + " - " + var10);
            }
         }
      }

      return names;
   }

   private static List<ModEntry> parseConfig(String configPath) {
      List<ModEntry> mods = new ArrayList<>();

      try {
         String content = Files.readString(new File(configPath).toPath());
         String[] blocks = content.split("\\{");

         for (int i = 1; i < blocks.length; i++) {
            String block = blocks[i];
            if (block.contains("jar")) {
               String jar = extractValue(block, "jar");
               String mixin = extractValue(block, "mixin");
               String property = extractValue(block, "property");
               if (jar != null) {
                  mods.add(new ModEntry(jar, mixin, property));
               }
            }
         }
      } catch (IOException var9) {
         Util.print("Failed to read config: " + var9);
      }

      return mods;
   }

   private static String extractValue(String block, String key) {
      String search = "\"" + key + "\"";
      int keyIdx = block.indexOf(search);
      if (keyIdx == -1) {
         return null;
      } else {
         int colon = block.indexOf(":", keyIdx);
         if (colon == -1) {
            return null;
         } else {
            int start = block.indexOf("\"", colon + 1);
            if (start == -1) {
               return null;
            } else {
               int end = block.indexOf("\"", start + 1);
               return end == -1 ? null : block.substring(start + 1, end);
            }
         }
      }
   }

   private static class ValueInjectingAnnotationVisitor extends AnnotationVisitor {
      private final String targetName;
      private boolean sawValue = false;

      ValueInjectingAnnotationVisitor(AnnotationVisitor av, String targetName) {
         super(589824, av);
         this.targetName = targetName;
      }

      public void visit(String name, Object value) {
         if ("value".equals(name)) {
            this.sawValue = true;
         }

         super.visit(name, value);
      }

      public void visitEnd() {
         if (!this.sawValue) {
            super.visit("value", this.targetName);
         }

         super.visitEnd();
      }
   }
}
