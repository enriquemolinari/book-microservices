package api.gateway.filters;

interface TokenVerification {
    TokenVerificationResult verify(String token);
}
