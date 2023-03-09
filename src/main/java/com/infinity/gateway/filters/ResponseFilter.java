package com.infinity.gateway.filters;

import io.opentelemetry.api.trace.Span;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ResponseFilter {

    public final FilterUtils filterUtils;
    private final Logger LOGGER = log;

    @Bean
    public GlobalFilter postGlobalFilter() {
        return (exchange, chain) -> chain
                .filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
                    String correlationId = FilterUtils.getCorrelationId(requestHeaders);
                    LOGGER.debug("Adding the correlation id to the outbound headers. {}", correlationId);
                    exchange.getResponse().getHeaders().add(FilterUtils.CORRELATION_ID, correlationId);
                    LOGGER.debug("Completing outgoing request for {}.", exchange.getRequest().getURI());
                }));
    }
}
