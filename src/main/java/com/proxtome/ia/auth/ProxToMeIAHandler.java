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
import java.util.Map;
import java.util.HashMap;

import com.sun.identity.shared.debug.Debug;

import org.apache.http.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;


public class ProxToMeIAHandler {
	// Name for the debug-log
    private final static String DEBUG_NAME = "ProxToMeIAAuth";
    private final static Debug debug = Debug.getInstance(DEBUG_NAME);

    // Return codes
    private final static int PROXTOME_OK = 0;
    private final static int PROXTOME_SERVER_DOWN = 1;
    private final static int PROXTOME_SERVER_DENIED = 2;

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
		Map<String, String> payload = new HashMap<String, String>();
        payload.put("p2m_user_id", userID);
        payload.put("p2m_device_id", deviceID);
        payload.put("challenge", challenge);
        payload.put("response", response);
        String jsonPayload = null;
        int statusCode = 0;
        try {
            jsonPayload = new ObjectMapper().writeValueAsString(payload);
        } catch (JsonProcessingException exc) {
        	debug.message("JSON Serialization Error");
            return PROXTOME_SERVER_DOWN;
        }
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost request = new HttpPost ("http://proxtome-ia.cloudapp.net/api/response");
            request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            request.setEntity(new StringEntity(jsonPayload));
            statusCode = client.execute(request).getStatusLine().getStatusCode();
        } catch (IOException exc) {
            debug.message("Error in Request");
            return PROXTOME_SERVER_DOWN;
        } 
        debug.message("ProxToMe Request Executed. Status: " + String.valueOf(statusCode));
        if (statusCode == 200) {
            try {
                JsonNode deviceIdNode = new ObjectMapper().readTree(requestBody).get("deviceId");
                if (deviceIdNode == null || !deviceIdNode.textValue().equals(deviceID)) {
                    return PROXTOME_SERVER_DENIED;
                } else {
                	debug.message("ProxToMe User '{}' authenticated with success.", userID);
            		return PROXTOME_OK;
                }
            } catch (IOException exc) {
                return PROXTOME_SERVER_DOWN;
            }
        } else {
            return PROXTOME_SERVER_DENIED;
        }
	}