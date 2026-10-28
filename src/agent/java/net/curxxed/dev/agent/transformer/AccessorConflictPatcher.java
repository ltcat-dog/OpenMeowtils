package net.curxxed.dev.agent.transformer;

import java.lang.instrument.ClassFileTransformer;
import java.nio.charset.StandardCharsets;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Set;
import net.curxxed.dev.agent.Util;
import net.curxxed.dev.agent.asm.ClassReader;
import net.curxxed.dev.agent.asm.ClassVisitor;
import net.curxxed.dev.agent.asm.ClassWriter;
import net.curxxed.dev.agent.asm.MethodVisitor;

public class AccessorConflictPatcher implements ClassFileTransformer {
   private final Set<String> modClassNames;
   private final Map<String, String> renames;

   public AccessorConflictPatcher(Set<String> modClassNames, Map<String, String> renames) {
      this.modClassNames = modClassNames;
      this.renames = renames;
   }

   @Override
   public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain domain, byte[] classfileBuffer) {
      if (className == null) {
         return null;
      } else if (this.renames.isEmpty()) {
         return null;
      } else if (!this.modClassNames.contains(className)) {
         return null;
      } else {
         boolean relevant = false;

         for (String key : this.renames.keySet()) {
            if (key.startsWith(className + "\n")) {
               relevant = true;
               break;
            }
         }

         if (!relevant) {
            for (String keyx : this.renames.keySet()) {
               String methodName = keyx.split("\n")[1];
               if (containsUtf8(classfileBuffer, methodName)) {
                  relevant = true;
                  break;
               }
            }
         }

         if (!relevant) {
            return null;
         } else {
            byte[] result = applyRenames(classfileBuffer, this.renames);
            Util.print("AccessorConflictPatcher applied renames in: " + className);
            return result;
         }
      }
   }

   public static byte[] applyRenames(byte[] bytes, final Map<String, String> renames) {
      ClassReader cr = new ClassReader(bytes);
      ClassWriter cw = new ClassWriter(cr, 0);
      cr.accept(new ClassVisitor(589824, cw) {
         private String currentOwner;

         public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.currentOwner = name;
            super.visit(version, access, name, signature, superName, interfaces);
         }

         public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            String key = this.currentOwner + "\n" + name + "\n" + descriptor;
            String newName = renames.get(key);
            if (newName != null) {
               Util.print("Renaming accessor declaration: " + name + " - " + newName + " in " + this.currentOwner);
               return super.visitMethod(access, newName, descriptor, signature, exceptions);
            } else {
               return new MethodVisitor(589824, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                  public void visitMethodInsn(int opcode, String owner, String mName, String descriptorx, boolean isInterface) {
                     String callKey = owner + "\n" + mName + "\n" + descriptorx;
                     String mappedName = renames.get(callKey);
                     if (mappedName != null) {
                        Util.print("Renaming accessor call site: " + mName + " to " + mappedName + " (owner: " + owner + ")");
                        super.visitMethodInsn(opcode, owner, mappedName, descriptorx, isInterface);
                     } else {
                        super.visitMethodInsn(opcode, owner, mName, descriptorx, isInterface);
                     }
                  }
               };
            }
         }
      }, 0);
      return cw.toByteArray();
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
}
