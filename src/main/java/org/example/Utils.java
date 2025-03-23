package org.example;

import java.io.*;
import java.nio.file.Path;

public class Utils {
    public Path resourceNameToPath(String name) throws IOException {
        InputStream input = getClass().getResourceAsStream(name);
        File file = File.createTempFile("tempfile", ".tmp");
        OutputStream out = new FileOutputStream(file);
        int read;
        byte[] bytes = new byte[1024];

        while ((read = input.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
        out.close();
        file.deleteOnExit();
        return file.toPath();
    }
}
