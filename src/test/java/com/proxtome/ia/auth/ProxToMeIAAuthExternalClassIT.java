package com.proxtome.ia.auth;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;


public class ProxToMeIAAuthExternalClassIT {
    private static final String AM_AUTHENTICATE_ENDPOINT = "http://proxtome-am.cloudapp.net:8080/openam/json/authenticate";
    private String userToken = null;
    private final static String VALID_USERID = "testuser";
    private final static String VALID_DEVICEID = "1111111111111111";
    private final static String VALID_CHALLENGE = "1234567890abcdef0987654321fedcba";
    private final static String VALID_RESPONSE = "eb06fb0fa8d56e1d96d46d74377b92bb";

    @BeforeSuite
    public void getAuthToken() throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost request = new HttpPost(AM_AUTHENTICATE_ENDPOINT);
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        request.setHeader("X-OpenAM-Username", "demo");
        request.setHeader("X-OpenAM-Password", "changeit");
        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        String jsonResponse = EntityUtils.toString(response.getEntity());
        System.out.println(jsonResponse);
        Assert.assertEquals(statusCode, 200);
        this.userToken = new ObjectMapper().reader().readTree(jsonResponse).get("tokenId").asText();
    }

    @Test
    public void testWorking() throws Exception {
        String jsonPayload = "{\"deviceId\": \"" + VALID_DEVICEID + "\", \"callbacks\": " +
                "[{\"type\": \"NameCallback\", " +
                "\"input\": [{\"name\": \"IDToken1\", \"value\": \"" + VALID_USERID + "\"}]}, " +
                "{\"type\": \"NameCallback\", " +
                "\"input\": [{\"name\": \"IDToken2\", \"value\": \"" + VALID_DEVICEID + "\"}]}, " +
                "{\"type\": \"PasswordCallback\", " +
                "\"input\": [{\"name\": \"IDToken3\", \"value\": \"" + VALID_CHALLENGE + "\"}]}, " +
                "{\"type\": \"PasswordCallback\", " +
                "\"input\": [{\"name\": \"IDToken4\", \"value\":\"" + VALID_RESPONSE + "\"}]}]}";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost request = new HttpPost(AM_AUTHENTICATE_ENDPOINT +
                "?authIndexType=module" +
                "&authIndexValue=ProxToMe" +
                "&sessionUpgradeSSOTokenId=" + this.userToken
        );
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        request.setEntity(new StringEntity(jsonPayload));
        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        String jsonResponse = EntityUtils.toString(response.getEntity());
        System.out.println(jsonResponse);
        Assert.assertEquals(statusCode, 200);
        String updatedToken = new ObjectMapper().reader().readTree(jsonResponse).get("tokenId").asText();
        Assert.assertNotNull(updatedToken);
    }
}