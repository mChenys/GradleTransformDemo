package com.mchenys.plugin

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter

/**
 * 方法分析器
 * AdviceAdapter 对methodVisitor进行了扩展， 能让我们更加轻松的进行方法分析
 */
class ActivityOnCreateMethodVisitor extends AdviceAdapter {

    protected ActivityOnCreateMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name, String descriptor) {
        super(api, methodVisitor, access, name, descriptor)
    }

    // 方法进入时触发
    @Override
    protected void onMethodEnter() {
        println("-------------------- ActivityOnCreateMethodVisitor#onMethodEnter --------------------")
        visitLdcInsn("cys")
        visitLdcInsn("---------------onCreate--------------------")
        visitMethodInsn(INVOKESTATIC, "android/util/Log", "e", "(Ljava/lang/String;Ljava/lang/String;)I", false);
        visitInsn(POP);
    }
}