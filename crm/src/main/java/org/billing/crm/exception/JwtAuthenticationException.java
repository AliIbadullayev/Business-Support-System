package org.billing.crm.exception;

public class JwtAuthenticationException extends RuntimeException {
    public JwtAuthenticationException(String cannotAuthenticateRefreshToken) {
        super(cannotAuthenticateRefreshToken);
    }
}
