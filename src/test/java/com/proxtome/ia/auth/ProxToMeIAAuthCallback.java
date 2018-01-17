package com.proxtome.ia.auth;

import java.util.List;

public class ProxToMeIAAuthCallback {

    private String authId;
    private String template;
    private String stage;
    private String header;
    private List<Callback> callbacks;

    public String getAuthId() {
        return authId;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public List<Callback> getCallbacks() {
        return callbacks;
    }

    public void setCallbacks(List<Callback> callbacks) {
        this.callbacks = callbacks;
    }

    public void setCredentials(final String userID, final String deviceID, final String challenge, final String response) {
        Callback userIdCallback = this.getCallbacks().get(0);
        Callback deviceIdCallback = this.getCallbacks().get(1);
        Callback challengeCallback = this.getCallbacks().get(2);
        Callback responseCallback = this.getCallbacks().get(3);
        userIdCallback.setInputValue(userID);
        deviceIdCallback.setInputValue(deviceID);
        challengeCallback.setInputValue(challenge);
        responseCallback.setInputValue(response);
    }

    @Override
    public String toString() {
        return "ProxToMeIAAuthCallback{" +
                "authId='" + authId + '\'' +
                ", template='" + template + '\'' +
                ", stage='" + stage + '\'' +
                ", header='" + header + '\'' +
                ", callbacks=" + callbacks +
                '}';
    }
}