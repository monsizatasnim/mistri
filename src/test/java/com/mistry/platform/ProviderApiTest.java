package com.mistry.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Backend cholte hobe, ar cloud@example.com / secret123 provider ta thakte hobe
class ProviderApiTest {

    private static final String BASE = "http://localhost:8080";
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final ObjectMapper JSON = new ObjectMapper();
    private static String token;

    private static HttpResponse<String> send(String method, String path, String bearer) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(BASE + path))
                .method(method, HttpRequest.BodyPublishers.noBody());
        if (bearer != null) b.header("Authorization", "Bearer " + bearer);
        return HTTP.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    @BeforeAll
    static void login() throws Exception {
        HttpResponse<String> r = send("POST",
                "/api/service-provider/login?email=cloud@example.com&password=secret123", null);
        assertEquals(200, r.statusCode());
        token = JSON.readTree(r.body()).get("token").asText();
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        HttpResponse<String> r = send("POST",
                "/api/service-provider/login?email=cloud@example.com&password=wrong", null);
        assertTrue(r.statusCode() >= 400 && r.statusCode() < 500, "status was " + r.statusCode());
    }

    @Test
    void providerCanSeeVerificationStatus() throws Exception {
        HttpResponse<String> r = send("GET", "/api/service-provider/verification-status", token);
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("verificationStatus"));
    }

    @Test
    void providerCanUpdatePrice() throws Exception {
        HttpResponse<String> r = send("PUT",
                "/api/service-provider/profile?priceMin=600&priceMax=2500", token);
        assertEquals(200, r.statusCode());
        JsonNode profile = JSON.readTree(
                send("GET", "/api/providers/2/profile", token).body());
        assertEquals(600.0, profile.get("priceMin").asDouble());
        assertEquals(2500.0, profile.get("priceMax").asDouble());
    }

    @Test
    void cancellationPolicyBelowAdminMinimumIsRejected() throws Exception {
        HttpResponse<String> r = send("PUT",
                "/api/service-provider/cancellation-policy?windowHours=1&refundPercent=50", token);
        assertEquals(400, r.statusCode());
    }

    @Test
    void validCancellationPolicyIsSaved() throws Exception {
        HttpResponse<String> r = send("PUT",
                "/api/service-provider/cancellation-policy?windowHours=24&refundPercent=50", token);
        assertEquals(200, r.statusCode());
    }

    @Test
    void customerTokenCannotOpenAdminEndpoint() throws Exception {
        HttpResponse<String> r = send("GET", "/api/admin/providers/2/documents", token);
        assertEquals(403, r.statusCode());
    }
}