package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ByteUtils {

    public static void writeBytes(File out, byte[] data) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(out)) {
            fos.write(data);
        }
    }
}
