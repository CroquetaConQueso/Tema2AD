package util;

import java.io.*;

public class ByteUtils {

    public static byte[] readFile(File file) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             InputStream in = new FileInputStream(file)) {
            in.transferTo(baos);
            return baos.toByteArray();
        }
    }

    public static void writeFile(byte[] data, File file) throws IOException {
        try (OutputStream out = new FileOutputStream(file)) {
            out.write(data);
        }
    }
}
