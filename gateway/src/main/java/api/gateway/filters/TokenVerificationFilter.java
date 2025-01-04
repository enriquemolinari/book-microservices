package api.gateway.filters;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Component
class TokenVerificationFilter extends AbstractGatewayFilterFactory<TokenVerificationFilter.Config> {

    public static final int INDEX_TOKEN_COOKIE = 0;
    public static final String DEFAULT_ERROR_MSG = "Something went wrong...";
    public static final String NO_TOKEN_PRESENT_MSG = "Authentication is required";
    public static final String APPLICATION_JSON_CONTENT_TYPE = "application/json";
    private final TokenVerification tokenVerification;
    @Value("${forward.requestHeaderUserId}")
    private String USER_ID_HEADER_NAME;
    @Value("${users.tokenCookieParamName}")
    private String TOKEN_COOKIE_PARAM_NAME;

    public TokenVerificationFilter(TokenVerification tokenVerification) {
        super(Config.class);
        this.tokenVerification = tokenVerification;
    }

    private Mono<Void> buildResponseBodyOnFailure(ServerWebExchange exchange, TokenVerificationResult result) {
        if (HttpStatusCode.UNAUTHORIZED.equals(result.statusCode())) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        } else {
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_CONTENT_TYPE);
        String errorMsg = result.errorMsg() == null ? DEFAULT_ERROR_MSG : result.errorMsg();
        String jsonRespond = "{\"message\": \"" + errorMsg + "\"}";
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(jsonRespond.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            var possiblyAToken = obtainTokenFromCookie(exchange);
            return forwardIfTokenOkRespondFailureIfNotValid(exchange, chain, possiblyAToken);
        };
    }

    private Mono<Void> forwardIfTokenOkRespondFailureIfNotValid(ServerWebExchange exchange, GatewayFilterChain chain, Optional<String> possiblyAToken) {
        return possiblyAToken.map(token -> {
            var result = tokenVerification.verify(token);
            if (HttpStatusCode.OK.equals(result.statusCode())) {
                ServerHttpRequest request = exchange.getRequest()
                        .mutate()
                        .header(USER_ID_HEADER_NAME, result.userId())
                        .build();
                return chain.filter(exchange.mutate().request(request).build());
            }
            return buildResponseBodyOnFailure(exchange, result);
        }).orElseGet(() -> {
            return buildResponseBodyOnFailure(exchange,
                    TokenVerificationResult.failure(HttpStatusCode.UNAUTHORIZED.code(), NO_TOKEN_PRESENT_MSG));
        });
    }

    private Optional<String> obtainTokenFromCookie(ServerWebExchange exchange) {
        List<HttpCookie> cookiesWithNameToken = exchange.getRequest().getCookies().get(TOKEN_COOKIE_PARAM_NAME);
        if (cookiesWithNameToken == null || cookiesWithNameToken.size() != 1) {
            return Optional.empty();
        }
        HttpCookie tokenCookie = cookiesWithNameToken.get(INDEX_TOKEN_COOKIE);
        return Optional.of(tokenCookie.getValue());
    }

    protected static class Config {
    }
}
