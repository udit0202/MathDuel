package studio.idle.mathduel.common;

/**
 * Created by Udit on 30/03/15.
 */
public class StringUtils {

    public static String convertToTimeFormat(long value) {
        String formattedValue = (value < 10 ? ("0" + value) : String.valueOf(value));
        return formattedValue;
    }
}
