package isep.crescendo.util;

import java.text.DecimalFormat;

public class Formatter {
    private static final DecimalFormat priceFormat = new DecimalFormat("#,##0.00");

    public static String formatPrice(double price) {
        return priceFormat.format(price);
    }
}