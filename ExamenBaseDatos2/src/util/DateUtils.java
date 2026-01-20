package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtils {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static String format(LocalDate date) {
        return date != null ? date.format(FMT) : "";
    }

    public static LocalDate parse(String text) throws DateTimeParseException {
        return LocalDate.parse(text.trim(), FMT);
    }
}
