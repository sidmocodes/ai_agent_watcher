# Azure Application Insights & OpenTelemetry Integration

This project integrates Azure Application Insights and OpenTelemetry for comprehensive observability, distributed tracing, and performance monitoring.

## Overview

The service uses:
- **Azure Application Insights** - Cloud-based APM service for monitoring
- **OpenTelemetry** - Vendor-neutral observability framework for traces, metrics, and logs
- **Micrometer** - Application metrics facade for Azure Monitor integration

## Setup

### 1. Azure Application Insights Configuration

#### Create Application Insights Resource

```bash
# Using Azure CLI
az group create --name agent-watcher-rg --location eastus

az monitor app-insights component create \
  --app agent-watcher \
  --location eastus \
  --resource-group agent-watcher-rg \
  --application-type web

# Get the instrumentation key and connection string
az monitor app-insights component show \
  --app agent-watcher \
  --resource-group agent-watcher-rg \
  --query '[instrumentationKey,connectionString]' \
  --output tsv
```

#### Set Environment Variables

```bash
# Add to your environment or .env file
export APPINSIGHTS_INSTRUMENTATION_KEY="your-instrumentation-key"
export APPINSIGHTS_CONNECTION_STRING="InstrumentationKey=your-key;IngestionEndpoint=https://your-region.in.applicationinsights.azure.com/;LiveEndpoint=https://your-region.livediagnostics.monitor.azure.com/"
```

### 2. Application Configuration

Configure in [`application.properties`](src/main/resources/application.properties):

```properties
# Azure Application Insights
azure.application-insights.instrumentation-key=${APPINSIGHTS_INSTRUMENTATION_KEY:}
azure.application-insights.connection-string=${APPINSIGHTS_CONNECTION_STRING:}
azure.application-insights.enabled=true

# OpenTelemetry
otel.service.name=agent-watcher
otel.traces.exporter=azure
otel.metrics.exporter=azure
otel.logs.exporter=azure

# Micrometer Azure Monitor
management.metrics.export.azuremonitor.enabled=true
management.metrics.export.azuremonitor.instrumentation-key=${APPINSIGHTS_INSTRUMENTATION_KEY:}
```

### 3. Run the Application

```bash
# Development
mvn spring-boot:run

# Production with Azure configuration
APPINSIGHTS_INSTRUMENTATION_KEY=your-key mvn spring-boot:run
```

## Features

### Distributed Tracing

Every agent session, thought, and action is automatically traced with:
- **Span IDs** - Unique identifiers for each operation
- **Trace IDs** - Links related operations across the system
- **Attributes** - Custom metadata (agent_id, session_id, thought_type, etc.)
- **Events** - Key milestones within operations
- **Exceptions** - Automatic error capture and stack traces

Example trace attributes:
```java
Span span = tracer.spanBuilder("startSession")
    .setAttribute("agent.id", agentId)
    .setAttribute("session.id", sessionId)
    .startSpan();
```

### Custom Metrics

The service tracks custom metrics including:

#### Counters
- `agent.session.started` - Total sessions started (tagged by agent_id)
- `agent.thought.logged` - Total thoughts logged (tagged by agent_id, thought_type)
- `agent.action.started` - Total actions started (tagged by agent_id, action_type)
- `agent.session.completed` - Total sessions completed

#### Gauges
- `agent.thought.confidence` - Current thought confidence score
- `agent.session.duration` - Session duration in seconds
- `agent.action.duration_ms` - Action duration in milliseconds

#### Timers
- `agent.session.timer` - Session execution time distribution
- `agent.action.timer` - Action execution time distribution

### Automatic Instrumentation

OpenTelemetry automatically instruments:
- **HTTP Requests** - All incoming REST API calls
- **Database Queries** - JPA/Hibernate database operations  
- **WebClient Calls** - Outbound HTTP requests to OpenAI API
- **Async Operations** - Background tasks and reactive streams

## Viewing Telemetry Data

### Azure Portal

1. Navigate to your Application Insights resource
2. Access telemetry views:

#### **Application Map**
- Visualize distributed service dependencies
- See call relationships between components
- Identify performance bottlenecks

#### **Performance**
- View operation duration percentiles
- Analyze slow operations
- Drill into specific traces

#### **Failures**
- Monitor exception rates
- View exception details and stack traces
- Correlate failures across operations

#### **Metrics**
- Create custom dashboards
- Query custom metrics
- Set up alerts

#### **Logs (Log Analytics)**
```kusto
// Query all session start events
traces
| where message contains "Started new agent session"
| project timestamp, message, customDimensions

// Query thought logging metrics
customMetrics
| where name == "agent.thought.logged"
| summarize count() by tostring(customDimensions.agent_id), bin(timestamp, 1h)

// Find slow operations
requests
| where duration > 1000
| project timestamp, name, duration, resultCode
| order by duration desc
```

### Local Development

Access metrics locally:
```bash
# Prometheus format (if enabled)
curl http://localhost:8080/api/actuator/metrics

# Specific metric
curl http://localhost:8080/api/actuator/metrics/agent.session.started

# Health check
curl http://localhost:8080/api/actuator/health
```

## Custom Instrumentation

### Adding Custom Spans

```java
@Service
public class MyService {
    
    @Autowired
    private Tracer tracer;
    
    public void myOperation() {
        Span span = tracer.spanBuilder("myOperation")
                .setAttribute("custom.attribute", "value")
                .startSpan();
        
        try (Scope scope = span.makeCurrent()) {
            // Your code here
            span.addEvent("Important milestone");
            
        } catch (Exception e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
}
```

### Adding Custom Metrics

```java
@Service
public class MyService {
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    public void trackMetric() {
        // Counter
        meterRegistry.counter("my.custom.counter", 
                "tag1", "value1").increment();
        
        // Gauge
        meterRegistry.gauge("my.custom.gauge", 
                gaugeValue);
        
        // Timer
        Timer.Sample sample = Timer.start(meterRegistry);
        // ... operation ...
        sample.stop(meterRegistry.timer("my.custom.timer"));
    }
}
```

## Alert Configuration

### Example Alerts

```bash
# High error rate
az monitor metrics alert create \
  --name agent-watcher-high-errors \
  --resource-group agent-watcher-rg \
  --scopes /subscriptions/{id}/resourceGroups/agent-watcher-rg/providers/microsoft.insights/components/agent-watcher \
  --condition "count requests/failed > 10" \
  --window-size 5m \
  --evaluation-frequency 1m

# Slow response time
az monitor metrics alert create \
  --name agent-watcher-slow-response \
  --resource-group agent-watcher-rg \
  --scopes /subscriptions/{id}/resourceGroups/agent-watcher-rg/providers/microsoft.insights/components/agent-watcher \
  --condition "avg requests/duration > 2000" \
  --window-size 5m
```

## Best Practices

1. **Use Meaningful Span Names** - Clearly indicate the operation
2. **Add Relevant Attributes** - Include context for debugging
3. **Record Exceptions** - Always capture errors in spans
4. **Use Structured Logging** - Include context in log messages
5. **Tag Metrics** - Add dimensions for filtering and grouping
6. **Set Sampling Rates** - Balance cost vs. observability in production
7. **Create Dashboards** - Build operational views for your team
8. **Configure Alerts** - Proactively monitor critical metrics

## Dependencies

```xml
<!-- Azure Application Insights -->
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>applicationinsights-spring-boot-starter</artifactId>
    <version>3.4.19</version>
</dependency>

<!-- OpenTelemetry -->
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-api</artifactId>
    <version>1.32.0</version>
</dependency>

<!-- Azure Monitor Exporter -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-monitor-opentelemetry-exporter</artifactId>
    <version>1.0.0-beta.18</version>
</dependency>

<!-- Micrometer Azure Monitor -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-azure-monitor</artifactId>
</dependency>
```

## Troubleshooting

### No Data in Azure Portal

1. Verify instrumentation key/connection string
2. Check network connectivity to Azure
3. Review application logs for export errors
4. Ensure `azure.application-insights.enabled=true`

### High Telemetry Volume

1. Configure sampling:
```properties
azure.application-insights.sampling.percentage=10
```

2. Filter specific operations:
```properties
otel.traces.sampler=traceidratio
otel.traces.sampler.arg=0.1
```

### Performance Impact

- Use async exporters (enabled by default)
- Configure appropriate batch sizes
- Monitor exporter queue sizes
- Consider sampling for high-volume scenarios

## References

- [Azure Application Insights Documentation](https://docs.microsoft.com/azure/azure-monitor/app/app-insights-overview)
- [OpenTelemetry Documentation](https://opentelemetry.io/docs/)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
