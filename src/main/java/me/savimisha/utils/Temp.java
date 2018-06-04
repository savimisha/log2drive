package me.savimisha.utils;


import me.savimisha.Config;

import java.io.File;

public class Temp {
    public static void check(){
        File tmpDir = new File(Config.TMP_DIR);
        if (!tmpDir.exists())
            tmpDir.mkdir();
        System.setProperty("java.io.tmpdir", Config.TMP_DIR);
    }
}
