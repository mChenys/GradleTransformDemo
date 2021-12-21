package com.mchenys.plugin.test;

import static groovyjarjarasm.asm.Opcodes.*;

import groovyjarjarasm.asm.ClassVisitor;
import groovyjarjarasm.asm.FieldVisitor;
import groovyjarjarasm.asm.MethodVisitor;
import groovyjarjarasm.asm.Opcodes;

//https://mp.weixin.qq.com/s/dQjsxduUiNrMYH2xhhpmQA
public class AddTimerClassVisitor extends ClassVisitor {

    public AddTimerClassVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    private String mOwner;

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        mOwner = name;
    }

//    @Override
//    public void visitEnd() {
        // 我们给原类添加了一个 timer 字段，访问修饰符是 public static，并且其类型是 J 也就是 long 类型。
//        FieldVisitor fv = cv.visitField(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "timer",
//                "J", null, null);
//        if (fv != null) {
//            fv.visitEnd();
//        }
//        // 我们给原类添加了一个printHello方法,访问修饰符是public static,返回值void,参数无
//        MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "printHello", "()V", null, null);
//        if (mv != null) {
//            mv.visitEnd();
//        }
//        cv.visitEnd();


//    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

        MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);

        if (methodVisitor != null && !name.equals("<init>")) {
            MethodVisitor newMethodVisitor = new MethodVisitor(api, methodVisitor) {
                @Override
                public void visitCode() {
                    mv.visitCode();

                    mv.visitFieldInsn(GETSTATIC, mOwner, "timer", "J");
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/System",
                            "currentTimeMillis", "()J");
                    mv.visitInsn(LSUB);
                    mv.visitFieldInsn(PUTSTATIC, mOwner, "timer", "J");

                }

                @Override
                public void visitInsn(int opcode) {

                    if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
                        mv.visitFieldInsn(GETSTATIC, mOwner, "timer", "J");
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System",
                                "currentTimeMillis", "()J");
                        mv.visitInsn(LADD);
                        mv.visitFieldInsn(PUTSTATIC, mOwner, "timer", "J");
                    }
                    mv.visitInsn(opcode);

                }
            };
            return newMethodVisitor;
        }

        return methodVisitor;
    }

    @Override
    public void visitEnd() {

        FieldVisitor fv = cv.visitField(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "timer",
                "J", null, null);
        if (fv != null) {
            fv.visitEnd();
        }
        cv.visitEnd();
    }
}