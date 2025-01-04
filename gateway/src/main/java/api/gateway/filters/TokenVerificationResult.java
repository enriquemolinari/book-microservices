package api.gateway.filters;

record TokenVerificationResult(HttpStatusCode statusCode, String errorMsg,
                               String userId) {

    static TokenVerificationResult success(String userId) {
        return new TokenVerificationResult(HttpStatusCode.OK, "", userId);
    }

    static TokenVerificationResult failure(int httpStatusCode, String errorMsg) {
        return new TokenVerificationResult(HttpStatusCode.fromCode(httpStatusCode), errorMsg, "");
    }
}
