package com.infinity.gateway.filters;


import com.infinity.gateway.config.OTelCustomSpanProcessorImpl;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Order(1)
@Component
@RequiredArgsConstructor
public class TrackingFilter implements GlobalFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrackingFilter.class);
    private final FilterUtils filterUtils;
    private final OTelCustomSpanProcessorImpl customSpanProcessor;
    private final Tracer tracer;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
        if (isCorrelationIdPresent(requestHeaders)) {
            LOGGER.debug("tmx-correlation-id found in tracking filter: {}. ",
                    filterUtils.getCorrelationId(requestHeaders));
        } else {
            String traceId = customSpanProcessor.getTraceId();
            exchange = filterUtils.setCorrelationId(exchange, traceId);
            LOGGER.debug("tmx-correlation-id added in tracking filter :{}.traceId1{}", traceId);
        }
        System.out.println("The authentication name from the token is : " + getAuthenticationName(requestHeaders));
        return chain.filter(exchange);
    }

    private boolean isCorrelationIdPresent(HttpHeaders requestHeaders) {
        if (filterUtils.getCorrelationId(requestHeaders) != null) {
            return true;
        } else {
            return false;
        }
    }

    private String generateCorrelationId() {
        return java.util.UUID.randomUUID().toString();
    }

    private String getAuthenticationName(HttpHeaders requestHeaders){
        String authenticationName = "";
        if (filterUtils.getAuthToken(requestHeaders)!=null){
            String authToken = filterUtils.getAuthToken(requestHeaders).replace("Bearer ","");
            JSONObject jsonObj = decodeJWT(authToken);
            authenticationName = jsonObj.getString(FilterUtils.AUTHENTICATION_PRINCIPALS);
        }
        return authenticationName;
    }


    private JSONObject decodeJWT(String JWTToken) {
        String[] split_string = JWTToken.split("\\.");
        String base64EncodedBody = split_string[1];
        Base64 base64Url = new Base64(true);
        String body = new String(base64Url.decode(base64EncodedBody));
        JSONObject jsonObj = new JSONObject(body);
        return jsonObj;
    }
}
