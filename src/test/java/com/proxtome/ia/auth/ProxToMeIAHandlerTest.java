package com.proxtome.ia.auth;

import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpPost;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;


public class ProxToMeIAHandlerTest {
	private final static String VALID_PROXTOME_URL = "http://proxtome-ia.cloudapp.net/api/response";
	private final static String VALID_CONTENT_TYPE = "application/json";
	private final static String VALID_METHOD = "POST";
	private final static String TEST_USERID = "testuser";
	private final static String TEST_DEVICEID = "1111111111111111";
	private final static String TEST_CHALLENGE = "testchallenge";
	private final static String TEST_RESPONSE = "testresponse";
	private final static String VALID_JSON_REQUEST = "{\"deviceId\": \"1111111111111111\"}";

	@Test
	public void testRequestURL() throws Exception {
		ProxToMeIAHandler handler = new ProxToMeIAHandler();
		HttpPost request = handler.makeRequest(TEST_USERID, TEST_DEVICEID, TEST_CHALLENGE, TEST_RESPONSE);
		Assert.assertEquals(request.getURI().toString(), VALID_PROXTOME_URL);
	}

    @Test
    public void testRequestContentType() throws Exception {
    	ProxToMeIAHandler handler = new ProxToMeIAHandler();
		HttpPost request = handler.makeRequest(TEST_USERID, TEST_DEVICEID, TEST_CHALLENGE, TEST_RESPONSE);
		Header[] header = request.getHeaders("Content-Type");
    	Assert.assertEquals(header.length, 1);
		Assert.assertEquals(header[0].getValue(), VALID_CONTENT_TYPE);
    }

	@Test
	public void testRequestMethod() throws Exception {
		ProxToMeIAHandler handler = new ProxToMeIAHandler();
		HttpPost request = handler.makeRequest(TEST_USERID, TEST_DEVICEID, TEST_CHALLENGE, TEST_RESPONSE);
		Assert.assertEquals(request.getMethod(), VALID_METHOD);
	}

	@Test
	public void testRequestParameters() throws Exception {
		ProxToMeIAHandler handler = new ProxToMeIAHandler();
		HttpPost request = handler.makeRequest(TEST_USERID, TEST_DEVICEID, TEST_CHALLENGE, TEST_RESPONSE);
		String payload = EntityUtils.toString(request.getEntity());
		JsonNode payloadJson = new ObjectMapper().reader().readTree(payload);
		Assert.assertEquals(payloadJson.get("p2m_user_id").asText(), TEST_USERID);
		Assert.assertEquals(payloadJson.get("p2m_device_id").asText(), TEST_DEVICEID);
		Assert.assertEquals(payloadJson.get("challenge").asText(), TEST_CHALLENGE);
		Assert.assertEquals(payloadJson.get("response").asText(), TEST_RESPONSE);
	}

	@Test
	public void testResponseGood() throws Exception {
		ProxToMeIAHandler handler = new ProxToMeIAHandler();
		int result = handler.handleResponse(VALID_JSON_REQUEST, TEST_DEVICEID, TEST_USERID);
		Assert.assertEquals(result, ProxToMeIAHandler.PROXTOME_OK);
	}

	@Test
	public void testResponseBad() throws Exception {
		ProxToMeIAHandler handler = new ProxToMeIAHandler();
		int result = handler.handleResponse(VALID_JSON_REQUEST, "invalid deviceid", TEST_USERID);
		Assert.assertEquals(result, ProxToMeIAHandler.PROXTOME_DENIED);
	}

	@Test
	public void testResponseInvalidJSON() throws Exception {
		ProxToMeIAHandler handler = new ProxToMeIAHandler();
		int result = handler.handleResponse("invalid json", TEST_DEVICEID, TEST_USERID);
		Assert.assertEquals(result, ProxToMeIAHandler.PROXTOME_DOWN);
	}
}