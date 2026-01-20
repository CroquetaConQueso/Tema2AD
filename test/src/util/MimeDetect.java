package util;

import java.util.Locale;

public class MimeDetect {

    public static String detect(byte[] data, String originalName) {
        // 1) Por extensión del nombre
        if (originalName != null) {
            String lower = originalName.toLowerCase(Locale.ROOT);
            if (lower.endsWith(".png"))  return "image/png";
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
            if (lower.endsWith(".gif"))  return "image/gif";
            if (lower.endsWith(".pdf"))  return "application/pdf";
            if (lower.endsWith(".txt"))  return "text/plain";
        }
        // 2) Cabeceras mágicas mínimas
        if (data != null && data.length >= 4) {
            // PNG: 89 50 4E 47
            if ((data[0] & 0xFF) == 0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47)
                return "image/png";
            // JPG: FF D8
            if ((data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8)
                return "image/jpeg";
            // GIF: 47 49 46 38
            if (data[0] == 0x47 && data[1] == 0x49 && data[2] == 0x46 && data[3] == 0x38)
                return "image/gif";
            // PDF: 25 50 44 46 = %PDF
            if (data[0] == 0x25 && data[1] == 0x50 && data[2] == 0x44 && data[3] == 0x46)
                return "application/pdf";
        }
        // 3) Fallback
        return "application/octet-stream";
    }
}
