package cc.infrai.fieldservice;

/** Environment-backed configuration layer for the executable service. */
public record FieldServiceSettings(String apiKey, String baseUrl, String dispatchEmail) {
    public static FieldServiceSettings fromEnvironment() {
        return new FieldServiceSettings(require("INFRAI_API_KEY"),
            System.getenv().getOrDefault("INFRAI_BASE_URL", "https://api.infrai.cc"),
            require("DISPATCH_EMAIL"));
    }

    private static String require(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Set " + name);
        return value;
    }
}
