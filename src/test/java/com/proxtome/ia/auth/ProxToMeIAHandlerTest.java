package com.proxtome.ia.auth;

import org.testng.Assert;
import org.testng.annotations.Test;
import com.proxtome.ia.auth.ProxToMeIAHandler;

public class ProxToMeIAHandlerTest {
	private final static String VALID_DEVICEID = "1111111111111111";
	private final static String VALID_CHALLENGE = "1234567890abcdef0987654321fedcba";
	private final static String VALID_RESPONSE = "eb06fb0fa8d56e1d96d46d74377b92bb";

    @Test
    public void testPositive() throws Exception {
    	ProxToMeIAHandler handler = new ProxToMeIAHandler();
    	int result = handler.handleAuthorization(
    		"testuser", 
    		VALID_DEVICEID
    		VALID_CHALLENGE, 
    		VALID_RESPONSE, 
    		'{"device_id": "1111111111111111"}');
        Assert.assertEquals(result, ProxToMeIAHandler.PROXTOME_OK);
    }

    @Test
    public void testRequestWrongDeviceID() throws Exception {
        ProxToMeIAHandler handler = new ProxToMeIAHandler();
    	int result = handler.handleAuthorization(
    		"testuser", 
    		VALID_DEVICEID,
    		VALID_CHALLENGE, 
    		VALID_RESPONSE, 
    		'{"device_id": "2222222222222222"}');
        Assert.assertEquals(result, ProxToMeIAHandler.PROXTOME_DENIED);
    }

    @Test
    public void testRequestInvalidJSON() throws Exception {
        ProxToMeIAHandler handler = new ProxToMeIAHandler();
    	int result = handler.handleAuthorization(
    		"testuser", 
    		VALID_DEVICEID,
    		VALID_CHALLENGE, 
    		VALID_RESPONSE, 
    		'invalid JSON');
        Assert.assertEquals(result, ProxToMeIAHandler.PROXTOME_DOWN);
    }

    @Test
    public void testWrongDeviceID() throws Exception {
    	ProxToMeIAHandler handler = new ProxToMeIAHandler();
    	int result = handler.handleAuthorization(
    		"testuser", 
    		"invalid device id", 
    		VALID_CHALLENGE, 
    		VALID_RESPONSE, 
    		'{"device_id": "1111111111111111"}');
        Assert.assertEquals(result, ProxToMeIAHandler.PROXTOME_DENIED);
    }

    @Test
    public void testWrongChallenge() throws Exception {
    	ProxToMeIAHandler handler = new ProxToMeIAHandler();
    	int result = handler.handleAuthorization(
    		"testuser", 
    		VALID_DEVICEID, 
    		"invalid challenge", 
    		VALID_RESPONSE, 
    		'{"device_id": "1111111111111111"}');
        Assert.assertEquals(result, ProxToMeIAHandler.PROXTOME_DENIED);
    }

    @Test
    public void testWrongResponse() throws Exception {
    	ProxToMeIAHandler handler = new ProxToMeIAHandler();
    	int result = handler.handleAuthorization(
    		"testuser", 
    		VALID_DEVICEID, 
    		VALID_CHALLENGE, 
    		"invalid response", 
    		'{"device_id": "1111111111111111"}');
        Assert.assertEquals(result, ProxToMeIAHandler.PROXTOME_DENIED);
    }
}