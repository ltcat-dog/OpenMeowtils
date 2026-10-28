package net.curxxed.dev.agent.transformer;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Set;
import net.curxxed.dev.agent.Util;
import net.curxxed.dev.agent.asm.ClassReader;
import net.curxxed.dev.agent.asm.ClassVisitor;
import net.curxxed.dev.agent.asm.ClassWriter;
import net.curxxed.dev.agent.asm.MethodVisitor;

public class EventConstructorPatcher implements ClassFileTransformer {
   private static final String FORGE_EVENT = "net/minecraftforge/fml/common/eventhandler/Event";
   private final Set<String> modClassNames;

   public EventConstructorPatcher(Set<String> modClassNames) {
      this.modClassNames = modClassNames;
   }

   @Override
   public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain domain, byte[] classfileBuffer) {
      if (className != null && this.modClassNames.contains(className)) {
         ClassReader cr = new ClassReader(classfileBuffer);
         if (!this.extendsForgeEvent(cr)) {
            return null;
         } else if (this.hasNoArgsConstructor(cr)) {
            return null;
         } else {
            ClassWriter cw = new ClassWriter(cr, 2) {
               protected String getCommonSuperClass(String type1, String type2) {
                  try {
                     return super.getCommonSuperClass(type1, type2);
                  } catch (Throwable var4) {
                     return "java/lang/Object";
                  }
               }
            };
            cr.accept(new EventConstructorPatcher.NoArgsConstructorInjector(cw, cr.getSuperName()), 0);
            byte[] patched = cw.toByteArray();
            Util.print("Injected no-args constructor into event: " + className);
            return patched;
         }
      } else {
         return null;
      }
   }

   private boolean extendsForgeEvent(ClassReader cr) {
      String superName = cr.getSuperName();
      if (superName == null) {
         return false;
      } else {
         return superName.equals("net/minecraftforge/fml/common/eventhandler/Event") ? true : superName.startsWith("net/minecraftforge/");
      }
   }

   private boolean hasNoArgsConstructor(ClassReader cr) {
      final boolean[] found = new boolean[]{false};
      cr.accept(new ClassVisitor(589824) {
         public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            if ("<init>".equals(name) && "()V".equals(descriptor)) {
               found[0] = true;
            }

            return null;
         }
      }, 5);
      return found[0];
   }

   private static class NoArgsConstructorInjector extends ClassVisitor {
      private final String superName;
      private String className;

      NoArgsConstructorInjector(ClassVisitor cv, String superName) {
         super(589824, cv);
         this.superName = superName;
      }

      public void visit(int version, int access, String name, String signature, String superN, String[] interfaces) {
         this.className = name;
         super.visit(version, access, name, signature, superN, interfaces);
      }

      public void visitEnd() {
         MethodVisitor mv = super.visitMethod(4097, "<init>", "()V", null, null);
         mv.visitCode();
         mv.visitVarInsn(25, 0);
         mv.visitMethodInsn(183, this.superName, "<init>", "()V", false);
         mv.visitInsn(177);
         mv.visitMaxs(1, 1);
         mv.visitEnd();
         super.visitEnd();
      }
   }
}
