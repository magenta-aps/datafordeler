package dk.magenta.datafordeler.core;

public abstract class Environment {

    public static String getEnv(String key, String fallback) {
        String value = System.getenv(key);
        return (value != null) ? value : fallback;
    }
    public static int getEnv(String key, int fallback) {
        String value = System.getenv(key);
        return (value != null) ? Integer.parseInt(value) : fallback;
    }
    public static boolean getEnv(String key, boolean fallback) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.equalsIgnoreCase("true");
    }

}
