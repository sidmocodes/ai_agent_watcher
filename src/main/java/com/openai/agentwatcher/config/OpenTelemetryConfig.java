package com.openai.agentwatcher.config;

import com.azure.monitor.opentelemetry.exporter.AzureMonitorExporterBuilder;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenTelemetry with Azure Application Insights
 */
@Configuration
public class OpenTelemetryConfig {

    @Value("${azure.application-insights.connection-string:}")
    private String connectionString;

    @Value("${otel.service.name:agent-watcher}")
    private String serviceName;

    @Bean
    public OpenTelemetry openTelemetry() {
        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.builder()
                        .put(AttributeKey.stringKey("service.name"), serviceName)
                        .put(AttributeKey.stringKey("service.version"), "1.0.0")
                        .put(AttributeKey.stringKey("deployment.environment"), "production")
                        .build()));

        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(
                        BatchSpanProcessor.builder(
                                new AzureMonitorExporterBuilder()
                                        .connectionString(connectionString)
                                        .buildTraceExporter()
                        ).build()
                )
                .build();

        return OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .buildAndRegisterGlobal();
    }

    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer("com.openai.agentwatcher", "1.0.0");
    }
}
