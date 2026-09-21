package cc.infrai.fieldservice;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Small REST client for the two capability groups used by this sample. */
public final class InfraiClient {
    private final HttpClient http;
    private final String baseUrl;
    private final String apiKey;

    public InfraiClient(String baseUrl, String apiKey) {
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.baseUrl = baseUrl.replaceAll("/$", "");
        this.apiKey = apiKey;
    }

    // Canonical capability reference: infrai.account.autorecharge.configure
    public String configureAutoRecharge(String triggerBalance, String rechargeAmount)
        throws IOException, InterruptedException, InfraiException {
        String body = "{\"trigger_balance\":" + json(triggerBalance)
            + ",\"recharge_amount\":" + json(rechargeAmount) + "}";
        return send("PUT", "/v1/account/autorecharge/configure", body);
    }

    public String sendRechargeNotice(String recipient, String subject, String text)
        throws IOException, InterruptedException, InfraiException {
        String body = "{\"to\":" + json(recipient) + ",\"subject\":" + json(subject)
            + ",\"body\":" + json(text) + "}";
        return send("POST", "/v1/email/send", body);
    }

    private String send(String method, String path, String body)
        throws IOException, InterruptedException, InfraiException {
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.ofString(body))
                .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String envelope = response.body();
            boolean successful = ok(envelope); // Decode the envelope before using the status.
            if (response.statusCode() == 429 && attempt < 2) {
                Thread.sleep(retryDelay(response, attempt));
                continue;
            }
            if (!successful) throw new InfraiException(response.statusCode(), error(envelope));
            if (response.statusCode() >= 500) {
                throw new IOException("Unexpected transport status " + response.statusCode());
            }
            return envelope;
        }
        throw new IOException("Retry limit reached");
    }

    private static boolean ok(String envelope) {
        return envelope.matches("(?s).*\\\"ok\\\"\\s*:\\s*true.*");
    }

    private static String error(String envelope) {
        return envelope.length() > 500 ? envelope.substring(0, 500) : envelope;
    }

    private static long retryDelay(HttpResponse<?> response, int attempt) {
        return response.headers().firstValue("Retry-After").map(value -> Long.parseLong(value) * 1000L)
            .orElse(250L * (1L << attempt));
    }

    private static String json(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    public static final class InfraiException extends Exception {
        public InfraiException(int status, String detail) {
            super("Infrai response " + status + ": " + detail);
        }
    }
}
