package com.mchenys.plugin.test;

import java.io.FileInputStream;

import groovyjarjarasm.asm.ClassReader;
import groovyjarjarasm.asm.ClassVisitor;
import groovyjarjarasm.asm.FieldVisitor;
import groovyjarjarasm.asm.MethodVisitor;
import groovyjarjarasm.asm.Opcodes;

public class VisitApiTest {
    public static void main(String[] args) throws Exception {
        Class clazz = User.class;
        String clazzFilePath = Utils.getClassFilePath(clazz);
        ClassReader classReader = new ClassReader(new FileInputStream(clazzFilePath));
        ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM5) {
            @Override
            public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                System.out.println("visit field:" + name + " , desc = " + descriptor);
                return super.visitField(access, name, descriptor, signature, value);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                System.out.println("visit method:" + name + " , desc = " + descriptor);
                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }
        };
        classReader.accept(classVisitor, 0);

        /**
         * 输出结果:
         * visit field:name , desc = Ljava/lang/String;
         * visit field:age , desc = I
         * visit method:<init> , desc = ()V
         * visit method:getName , desc = ()Ljava/lang/String;
         * visit method:getAge , desc = ()I
         */
    }
}