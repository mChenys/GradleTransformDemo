package com.mchenys.plugin.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import groovyjarjarasm.asm.ClassReader;
import groovyjarjarasm.asm.ClassWriter;
import groovyjarjarasm.asm.Opcodes;

public class ClassWriterTest {


    public static void main(String[] args) throws Exception {
       /**
        *   Class clazz = C.class;
        *   String clazzFilePath = Utils.getClassFilePath(clazz);
        *   ClassReader classReader = new ClassReader(new FileInputStream(clazzFilePath));
        *
        *   ClassWriter classWriter = new ClassWriter(0);
        *   classReader.accept(classWriter, 0);
        *
        *   // 写入文件
        *   byte[] bytes = classWriter.toByteArray();
        *   FileOutputStream fos = new FileOutputStream("/Users/chenyousheng/Desktop/copyed.class");
        *   fos.write(bytes);
        *   fos.flush();
        *   fos.close();
        */

        Class clazz = C.class;
        String clazzFilePath = Utils.getClassFilePath(clazz);
        ClassReader classReader = new ClassReader(new FileInputStream(clazzFilePath));

        ClassWriter classWriter = new ClassWriter(0);
        // 这里通过AddTimerClassVisitor对ClassWriter进行包装了一层,相当于代理类
        AddTimerClassVisitor addTimerClassVisitor = new AddTimerClassVisitor(Opcodes.ASM5, classWriter);
        classReader.accept(addTimerClassVisitor, 0);

        // 写入文件
        byte[] bytes = classWriter.toByteArray();
        FileOutputStream fos = new FileOutputStream("/Users/chenyousheng/Desktop/copyed.class");
        fos.write(bytes);
        fos.flush();
        fos.close();

    }
}