package com.infinity.gateway.config;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@Slf4j
public class OTelCustomSpanProcessorImpl implements SpanProcessor {
    private String traceId = "";

    public final String getTraceId() {
        return this.traceId;
    }
    public final void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    @Override
    public void onStart(Context context, ReadWriteSpan span) {
        span.setAttribute("client.language", "english");
        span.setAttribute("client.country", "sweden");
        setTraceId(span.getSpanContext().getTraceId());
        log.debug("add custom tag on span starts with traceId:{}", span.getSpanContext().getTraceId());
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan readableSpan) {

    }

    @Override
    public boolean isEndRequired() {
        return false;
    }
}
