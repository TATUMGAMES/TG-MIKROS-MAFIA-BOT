package com.tatumgames.mikros.models.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response model for /trackPlayerRating API endpoint.
 */
public class TrackPlayerRatingResponse {
    @JsonProperty("status")
    private Status status;
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
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
}

