package com.example.soundCloud_BE.security;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import com.google.firebase.auth.FirebaseToken;

public class FirebaseAuthenticationToken extends AbstractAuthenticationToken {

    private final String uid;
    private final FirebaseToken token;

    public FirebaseAuthenticationToken(String uid, FirebaseToken token) {
        super(null);
        this.uid = uid;
        this.token = token;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return uid;
    }

    public FirebaseToken getToken() {
        return token;
    }
}