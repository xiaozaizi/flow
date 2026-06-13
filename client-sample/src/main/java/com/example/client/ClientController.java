package com.example.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
public class ClientController {
    private final RestTemplate rest = new RestTemplate();

    @PostMapping("/client/demo/start")
    public Map start() {
        String url = "http://localhost:8080/api/process/start/sampleApproval";
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("X-Tenant-Id", "tenant_abc");
        h.set("X-User-Id", "client_user");
        HttpEntity<Map> e = new HttpEntity<>(Map.of(), h);
        return rest.postForObject(url, e, Map.class);
    }
}
