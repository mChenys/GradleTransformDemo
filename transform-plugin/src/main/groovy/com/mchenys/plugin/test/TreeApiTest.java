package com.mchenys.plugin.test;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import groovyjarjarasm.asm.ClassReader;
import groovyjarjarasm.asm.Opcodes;
import groovyjarjarasm.asm.tree.ClassNode;
import groovyjarjarasm.asm.tree.FieldNode;
import groovyjarjarasm.asm.tree.MethodNode;

/**
 * 参考:https://mp.weixin.qq.com/s/dQjsxduUiNrMYH2xhhpmQA
 */
public class TreeApiTest {

    public static void main(String[] args) throws Exception {
        Class clazz = User.class;
        String clazzFilePath = Utils.getClassFilePath(clazz);
        ClassReader classReader = new ClassReader(new FileInputStream(clazzFilePath));
        ClassNode classNode = new ClassNode(Opcodes.ASM5);
        classReader.accept(classNode, 0);

        List<MethodNode> methods = classNode.methods;
        List<FieldNode> fields = classNode.fields;

        System.out.println("methods:");
        for (MethodNode methodNode : methods) {
            System.out.println(methodNode.name + ", " + methodNode.desc);
        }

        System.out.println("fields:");
        for (FieldNode fieldNode : fields) {
            System.out.println(fieldNode.name + ", " + fieldNode.desc);
        }
        /**
         * 打印结果:
         * methods:
         *     <init>, ()V
         *     getName, ()Ljava/lang/String;
         *     getAge, ()I
         * fields:
         *     name, Ljava/lang/String;
         *     age, I
         */
    }

}
