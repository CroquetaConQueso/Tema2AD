package vista.validacion;

import java.util.Set;
import java.util.regex.Pattern;

public final class Validator {
    private Validator(){}

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern CP_ES = Pattern.compile("^\\d{5}$");
    private static final Pattern TEL   = Pattern.compile("^[+\\d][\\d\\s-]{6,}$");
    private static final Pattern MIME  = Pattern.compile("^[a-zA-Z0-9!#$&^_.+-]+/[a-zA-Z0-9!#$&^_.+-]+$");

    private static final Set<String> MIME_IMAGEN = Set.of(
        "image/png","image/jpeg","image/jpg","image/gif","image/webp","image/bmp"
    );

    public static boolean notBlank(String s){ return s != null && !s.trim().isEmpty(); }

    public static boolean email(String s){ return notBlank(s) && EMAIL.matcher(s.trim()).matches(); }

    public static boolean edad(int v){ return v >= 0 && v <= 120; }

    public static boolean cpES(String s){ return notBlank(s) && CP_ES.matcher(s.trim()).matches(); }

    public static boolean telefono(String s){ return notBlank(s) && TEL.matcher(s.trim()).matches(); }

    public static boolean mime(String s){ return notBlank(s) && MIME.matcher(s.trim()).matches(); }

    public static boolean mimeImagen(String s){ return mime(s) && MIME_IMAGEN.contains(s.trim().toLowerCase()); }

    public static boolean lengthBetween(String s, int min, int max){
        if (s == null) return false;
        int len = s.trim().length();
        return len >= min && len <= max;
    }

    public static boolean bytesMax(byte[] data, long maxBytes){
        return data != null && data.length <= maxBytes;
    }
}
