package com.mchenys.plugin

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * 字节码插桩
 * 参考:
 * https://blog.csdn.net/milovetingting/article/details/104507204/
 * https://www.jb51.net/article/132709.htm
 * https://github.com/mChenys/Luffy
 */
class ASMPlugin extends Transform implements Plugin<Project> {
    def NAME = "AMSPlugin"
    def mAnnotationClassName

    @Override
    String getName() {
        return NAME
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void apply(Project project) {

        project.extensions.create("asm", Asm) // 添加Asm闭包,关联扩展属性
        def android = project.extensions.getByType(AppExtension)
        android.registerTransform(this) // 添加当前的Transform

        project.afterEvaluate{
            // 配置完成后,获取自定义属性中的annotationClassName
            mAnnotationClassName = project.asm.annotationClassName
        }

    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        println('--------------------ASMPlugin transform start--------------------')
        def startTime = System.currentTimeMillis()

        def inputs = transformInvocation.inputs
        def outputProvider = transformInvocation.outputProvider

        //删除旧的输出
        if (null != outputProvider) {
            outputProvider.deleteAll()
        }

        //遍历inputs
        inputs.each { input ->
            //遍历directoryInputs
            input.directoryInputs.each {
                directoryInput -> handleDirectoryInput(directoryInput, outputProvider)
            }
            //遍历jarInputs
            input.jarInputs.each {
                jarInput -> handleJarInput(jarInput, outputProvider)
            }
        }

        println('-------------------- ASMPlugin transform end --------------------')
        def time = (System.currentTimeMillis() - startTime) / 1000
        println("ASMPlugin cost $time s")
    }

    /**
     * 处理目录下的class文件
     * @param directoryInput
     * @param outputProvider
     */
    void handleDirectoryInput(DirectoryInput directoryInput, TransformOutputProvider outputProvider) {
        //是否为目录
        if (directoryInput.file.isDirectory()) {
            //列出目录所有文件（包含子文件夹，子文件夹内文件）
            directoryInput.file.eachFileRecurse {
                file ->
                    def name = file.name
                    if (isClassFile(name)) {
                        println("-------------------- handle class file:<$name> --------------------file.parentFile.absolutePath:${file.parentFile.absolutePath}")
                        // 获取一个分析器,去读class文件
                        ClassReader classReader = new ClassReader(file.bytes)
                        // 获取一个插桩用的ClassWriter
                        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES)
                        // 创建类访问者
                        ClassVisitor classVisitor = new ActivityClassVisitor(Opcodes.ASM7, classWriter, mAnnotationClassName)
                        // 注册一个类的访问者以及class的操作对象
                        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
                        // 插桩完成后获取最终结果
                        byte[] bytes = classWriter.toByteArray()
                        // 覆盖源文件
                        FileOutputStream fileOutputStream = new FileOutputStream(new File(file.parentFile, name))
                        fileOutputStream.write(bytes)
                        fileOutputStream.close()
                    }
            }
        }
        // 定义输出目录
        def dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
        // 拷贝到输出目录
        FileUtils.copyDirectory(directoryInput.file, dest)
    }

    /**
     * 处理Jar中的class文件
     * @param jarInput
     * @param outputProvider
     */
    void handleJarInput(JarInput jarInput, TransformOutputProvider outputProvider) {
        /*if (jarInput.file.getAbsolutePath().endsWith(".jar")) {
            //重名名输出文件,因为可能同名,会覆盖
            def jarName = jarInput.name
            def md5Name = DigestUtils.md5Hex(jarInput.file.absolutePath)
            if (jarName.endsWith(".jar")) {
                jarName = jarName.substring(0, jarName.length() - 4)
            }
            JarFile jarFile = new JarFile(jarInput.file)
            Enumeration enumeration = jarFile.entries()
            File tempFile = new File(jarInput.file.parent + File.separator + "temp.jar")
            //避免上次的缓存被重复插入
            if (tempFile.exists()) {
                tempFile.delete()
            }
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tempFile))
            //保存
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = enumeration.nextElement()
                String entryName = jarEntry.name
                ZipEntry zipEntry = new ZipEntry(entryName)
                InputStream inputStream = jarFile.getInputStream(zipEntry)
                if (isClassFile(entryName)) {
                    println("-------------------- handle jar file:<$entryName> --------------------")
                    jarOutputStream.putNextEntry(zipEntry)
                    ClassReader classReader = new ClassReader(IOUtils.toByteArray(inputStream))
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                    ClassVisitor classVisitor = new ActivityClassVisitor(classWriter)
                    classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
                    byte[] bytes = classWriter.toByteArray()
                    jarOutputStream.write(bytes)
                } else {
                    jarOutputStream.putNextEntry(zipEntry)
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))
                }
                jarOutputStream.closeEntry()
            }
            jarOutputStream.close()
            jarFile.close()
            def dest = outputProvider.getContentLocation(jarName + "_" + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
            FileUtils.copyFile(tempFile, dest)
            tempFile.delete()
        }*/
        // jar包不处理
        def jarName = jarInput.name
        def md5Name = DigestUtils.md5Hex(jarInput.file.absolutePath)
        def dest = outputProvider.getContentLocation(jarName + "_" + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
        FileUtils.copyFile(jarInput.file, dest)
        jarInput.file
    }

    /**
     * 判断是否为需要处理class文件
     * @param name
     * @return
     */
    static boolean isClassFile(String name) {
        return (name.endsWith(".class") && !name.startsWith("R\$")
                && "R.class" != name && "BuildConfig.class" != name && name.contains("Activity"))
    }
}