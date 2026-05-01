package github.titandea.utils;

import lombok.experimental.UtilityClass;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class TimeFormatter {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm");

    public static LocalTime normalizeTime(String time) {
        return LocalTime.parse(time.trim(), formatter);
    }
}
