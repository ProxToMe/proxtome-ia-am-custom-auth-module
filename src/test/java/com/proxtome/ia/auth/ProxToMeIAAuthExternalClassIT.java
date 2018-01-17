package com.proxtome.ia.auth;

import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ProxToMeIAAuthExternalClassIT {

    public static final String AM_AUTHENTICATE_ENDPOINT = "http://openam.example.com:8080/openam/json/authenticate";

    @Test
    public void testGetToken() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        //Get Initial Auth ID
        ResponseEntity<SampleAuthCallback> entity = restTemplate.exchange(AM_AUTHENTICATE_ENDPOINT,
                HttpMethod.POST, new HttpEntity<>(httpHeaders), ProxToMeIAAuthCallback.class);
        ProxToMeIAAuthCallback callback = entity.getBody();

        //Set correct username and password
        callback.setCredentials("testdevice", "1111111111111111", "<challenge>", "<response>");

        //Authenticate to OpenAM
        ResponseEntity<String> tokenEntity = restTemplate.exchange(AM_AUTHENTICATE_ENDPOINT,
                HttpMethod.POST, new HttpEntity<>(callback, httpHeaders), String.class);

        //Assert response is 200 and print token
        Assert.assertEquals(tokenEntity.getStatusCode(), HttpStatus.OK);
        System.out.println(tokenEntity.getBody());
    }

    @Test
    public void testFailGetToken() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        //Get Initial Auth ID
        ResponseEntity<SampleAuthCallback> entity = restTemplate.exchange(AM_AUTHENTICATE_ENDPOINT,
                HttpMethod.POST, new HttpEntity<>(httpHeaders), ProxToMeIAAuthCallback.class);
        ProxToMeIAAuthCallback callback = entity.getBody();

        //Set incorrect username and password
        callback.setCredentials("testdevice", "1111111111111111", "wrongchallenge", "wrongresponse");

        //Authenticate to OpenAM
        try {
            restTemplate.exchange(AM_AUTHENTICATE_ENDPOINT,
                    HttpMethod.POST, new HttpEntity<>(callback, httpHeaders), String.class);
        } catch (HttpClientErrorException e) {
            //Assert response is 401
            Assert.assertEquals(e.getStatusCode(), HttpStatus.UNAUTHORIZED);
            return;

        }
        // Fail if 401 isn't received
        Assert.fail();
    }
}