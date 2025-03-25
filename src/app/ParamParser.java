package app;

import java.util.*;

public class ParamParser {

    public static int parseInt(String input, int defaultVal) {
        List<Integer> values = new ArrayList<>();

        for (String part : input.split(",")) {
            if (part.contains("-")) {
                String[] range = part.split("-");
                int start = convertToInt(range[0]);
                int end = convertToInt(range[1]);
                for (int i = start; i <= end; i++) {
                    values.add(i);
                }
            } else {
                values.add(convertToInt(part.trim()));
            }
        }

        if (values.isEmpty()) return defaultVal;
        return values.get(new Random().nextInt(values.size()));
    }

    public static double parseFloat(String input, double defaultVal) {
        List<Double> values = new ArrayList<>();

        for (String part : input.split(",")) {
            if (part.contains("-")) {
                String[] range = part.split("-");
                double start = convertToFloat(range[0]);
                double end = convertToFloat(range[1]);
                for (double i = start; i <= end; i += 0.1) {
                    values.add(Math.round(i * 100.0) / 100.0); // precision 2 digits
                }
            } else {
                values.add(convertToFloat(part.trim()));
            }
        }

        if (values.isEmpty()) return defaultVal;
        return values.get(new Random().nextInt(values.size()));
    }

    private static int convertToInt(String raw) {
        raw = raw.toUpperCase();
        double multiplier = getUnitMultiplier(raw);
        double num = Double.parseDouble(raw.replaceAll("[KMG]", ""));
        return (int)(num * multiplier);
    }

    private static double convertToFloat(String raw) {
        raw = raw.toUpperCase();
        double multiplier = getUnitMultiplier(raw);
        double num = Double.parseDouble(raw.replaceAll("[KMG]", ""));
        return num * multiplier;
    }

    private static double getUnitMultiplier(String raw) {
        if (raw.endsWith("K")) return 1024;
        if (raw.endsWith("M")) return 1024 * 1024;
        if (raw.endsWith("G")) return 1024 * 1024 * 1024;
        return 1;
    }
}
