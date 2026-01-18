# OpenAI Agent Watcher

A Spring Boot service for monitoring and logging OpenAI agent telemetry and observability data. This service captures what AI agents are "thinking" and doing during their computation processes.

## Features

- **Agent Session Management**: Track complete agent sessions from start to finish
- **Thought Logging**: Capture agent reasoning, planning, and decision-making processes
- **Action Tracking**: Monitor agent actions including API calls, tool usage, and computations
- **Telemetry Collection**: Gather performance metrics like latency, token usage, and costs
- **REST API**: Comprehensive endpoints for submitting and querying agent data
- **Real-time Streaming**: Support for streaming agent events from OpenAI

## Architecture

### Model Classes
- `AgentSession`: Represents a complete agent interaction session
- `AgentThought`: Captures agent reasoning and thought processes
- `AgentAction`: Tracks agent actions and operations
- `AgentTelemetry`: Stores performance and usage metrics

### Service Classes
- `AgentWatcherService`: Core service for logging and retrieving agent data
- `AgentEventParserService`: Parses and interprets OpenAI agent events
- `OpenAIStreamService`: Handles streaming connections to OpenAI API

### Controllers
- `AgentSessionController`: REST endpoints for session management
- `TelemetryController`: REST endpoints for receiving telemetry events

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- OpenAI API key (optional for streaming)

### Configuration

Set your OpenAI API key as an environment variable:
```bash
export OPENAI_API_KEY=your-api-key-here
```

Or configure it in `application.properties`:
```properties
openai.api.key=your-api-key-here
```

### Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The service will start on `http://localhost:8080/api`

### H2 Console
Access the H2 database console at: `http://localhost:8080/api/h2-console`
- JDBC URL: `jdbc:h2:mem:agentwatcher`
- Username: `sa`
- Password: (empty)

## API Endpoints

### Session Management

**Start a new session:**
```bash
POST /api/sessions/start
Content-Type: application/json

{
  "agentId": "agent-123",
  "userQuery": "Analyze the latest sales data"
}
```

**Get session details:**
```bash
GET /api/sessions/{sessionId}
```

**Get session thoughts:**
```bash
GET /api/sessions/{sessionId}/thoughts
```

**Get session actions:**
```bash
GET /api/sessions/{sessionId}/actions
```

**Complete a session:**
```bash
POST /api/sessions/{sessionId}/complete
Content-Type: application/json

{
  "finalResponse": "Analysis complete..."
}
```

### Telemetry

**Submit an event:**
```bash
POST /api/telemetry/events
Content-Type: application/json

{
  "agentId": "agent-123",
  "sessionId": "session-456",
  "type": "thought",
  "thought_type": "PLANNING",
  "content": "I need to analyze the data using statistical methods",
  "confidence": 0.95
}
```

**Log a thought:**
```bash
POST /api/telemetry/thoughts
Content-Type: application/json

{
  "agentId": "agent-123",
  "sessionId": "session-456",
  "thoughtType": "REASONING",
  "content": "Based on the data, I should use linear regression",
  "confidence": 0.87
}
```

**Log an action:**
```bash
POST /api/telemetry/actions
Content-Type: application/json

{
  "agentId": "agent-123",
  "sessionId": "session-456",
  "actionType": "API_CALL",
  "actionName": "fetchSalesData",
  "inputData": "{\"dateRange\": \"2024-01\"}"
}
```

**Log a metric:**
```bash
POST /api/telemetry/metrics
Content-Type: application/json

{
  "agentId": "agent-123",
  "sessionId": "session-456",
  "metricName": "api_latency",
  "metricValue": 245.5,
  "metricUnit": "ms",
  "metricType": "LATENCY"
}
```

## Event Types

### Thought Types
- `PLANNING`: Strategic planning and approach
- `REASONING`: Logical reasoning and deduction
- `TOOL_SELECTION`: Choosing appropriate tools
- `EXECUTION`: Execution planning
- `REFLECTION`: Self-reflection and learning

### Action Types
- `API_CALL`: External API calls
- `TOOL_USE`: Tool or function usage
- `COMPUTATION`: Internal computations
- `DATA_RETRIEVAL`: Data fetching operations

### Metric Types
- `LATENCY`: Time-based metrics
- `TOKENS`: Token usage
- `COST`: Cost tracking
- `ERROR_RATE`: Error tracking

## Monitoring

Health check: `http://localhost:8080/api/actuator/health`
Metrics: `http://localhost:8080/api/actuator/metrics`

## Development

### Database Schema
The application uses JPA with automatic schema generation. On startup, tables are created for:
- `agent_sessions`
- `agent_thoughts`
- `agent_actions`
- `agent_telemetry`

### Logging
Logs are written to:
- Console (DEBUG level for application, INFO for Spring)
- File: `logs/agent-watcher.log`

## Future Enhancements

- WebSocket support for real-time updates
- Advanced analytics and visualization
- Integration with monitoring platforms (Prometheus, Grafana)
- Support for multiple LLM providers
- Enhanced search and filtering capabilities

## License

This project is provided as-is for monitoring and observability purposes.
