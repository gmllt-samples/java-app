package app;

import java.util.*;
import java.text.SimpleDateFormat;

public class Logger {
    public static void log(Map<String, Object> data) {
        System.out.println(toJson(data));
    }

    public static String timestamp() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date());
    }

    private static String toJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        List<String> parts = new ArrayList<>();
        for (var entry : map.entrySet()) {
            parts.add("\"" + entry.getKey() + "\":" + toJsonValue(entry.getValue()));
        }
        json.append(String.join(",", parts));
        json.append("}");
        return json.toString();
    }

    private static String toJsonValue(Object value) {
        if (value instanceof String)
            return "\"" + value.toString().replace("\"", "\\\"") + "\"";
        if (value instanceof Map)
            return toJson((Map<String, Object>) value);
        return String.valueOf(value);
    }
}
