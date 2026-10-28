package net.curxxed.dev.agent.transformer;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Set;
import net.curxxed.dev.agent.Util;
import net.curxxed.dev.agent.asm.ClassReader;
import net.curxxed.dev.agent.asm.ClassWriter;
import net.curxxed.dev.agent.asm.commons.ClassRemapper;
import net.curxxed.dev.agent.asm.commons.Remapper;

public class RuntimeRemapper implements ClassFileTransformer {
   private final Map<String, String> methodMap;
   private final Map<String, String> fieldMap;
   private final Set<String> modClassNames;

   public RuntimeRemapper(Set<String> modClassNames, Map<String, String> methodMap, Map<String, String> fieldMap) {
      this.modClassNames = modClassNames;
      this.methodMap = methodMap;
      this.fieldMap = fieldMap;
      Util.print("RuntimeRemapper ready: " + methodMap.size() + " method mappings, " + fieldMap.size() + " field mappings.");
   }

   @Override
   public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain domain, byte[] classfileBuffer) {
      if (className == null || !this.modClassNames.contains(className)) {
         return null;
      } else if (!this.containsSrgNames(classfileBuffer)) {
         return null;
      } else {
         ClassReader cr = new ClassReader(classfileBuffer);
         ClassWriter cw = new ClassWriter(cr, 1);
         cr.accept(new ClassRemapper(cw, new RuntimeRemapper.SrgRemapper()), 0);
         byte[] remapped = cw.toByteArray();
         Util.print("Remapped SRG to MCP: " + className);
         return remapped;
      }
   }

   private boolean containsSrgNames(byte[] bytes) {
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

   private class SrgRemapper extends Remapper {
      public String mapMethodName(String owner, String name, String descriptor) {
         String m = RuntimeRemapper.this.methodMap.get(name);
         return m != null ? m : name;
      }

      public String mapFieldName(String owner, String name, String descriptor) {
         String f = RuntimeRemapper.this.fieldMap.get(name);
         return f != null ? f : name;
      }
   }
}
