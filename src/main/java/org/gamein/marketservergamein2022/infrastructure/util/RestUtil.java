package org.gamein.marketservergamein2022.infrastructure.util;

import com.google.gson.JsonObject;
import org.gamein.marketservergamein2022.core.exception.InvalidTokenException;
import org.gamein.marketservergamein2022.web.dto.AuthInfo;
import org.gamein.marketservergamein2022.web.dto.AuthInfoResponse;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


public class RestUtil {
    private static final String dashboardUrl = "http://localhost:8081/dashboard/auth/info";

    public static AuthInfoResponse getAuthInfo(String token) throws RestClientException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("Authorization", token);

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<AuthInfoResponse> response = restTemplate.exchange(dashboardUrl, HttpMethod.GET, request,
                AuthInfoResponse.class);
        return response.getBody();
    }

    public static String sendRawRequest(String url, Map<String, String> params, HttpMethod method, MediaType mediaType) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        if (mediaType != null)
            headers.setContentType(mediaType);

        JsonObject properties = new JsonObject();
        params.keySet().forEach(key -> {
            properties.addProperty(key, params.get(key));
        });

        HttpEntity<String> request = new HttpEntity<>(properties.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(url, method, request, String.class);

        return response.getBody();
    }

    public static String sendRawRequestByToken(String token, String url, Map<String, String> params, HttpMethod method, MediaType mediaType) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("G-BT-TOKEN", token);
        if (mediaType != null)
            headers.setContentType(mediaType);

        JsonObject properties = new JsonObject();
        params.keySet().forEach(key -> {
            properties.addProperty(key, params.get(key));
        });

        HttpEntity<String> request = new HttpEntity<>(properties.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(url, method, request, String.class);

        return response.getBody();
    }
}