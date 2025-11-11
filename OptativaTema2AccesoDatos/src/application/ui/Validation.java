package application.ui;

import javafx.css.PseudoClass;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.util.regex.Pattern;

public final class Validation {
    private static final PseudoClass PSEUDO_ERROR = PseudoClass.getPseudoClass("error");

    // Límites
    public static final int MAX_NOMBRE = 100;
    public static final int MAX_TLF    = 20;
    public static final int MAX_EMAIL  = 120;
    public static final int MAX_MARCA  = 50;
    public static final int MAX_MODELO = 50;
    public static final int MAX_MAT    = 15;

    // Patrones
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+()\\-\\s]{6,20}$");
    private static final Pattern MATRICULA_FLEX = Pattern.compile("^[A-Z0-9\\-]{4,15}$");
    private static final Pattern MATRICULA_ES   = Pattern.compile("^[0-9]{4}[A-Z]{3}$");

    private Validation(){}

    // --- Marcas de error UI ---
    public static void clearError(Control... cs){ for (var c: cs) if (c!=null) c.pseudoClassStateChanged(PSEUDO_ERROR,false); }
    public static void markError(Control c){ if (c!=null) c.pseudoClassStateChanged(PSEUDO_ERROR,true); }

    // --- Campos obligatorios / opcionales ---
    public static int requirePositiveInt(TextField tf, String nombre){
        String s = safe(tf);
        if (s.isEmpty()) { markError(tf); throw new IllegalArgumentException(nombre+" vacío"); }
        try {
            int n = Integer.parseInt(s);
            if (n<=0) { markError(tf); throw new IllegalArgumentException(nombre+" debe ser > 0"); }
            return n;
        } catch (NumberFormatException e){ markError(tf); throw new IllegalArgumentException(nombre+" no numérico"); }
    }

    public static String requireNonEmpty(TextField tf, String nombre, int max){
        String s = safe(tf);
        if (s.isEmpty()) { markError(tf); throw new IllegalArgumentException(nombre+" vacío"); }
        if (s.length()>max) { markError(tf); throw new IllegalArgumentException(nombre+" supera "+max+" caracteres"); }
        return s;
    }

    public static String optionalLimited(TextField tf, int max){
        String s = safe(tf);
        if (s.length()>max) { markError(tf); throw new IllegalArgumentException("Longitud máxima "+max+" superada"); }
        return s;
    }

    public static String optionalEmail(TextField tf){
        String s = safe(tf);
        if (!s.isEmpty() && !EMAIL.matcher(s).matches()){ markError(tf); throw new IllegalArgumentException("Email no válido"); }
        if (s.length()>MAX_EMAIL){ markError(tf); throw new IllegalArgumentException("Email demasiado largo"); }
        return s;
    }

    public static String optionalTelefono(TextField tf){
        String s = safe(tf);
        if (!s.isEmpty() && !PHONE.matcher(s).matches()){ markError(tf); throw new IllegalArgumentException("Teléfono no válido"); }
        if (s.length()>MAX_TLF){ markError(tf); throw new IllegalArgumentException("Teléfono demasiado largo"); }
        return s;
    }

    public static String matricula(TextField tf, boolean strictES){
        String s = safe(tf).toUpperCase();
        if (s.isEmpty()){ markError(tf); throw new IllegalArgumentException("Matrícula vacía"); }
        if (s.length()>MAX_MAT){ markError(tf); throw new IllegalArgumentException("Matrícula demasiado larga"); }
        boolean ok = strictES ? MATRICULA_ES.matcher(s).matches() : MATRICULA_FLEX.matcher(s).matches();
        if (!ok){ markError(tf); throw new IllegalArgumentException(strictES? "Formato 0000ABC":"Matrícula inválida"); }
        return s;
    }

    public static int requireComboId(ComboBox<String> combo, String nombreCampo){
        String v = combo.getValue()==null? "": combo.getValue();
        if (v.isBlank()){ markError(combo); throw new IllegalArgumentException("Selecciona "+nombreCampo); }
        int i = v.indexOf(" - ");
        try { return Integer.parseInt(i>0 ? v.substring(0,i) : v); }
        catch (Exception e){ markError(combo); throw new IllegalArgumentException(nombreCampo+" inválido"); }
    }

    public static String requireChoice(ChoiceBox<String> cb, String nombreCampo){
        String v = cb.getValue()==null? "": cb.getValue();
        if (v.isBlank()){ markError(cb); throw new IllegalArgumentException("Selecciona "+nombreCampo); }
        return v;
    }

    public static LocalDate optionalDateOr(LocalDate fallback, DatePicker dp){
        return dp==null || dp.getValue()==null ? fallback : dp.getValue();
    }

    private static String safe(TextField tf){ return tf.getText()==null? "": tf.getText().trim(); }
}

