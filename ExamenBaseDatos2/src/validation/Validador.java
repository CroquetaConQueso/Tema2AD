package validation;

import model.EventoSocial;
import model.FotoEventoSocial;

public class Validador {

    public static void validar(EventoSocial e) {
        if (e.getNombre() == null || e.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del evento es obligatorio.");
        }
        if (e.getFechaEvento() == null) {
            throw new IllegalArgumentException("La fecha del evento es obligatoria.");
        }
    }

    public static void validar(FotoEventoSocial f) {
        if (f.getEvento() == null || f.getEvento().getIdEvento() <= 0) {
            throw new IllegalArgumentException("Debes seleccionar un evento.");
        }
        if (f.getFoto() == null || f.getFoto().length == 0) {
            throw new IllegalArgumentException("Debes seleccionar una imagen.");
        }
        if (f.getCantidad() < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa.");
        }
    }
}
