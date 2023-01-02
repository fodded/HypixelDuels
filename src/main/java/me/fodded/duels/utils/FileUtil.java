package me.fodded.duels.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class FileUtil {
    // Originally not my code
    public static void copy(File source, File destination) throws IOException {
        ArrayList<String> ignore = new ArrayList<>(Arrays.asList("uid.dat", "session.lock"));
        if(!ignore.contains(source.getName())) {
            if (source.isDirectory()) {
                if (!destination.exists()) {
                    destination.mkdir();
                }

                String[] files = source.list();
                if (files == null) return;
                for (String file : files) {
                    File newSource = new File(source, file);
                    File newDestination = new File(destination, file);
                    copy(newSource, newDestination);
                }
            } else {
                InputStream in = new FileInputStream(source);
                OutputStream out = new FileOutputStream(destination);

                byte[] buffer = new byte[1024];
                int length;

                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }

                in.close();
                out.close();
            }
        }
    }

    public static void delete(File file) {
        if(file.isDirectory()) {
            File[] files = file.listFiles();
            if(files == null) return;
            for(File child : files) {
                delete(child);
            }
        }

        file.delete();
    }
}
