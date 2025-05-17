package com.example.soundCloud_BE.zingMp3.Dto;

import lombok.Data;

@Data
public class SyncResponse {
    private boolean success;
    private String zingId;
    private String message;

    public static SyncResponse success(String zingId) {
        SyncResponse response = new SyncResponse();
        response.success = true;
        response.zingId = zingId;
        return response;
    }

    public static SyncResponse failure(String message) {
        SyncResponse response = new SyncResponse();
        response.success = false;
        response.message = message;
        return response;
    }
}