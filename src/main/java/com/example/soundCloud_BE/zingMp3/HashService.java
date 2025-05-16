package com.example.soundCloud_BE.zingMp3;


import jdk.jfr.consumer.RecordedStackTrace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Component
public class HashService {

    @Value("${zing-mp3.BASE_URL}")
    private String BASE_URL;
    @Value("${zing-mp3.API_KEY}")
    private String API_KEY;
    @Value("${zing-mp3.SECRET_KEY}")
    private String SECRET_KEY;
    @Value("${zing-mp3.VERSION}")
    private String VERSION;
    @Value("${zing-mp3.COOKIE_PATH}")
    private String COOKIE_PATH;

    private String CTIME = String.valueOf(Instant.now().getEpochSecond());

    public String getCTIME(){return this.CTIME;}

    public String getHash256(String str) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(str.getBytes("UTF-8"));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error while hashing with SHA-256", e);
        }
    }

    public String getHmac512(String str, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA512");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(str.getBytes("UTF-8"));
            return bytesToHex(hmacBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error while creating HMAC-SHA512", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));  // convert each byte to hex
        }
        return result.toString();
    }


    public String hashParamNoId(String path){
        return getHmac512(
                path + getHash256(String.format("ctime=%sversion=%s",CTIME, VERSION)),
                        SECRET_KEY
        );
    }

    public String hashParamWithId(String path, String id){
        return getHmac512(
                path + getHash256(String.format("ctime=%sid=%sversion=%s",CTIME, id,VERSION)),
                        SECRET_KEY
        );
    }

    public String hashListMV(String path, String id, String type, String page, String count){
        return getHmac512(
                path + getHash256(String.format("count=%sctime=%sid=%spage=%stype=%sversion=%s",count, CTIME, id, page, type, VERSION)),
                        SECRET_KEY
        );
    }









}
