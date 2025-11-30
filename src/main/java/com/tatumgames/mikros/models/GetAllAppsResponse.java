package com.tatumgames.mikros.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response wrapper for /getAllApps API endpoint.
 */
public class GetAllAppsResponse {
    private Status status;
    private Data data;
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public Data getData() {
        return data;
    }
    
    public void setData(Data data) {
        this.data = data;
    }
    
    public static class Status {
        @JsonProperty("statusCode")
        private int statusCode;
        
        @JsonProperty("statusMessage")
        private String statusMessage;
        
        public int getStatusCode() {
            return statusCode;
        }
        
        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }
        
        public String getStatusMessage() {
            return statusMessage;
        }
        
        public void setStatusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
        }
    }
    
    public static class Data {
        @JsonProperty("apps")
        private List<AppPromotion> apps;
        
        public List<AppPromotion> getApps() {
            return apps;
        }
        
        public void setApps(List<AppPromotion> apps) {
            this.apps = apps;
        }
    }
}

