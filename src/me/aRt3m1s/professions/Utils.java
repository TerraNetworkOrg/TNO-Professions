package me.aRt3m1s.professions;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: Christian
 * Date: 9/15/11
 * Time: 7:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class Utils {
    public static Professions plugin;

    public Utils(Professions instance) {
        plugin = instance;
    }

    public void copy(InputStream inputThis, File sFile) throws IOException{
        InputStream in = inputThis;
        OutputStream out = new FileOutputStream(sFile);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        in.close();
    }
}
