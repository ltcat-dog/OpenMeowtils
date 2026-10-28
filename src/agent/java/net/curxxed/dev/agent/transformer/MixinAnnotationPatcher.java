package net.curxxed.dev.agent.transformer;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Set;
import net.curxxed.dev.agent.Util;
import net.curxxed.dev.agent.asm.AnnotationVisitor;
import net.curxxed.dev.agent.asm.ClassReader;
import net.curxxed.dev.agent.asm.ClassVisitor;
import net.curxxed.dev.agent.asm.ClassWriter;
import net.curxxed.dev.agent.asm.FieldVisitor;
import net.curxxed.dev.agent.asm.MethodVisitor;

public class MixinAnnotationPatcher implements ClassFileTransformer {
   private static final String MIXIN_DESC = "Lorg/spongepowered/asm/mixin/Mixin;";
   private static final String SHADOW_DESC = "Lorg/spongepowered/asm/mixin/Shadow;";
   private static final String ACCESSOR_DESC = "Lorg/spongepowered/asm/mixin/gen/Accessor;";
   private static final String INVOKER_DESC = "Lorg/spongepowered/asm/mixin/gen/Invoker;";
   private static final String INJECT_DESC = "Lorg/spongepowered/asm/mixin/injection/Inject;";
   private static final String REDIRECT_DESC = "Lorg/spongepowered/asm/mixin/injection/Redirect;";
   private static final String MODIFY_ARG = "Lorg/spongepowered/asm/mixin/injection/ModifyArg;";
   private static final String MODIFY_ARGS = "Lorg/spongepowered/asm/mixin/injection/ModifyArgs;";
   private static final String MODIFY_CONST = "Lorg/spongepowered/asm/mixin/injection/ModifyConstant;";
   private static final String MODIFY_VAR = "Lorg/spongepowered/asm/mixin/injection/ModifyVariable;";
   private static final String OVERWRITE = "Lorg/spongepowered/asm/mixin/Overwrite;";
   private static final Set<String> REMAP_TARGETS = Set.of(
      "Lorg/spongepowered/asm/mixin/Shadow;",
      "Lorg/spongepowered/asm/mixin/gen/Accessor;",
      "Lorg/spongepowered/asm/mixin/gen/Invoker;",
      "Lorg/spongepowered/asm/mixin/injection/Inject;",
      "Lorg/spongepowered/asm/mixin/injection/Redirect;",
      "Lorg/spongepowered/asm/mixin/injection/ModifyArg;",
      "Lorg/spongepowered/asm/mixin/injection/ModifyArgs;",
      "Lorg/spongepowered/asm/mixin/injection/ModifyConstant;",
      "Lorg/spongepowered/asm/mixin/injection/ModifyVariable;",
      "Lorg/spongepowered/asm/mixin/Overwrite;"
   );
   private static final int MAX_PRIORITY = 100;
   private final Set<String> modClassNames;

   public MixinAnnotationPatcher(Set<String> modClassNames) {
      this.modClassNames = modClassNames;
   }

   @Override
   public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain domain, byte[] classfileBuffer) {
      if (className != null && this.modClassNames.contains(className)) {
         ClassReader cr = new ClassReader(classfileBuffer);
         if (!this.hasMixinAnnotation(cr)) {
            return null;
         } else {
            ClassWriter cw = new ClassWriter(cr, 0);
            cr.accept(new MixinAnnotationPatcher.MixinPatchingVisitor(cw), 0);
            byte[] patched = cw.toByteArray();
            Util.print("Patched mixin annotations in: " + className);
            return patched;
         }
      } else {
         return null;
      }
   }

   private boolean hasMixinAnnotation(ClassReader cr) {
      final boolean[] found = new boolean[]{false};
      cr.accept(new ClassVisitor(589824) {
         public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if ("Lorg/spongepowered/asm/mixin/Mixin;".equals(desc)) {
               found[0] = true;
            }

            return null;
         }
      }, 5);
      return found[0];
   }

   private static class MixinClassAnnotationVisitor extends AnnotationVisitor {
      private boolean sawRemap = false;
      private boolean sawPriority = false;

      MixinClassAnnotationVisitor(AnnotationVisitor av) {
         super(589824, av);
      }

      public void visit(String name, Object value) {
         if ("remap".equals(name)) {
            this.sawRemap = true;
            super.visit(name, false);
         } else if ("priority".equals(name)) {
            this.sawPriority = true;
            int p = (Integer)value;
            if (p > 100) {
               Util.print("Clamping mixin priority from " + p + " to 100");
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
   }

   private static class MixinPatchingVisitor extends ClassVisitor {
      MixinPatchingVisitor(ClassVisitor cv) {
         super(589824, cv);
      }

      public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
         AnnotationVisitor av = super.visitAnnotation(desc, visible);
         return (AnnotationVisitor)("Lorg/spongepowered/asm/mixin/Mixin;".equals(desc) ? new MixinAnnotationPatcher.MixinClassAnnotationVisitor(av) : av);
      }

      public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
         return new FieldVisitor(589824, super.visitField(access, name, descriptor, signature, value)) {
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
               AnnotationVisitor av = super.visitAnnotation(desc, visible);
               return (AnnotationVisitor)(MixinAnnotationPatcher.REMAP_TARGETS.contains(desc) ? new MixinAnnotationPatcher.RemapFalseVisitor(av) : av);
            }
         };
      }

      public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
         return new MethodVisitor(589824, super.visitMethod(access, name, descriptor, signature, exceptions)) {
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
               AnnotationVisitor av = super.visitAnnotation(desc, visible);
               return (AnnotationVisitor)(MixinAnnotationPatcher.REMAP_TARGETS.contains(desc) ? new MixinAnnotationPatcher.RemapFalseVisitor(av) : av);
            }
         };
      }
   }

   private static class RemapFalseVisitor extends AnnotationVisitor {
      private boolean sawRemap = false;

      RemapFalseVisitor(AnnotationVisitor av) {
         super(589824, av);
      }

      public void visit(String name, Object value) {
         if ("remap".equals(name)) {
            this.sawRemap = true;
            super.visit(name, false);
         } else {
            super.visit(name, value);
         }
      }

      public void visitEnd() {
         if (!this.sawRemap) {
            super.visit("remap", false);
         }

         super.visitEnd();
      }
   }
}
