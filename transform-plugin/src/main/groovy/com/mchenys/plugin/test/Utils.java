package com.mchenys.plugin.test;

import java.io.File;

/**
 * Author: ChenYouSheng
 * Date: 2021/12/20
 * Email: chenyousheng@lizhi.fm
 * Desc:
 */
public class Utils {

    public static String getClassFilePath(Class clazz) {
        String buildDir = clazz.getProtectionDomain().getCodeSource().getLocation().getFile();
        String fileName = clazz.getSimpleName() + ".class";
        File file = new File(buildDir + clazz.getPackage().getName().replaceAll("[.]", "/") + "/", fileName);
        return file.getAbsolutePath();
    }
}
