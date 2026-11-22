package com.family.birthday;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Sends WhatsApp messages through the free CallMeBot API (personal use only).
 * Each recipient must activate CallMeBot once from their own phone to get an API key.
 */
public class CallMeBotWhatsAppNotifier implements Notifier {

    private static final String ENDPOINT = "https://api.callmebot.com/whatsapp.php";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    @Override
    public boolean send(Member recipient, String message) {
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                String url = ENDPOINT
                        + "?phone=" + enc(recipient.phone())
                        + "&text=" + enc(message)
                        + "&apikey=" + enc(recipient.apiKey());
                HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                        .timeout(Duration.ofSeconds(60))
                        .GET()
                        .build();
                HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
                String body = res.body() == null ? "" : res.body();
                boolean ok = res.statusCode() / 100 == 2 && !body.toLowerCase().contains("error");
                if (ok) {
                    return true;
                }
                System.err.printf("  CallMeBot rejected message to %s (HTTP %d): %s%n",
                        recipient.name(), res.statusCode(), snippet(body));
            } catch (Exception e) {
                System.err.printf("  Attempt %d to %s failed: %s%n", attempt, recipient.name(), e.getMessage());
            }
            sleep(10);
        }
        return false;
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String snippet(String body) {
        String plain = body.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").strip();
        return plain.length() > 200 ? plain.substring(0, 200) + "..." : plain;
    }

    static void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
