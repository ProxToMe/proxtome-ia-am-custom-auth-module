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

package com.proxtome.ia.auth;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.HashMap;
import java.util.ResourceBundle;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;

import com.sun.identity.authentication.spi.AMLoginModule;
import com.sun.identity.authentication.spi.AuthLoginException;
import com.sun.identity.authentication.spi.InvalidPasswordException;
import com.sun.identity.authentication.util.ISAuthConstants;
import com.sun.identity.shared.datastruct.CollectionHelper;
import com.sun.identity.shared.debug.Debug;

import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.ClientProtocolException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
/**
 * SampleAuth authentication module example.
 *
 * If you create your own module based on this example, you must modify all
 * occurrences of "SampleAuth" in addition to changing the name of the class.
 *
 * Please refer to OpenAM documentation for further information.
 *
 * Feel free to look at the code for authentication modules delivered with
 * OpenAM, as they implement this same API.
 */
public class ProxToMeIAAuth extends AMLoginModule {

    // Name for the debug-log
    private final static String DEBUG_NAME = "ProxToMeIAAuth";
    private final static Debug debug = Debug.getInstance(DEBUG_NAME);

    // Name of the resource bundle
    private final static String amAuthProxToMeIAAuth = "amAuthProxToMeIAAuth";

    // Orders defined in the callbacks file
    private final static int STATE_BEGIN = 1;
    private final static int STATE_AUTH = 2;
    private final static int STATE_ERROR = 3;

    private Map<String, String> options;
    private ResourceBundle bundle;
    private Map<String, String> sharedState;
    private String username;

    public ProxToMeIAAuth() {
        super();
    }


    /**
     * This method stores service attributes and localized properties for later
     * use.
     * @param subject
     * @param sharedState
     * @param options
     */
    @Override
    public void init(Subject subject, Map sharedState, Map options) {

        debug.message("ProxToMeIAAuth::init");

        this.options = options;
        this.sharedState = sharedState;
        this.bundle = amCache.getResBundle(amAuthProxToMeIAAuth, getLoginLocale());
        this.username = null;
    }

    @Override
    public int process(Callback[] callbacks, int state) throws LoginException {

        debug.message("ProxToMeIAAuth::process state: {}", state);

        switch (state) {

            case STATE_BEGIN:
                // No time wasted here - simply modify the UI and
                // proceed to next state
                if (this.username == null && this.sharedState != null) { 
                    this.username = this.sharedState.get(getUserKey()); 
                }
                substituteUIStrings();
                return STATE_AUTH;

            case STATE_AUTH:
                // Get data from callbacks. Refer to callbacks XML file.
                NameCallback userIDCb = (NameCallback) callbacks[0];
                NameCallback deviceIDCb = (NameCallback) callbacks[1];
                PasswordCallback challengeCb = (PasswordCallback) callbacks[2];
                PasswordCallback responseCb = (PasswordCallback) callbacks[3];
                String userID = userIDCb.getName();
                String deviceID = deviceIDCb.getName();
                String challenge = String.valueOf(challengeCb.getPassword());
                String response = String.valueOf(responseCb.getPassword());
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
                    throw new InvalidPasswordException("serialization error");
                }
                try {
                    statusCode = Request.Post("http://proxtome-ia.cloudapp.net/api/challenge")
                                           .useExpectContinue()
                                           .bodyString(jsonPayload, ContentType.APPLICATION_JSON)
                                           .execute().returnResponse().getStatusLine().getStatusCode();
                    // String result = Request.Post("http://proxtome-ia.cloudapp.net/api/challenge")
                    //                        .useExpectContinue()
                    //                        .bodyString(jsonPayload, ContentType.APPLICATION_JSON)
                    //                        .execute().returnContent().asString();
                    // Map<String, String> decodedResult = new HashMap<Stirng, String>();
                    // decodedResult = new ObjectMapper().readValue(result, new TypeReference<HashMap>(){});
                } catch (IOException exc) {
                    throw new InvalidPasswordException("HTTP error");
                }

                if (!userID.equals(deviceID) && challenge.equals(response) && statusCode == 200) {
                    debug.message("ProxToMeIAAuth::process User '{}' " +
                            "authenticated with success.", userID);
                    return ISAuthConstants.LOGIN_SUCCEED;
                }

                throw new InvalidPasswordException("something is wrong",
                        userID);

            case STATE_ERROR:
                return STATE_ERROR;
            default:
                throw new AuthLoginException("invalid state");
        }
    }

    @Override
    public Principal getPrincipal() {
        return new ProxToMeIAAuthPrincipal(this.username);
    }

    private void setErrorText(String err) throws AuthLoginException {
        // Receive correct string from properties and substitute the
        // header in callbacks order 3.
        substituteHeader(STATE_ERROR, bundle.getString(err));
    }

    private void substituteUIStrings() throws AuthLoginException {
        // Get service specific attribute configured in OpenAM
        String ssa = CollectionHelper.getMapAttr(options, "specificAttribute");

        // Get property from bundle
        String new_hdr = ssa + " " +
                bundle.getString("proxtome-ia-auth-header");
        substituteHeader(STATE_AUTH, new_hdr);

        replaceCallback(STATE_AUTH, 0, new NameCallback(
                bundle.getString("proxtome-ia-userid-prompt")));
        replaceCallback(STATE_AUTH, 1, new NameCallback(
                bundle.getString("proxtome-ia-deviceid-prompt")));
        replaceCallback(STATE_AUTH, 2, new PasswordCallback(
                bundle.getString("proxtome-ia-challenge-prompt"), false));
        replaceCallback(STATE_AUTH, 3, new PasswordCallback(
                bundle.getString("proxtome-ia-response-prompt"), false));
    }
}
