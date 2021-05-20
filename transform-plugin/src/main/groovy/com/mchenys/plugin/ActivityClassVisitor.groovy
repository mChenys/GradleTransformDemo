package com.mchenys.plugin

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 类访问者
 */
class ActivityClassVisitor extends ClassVisitor implements Opcodes {

    private String mSuperClassName
    private String mAnnotationClassName

    private static final String CLASS_NAME_ACTIVITY = "androidx/appcompat/app/AppCompatActivity"

    private static final String METHOD_NAME_ONCREATE = "onCreate"

    private static final String METHOD_NAME_ONDESTROY = "onDestroy"

    ActivityClassVisitor(int api, ClassWriter writer, String annotationClassName) {
        super(api, writer)
        this.mAnnotationClassName = annotationClassName;
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        mSuperClassName = superName
        println("-------------------- ActivityClassVisitor#visit --------------------name=${name},superName=${superName},mAnnotationClassName=${mAnnotationClassName},signature=$signature")
        super.visit(version, access, name, signature, superName, interfaces)
    }

    //标记是否有注解
    boolean inject

    //访问注解
    @Override
    AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        def targetDescriptor = (mAnnotationClassName == null || mAnnotationClassName.isEmpty()) ? "" : "L${mAnnotationClassName.replaceAll("\\.", "/")};"
        inject = targetDescriptor == descriptor
        println("-------------------- ActivityClassVisitor#visitAnnotation --------------------descriptor=$descriptor,targetDescriptor=$targetDescriptor,inject=$inject")
        return super.visitAnnotation(descriptor, visible)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        println("-------------------- ActivityClassVisitor#visitMethod --------------------name=${name}")
        MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (/*CLASS_NAME_ACTIVITY == mSuperClassName */ inject) { // 这里改成通过注解标记来判断,更加灵活
            if (METHOD_NAME_ONCREATE == name) {
                // 返回处理Activity#onCreate方法的方法分析器
                return new ActivityOnCreateMethodVisitor(api, methodVisitor, access, name, descriptor)
            } else if (METHOD_NAME_ONDESTROY == name) {
                // 返回处理Activity#onDestory的方法分析器
                return new ActivityOnDestroyMethodVisitor(api, methodVisitor, access, name, descriptor)
            }
        }
        return methodVisitor
    }
}