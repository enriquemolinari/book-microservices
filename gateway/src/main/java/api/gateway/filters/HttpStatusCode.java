package api.gateway.filters;

enum HttpStatusCode {
    OK(200),
    UNAUTHORIZED(401),
    INTERNAL_SERVER_ERROR(500);

    private final int code;

    HttpStatusCode(int code) {
        this.code = code;
    }

    public static HttpStatusCode fromCode(int code) {
        for (HttpStatusCode status : HttpStatusCode.values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Http Code not supported : " + code);
    }

    public int code() {
        return code;
    }
}
