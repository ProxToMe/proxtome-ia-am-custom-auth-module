/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2016 ForgeRock AS. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file at legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 */

/**
 * Portions Copyrighted 2017-2018 ProxToMe inc.
 */
package com.proxtome.ia.auth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.HashMap;

import com.sun.identity.shared.debug.Debug;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;


public class ProxToMeIAHandler {
    // ProxToMe Server URL
    private final static String PROXTOME_URL = "http://proxtome-ia.cloudapp.net/api/response";
    private final static String CONTENT_TYPE = "application/json";
    
	// Name for the debug-log
    private final static String DEBUG_NAME = "ProxToMeIAAuth";
    private final static Debug debug = Debug.getInstance(DEBUG_NAME);

    // Return codes
    public final static int PROXTOME_OK = 0;
    public final static int PROXTOME_DOWN = 1;
    public final static int PROXTOME_DENIED = 2;

    /**
     * This method creates the HttpPost request object to the ProxToMe Backend.
     * @param userID: The ProxToMe userID field created by the mobile SDK.
     * @param deviceID: The deviceID of the ProxToMe Device believed to be close to the User.
     * @param challenge: The challenge data used by ProxToMe to verify the proximity.
     * @param response: The response data used by ProxToMe to verify the proximity.
     */
    public HttpPost makeRequest(String userID, String deviceID, String challenge, String response) {
        Map<String, String> payload = new HashMap<String, String>();
        payload.put("p2m_user_id", userID);
        payload.put("p2m_device_id", deviceID);
        payload.put("challenge", challenge);
        payload.put("response", response);
        String jsonPayload = null;
        try {
            jsonPayload = new ObjectMapper().writeValueAsString(payload);
            HttpPost request = new HttpPost(PROXTOME_URL);
            request.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE);
            request.setEntity(new StringEntity(jsonPayload));
            return request;
        } catch (JsonProcessingException exc) {
            debug.message("Error in JSON Serialization");
            return null;
        } catch (UnsupportedEncodingException exc2) {
            debug.message("Error in setting up Request");
            return null;
        }
    }

    /**
     * This method handles a successful response from the ProxToMe Backend,
     * to determine if the result is compatible with a successful authorization or not.
     * @param requestBody: The AM request body, used to match the ProxToMe response with what the User requested through AM.
     * @param userID: The ProxToMe userID field created by the mobile SDK.
     * @param deviceID: The deviceID of the ProxToMe Device believed to be close to the User.
     */
    public int handleResponse(String requestBody, String deviceID, String userID) {
        try {
            JsonNode deviceIdNode = new ObjectMapper().readTree(requestBody).get("deviceId");
            if (deviceIdNode == null || !deviceIdNode.textValue().equals(deviceID)) {
                return PROXTOME_DENIED;
            } else {
                debug.message("ProxToMe User '{}' authenticated with success.", userID);
                return PROXTOME_OK;
            }
        } catch (IOException exc) {
            return PROXTOME_DOWN;
        }
    }

    /**
     * This method handles the communication with the ProxToMe Backend, 
     * to determine if the authenticated User is close to a ProxToMe enabled Device.
     * @param userID: The ProxToMe userID field created by the mobile SDK.
     * @param deviceID: The deviceID of the ProxToMe Device believed to be close to the User.
     * @param challenge: The challenge data used by ProxToMe to verify the proximity.
     * @param response: The response data used by ProxToMe to verify the proximity.
     * @param requestBody: The AM request body, used to match the ProxToMe response with what the User requested through AM.
     */
	public int handleAuthorization(String userID, String deviceID, String challenge, String response, String requestBody) {
        int statusCode = 0;
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost request = this.makeRequest(userID, deviceID, challenge, response);
        if (request == null) {
            return PROXTOME_DOWN;
        }
        try {
            statusCode = client.execute(request).getStatusLine().getStatusCode();
        } catch (IOException exc) {
            debug.message("Error in Request");
            return PROXTOME_DOWN;
        }
        debug.message("ProxToMe Request Executed. Status: " + String.valueOf(statusCode));
        if (statusCode == 200) {
            return this.handleResponse(requestBody, deviceID, userID);
        } else {
            return PROXTOME_DENIED;
        }
	}
}