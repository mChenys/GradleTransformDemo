 package com.mchenys.plugin

 import com.android.build.gradle.AppExtension
 import org.gradle.api.Plugin
 import org.gradle.api.Project

 /**
  * 自定义插件
  */
 class CysPlugin implements Plugin<Project>{

     @Override
     void apply(Project project) {
         // 查找AppExtension
         def appExtension = project.extensions.findByType(AppExtension.class)
         // 注册自定义的Transform
         appExtension.registerTransform(new CysTransform(project))
     }
 }