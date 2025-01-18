package api.gateway.filters.security;

interface TokenVerification {
    TokenVerificationResult verify(String token);
}
