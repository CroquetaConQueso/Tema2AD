package validation;

import model.Ciudad;
import model.Persona;

public class Validador {

    public static void validarPersona(Persona p) {
        if (p.getNombre() == null || p.getNombre().isBlank())
            throw new IllegalArgumentException("Nombre de persona es obligatorio.");
        if (p.getEdad() == null || p.getEdad() < 0 || p.getEdad() > 120)
            throw new IllegalArgumentException("Edad debe estar entre 0 y 120.");
    }

    public static void validarCiudad(Ciudad c) {
        if (c.getNombre() == null || c.getNombre().isBlank())
            throw new IllegalArgumentException("Nombre de ciudad es obligatorio.");
    }
}
