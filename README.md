# OpenAI Agent Watcher

A Spring Boot service for monitoring and logging OpenAI agent telemetry and observability data. This service captures what AI agents are "thinking" and doing during their computation processes.

## Features

- **Agent Session Management**: Track complete agent sessions from start to finish
- **Thought Logging**: Capture agent reasoning, planning, and decision-making processes
- **Action Tracking**: Monitor agent actions including API calls, tool usage, and computations
- **Telemetry Collection**: Gather performance metrics like latency, token usage, and costs
- **REST API**: Comprehensive endpoints for submitting and querying agent data
- **Real-time Streaming**: Support for streaming agent events from OpenAI
- **Azure Application Insights Integration**: Full observability with distributed tracing
- **OpenTelemetry Support**: Vendor-neutral instrumentation for traces and metrics
- **Database Migrations**: Liquibase for versioned schema management

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
- PostgreSQL (optional, for production use)

### Installation & Configuration

#### 1. Clone and Build

```bash
# Clone the repository
git clone https://github.com/sidmocodes/ai_agent_watcher.git
cd ai_agent_watcher

# Build the project
mvn clean install

# Or build without running tests
mvn clean install -DskipTests
```

#### 2. Environment Configuration

**Option A: Environment Variables**

```bash
# OpenAI API key (optional, for streaming support)
export OPENAI_API_KEY=your-api-key-here

# Azure Application Insights (optional, for cloud observability)
export APPINSIGHTS_CONNECTION_STRING=your-connection-string
export APPINSIGHTS_INSTRUMENTATION_KEY=your-instrumentation-key
```

**Option B: application.properties**

Edit `src/main/resources/application.properties`:

```properties
# OpenAI Configuration
openai.api.key=your-api-key-here
openai.api.url=https://api.openai.com/v1
openai.stream.enabled=true
openai.stream.buffer-size=1024

# Azure Monitor Configuration
azure.application-insights.connection-string=your-connection-string
azure.application-insights.instrumentation-key=your-key
azure.application-insights.enabled=true

# CORS Configuration (adjust for your frontend)
cors.allowed-origins=http://localhost:3000,http://localhost:4200
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
cors.allowed-headers=*
cors.allow-credentials=true
```

#### 3. Database Setup

**Development Mode (H2 In-Memory Database)**

The default configuration uses H2, which requires no setup. Just run the application.

**Production Mode (PostgreSQL)**

1. Install and start PostgreSQL:
```bash
# macOS
brew install postgresql
brew services start postgresql

# Create database
createdb agent_watcher
```

2. Update `application-postgres.properties` or create a custom profile:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/agent_watcher
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. Run with PostgreSQL profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

#### 4. Running the Application

**Development (H2 Database)**
```bash
mvn spring-boot:run
```

**Production (PostgreSQL)**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

**Using the JAR**
```bash
# Build the JAR
mvn clean package

# Run with H2
java -jar target/agent-watcher-1.0.0-SNAPSHOT.jar

# Run with PostgreSQL
java -jar target/agent-watcher-1.0.0-SNAPSHOT.jar --spring.profiles.active=postgres
```

**With Custom Port**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9090"
```

The service will start on `http://localhost:8080/api` (or your custom port)

#### 5. Verify Installation

Check the health endpoint:
```bash
curl http://localhost:8080/api/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

### Database Access

#### H2 Console (Development)
Access the H2 database console at: `http://localhost:8080/api/h2-console`

**Connection Settings:**
- JDBC URL: `jdbc:h2:mem:agentwatcher`
- Username: `sa`
- Password: (leave empty)
- Driver Class: `org.h2.Driver`

#### PostgreSQL (Production)
Connect using any PostgreSQL client:
```bash
psql -h localhost -U your_username -d agent_watcher
```

**Useful Queries:**
```sql
-- View all sessions
SELECT * FROM agent_sessions ORDER BY start_time DESC LIMIT 10;

-- View thoughts for a specific session
SELECT * FROM agent_thoughts WHERE session_id = 'your-session-id' ORDER BY timestamp;

-- View actions for a specific session
SELECT * FROM agent_actions WHERE session_id = 'your-session-id' ORDER BY start_time;

-- View telemetry metrics
SELECT * FROM agent_telemetry WHERE session_id = 'your-session-id' ORDER BY timestamp;
```

## Detailed API Usage Guide

### Complete Workflow Example

Here's a complete example of tracking an agent session from start to finish:

```bash
# 1. Start a new session
SESSION_RESPONSE=$(curl -X POST http://localhost:8080/api/sessions/start \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "my-sales-agent",
    "userQuery": "Analyze Q4 sales data and provide insights"
  }')

# Extract session ID (using jq)
SESSION_ID=$(echo $SESSION_RESPONSE | jq -r '.sessionId')
echo "Session ID: $SESSION_ID"

# 2. Log agent thoughts as it processes
curl -X POST http://localhost:8080/api/telemetry/thoughts \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "my-sales-agent",
    "sessionId": "'"$SESSION_ID"'",
    "thoughtType": "PLANNING",
    "content": "I need to fetch Q4 sales data from the database first",
    "confidence": 0.95
  }'

# 3. Log an action (API call)
curl -X POST http://localhost:8080/api/telemetry/actions \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "my-sales-agent",
    "sessionId": "'"$SESSION_ID"'",
    "actionType": "API_CALL",
    "actionName": "fetchSalesData",
    "inputData": "{\"quarter\": \"Q4\", \"year\": 2024}"
  }'

# 4. Log metrics (latency, tokens, etc.)
curl -X POST http://localhost:8080/api/telemetry/metrics \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "my-sales-agent",
    "sessionId": "'"$SESSION_ID"'",
    "metricName": "api_call_latency",
    "metricValue": 342.5,
    "metricUnit": "ms",
    "metricType": "LATENCY"
  }'

# 5. Log reasoning thought
curl -X POST http://localhost:8080/api/telemetry/thoughts \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "my-sales-agent",
    "sessionId": "'"$SESSION_ID"'",
    "thoughtType": "REASONING",
    "content": "Sales increased 23% compared to Q3. This correlates with holiday season and new marketing campaign",
    "confidence": 0.89
  }'

# 6. Log token usage
curl -X POST http://localhost:8080/api/telemetry/metrics \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "my-sales-agent",
    "sessionId": "'"$SESSION_ID"'",
    "metricName": "total_tokens",
    "metricValue": 1250,
    "metricUnit": "tokens",
    "metricType": "TOKENS"
  }'

# 7. Complete the session
curl -X POST http://localhost:8080/api/sessions/$SESSION_ID/complete \
  -H "Content-Type: application/json" \
  -d '{
    "finalResponse": "Q4 sales analysis complete. Revenue grew 23% to $5.2M, driven by holiday promotions and new product launches."
  }'

# 8. Query the complete session timeline
curl http://localhost:8080/api/sessions/$SESSION_ID | jq

# 9. Get all thoughts for this session
curl http://localhost:8080/api/sessions/$SESSION_ID/thoughts | jq

# 10. Get all actions for this session
curl http://localhost:8080/api/sessions/$SESSION_ID/actions | jq
```

### API Reference

#### Session Management Endpoints

##### 1. Start a New Session

**Endpoint:** `POST /api/sessions/start`

**Description:** Creates a new agent session and returns a unique session ID.

**Request Body:**
```json
{
  "agentId": "string (required)",
  "userQuery": "string (required)"
}
```

**Response:**
```json
{
  "id": 1,
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "agentId": "my-sales-agent",
  "userQuery": "Analyze Q4 sales data and provide insights",
  "sessionStatus": "ACTIVE",
  "startTime": "2026-01-19T10:30:00",
  "endTime": null,
  "totalThoughts": 0,
  "totalActions": 0,
  "finalResponse": null
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/sessions/start \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "customer-support-agent",
    "userQuery": "Handle customer refund request for order #12345"
  }'
```

##### 2. Get Session Details

**Endpoint:** `GET /api/sessions/{sessionId}`

**Description:** Retrieves complete session information.

**Response:**
```json
{
  "id": 1,
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "agentId": "my-sales-agent",
  "userQuery": "Analyze Q4 sales data",
  "sessionStatus": "COMPLETED",
  "startTime": "2026-01-19T10:30:00",
  "endTime": "2026-01-19T10:32:15",
  "totalThoughts": 5,
  "totalActions": 3,
  "finalResponse": "Analysis complete with insights."
}
```

**Example:**
```bash
curl http://localhost:8080/api/sessions/550e8400-e29b-41d4-a716-446655440000
```

##### 3. Get Session Thoughts

**Endpoint:** `GET /api/sessions/{sessionId}/thoughts`

**Description:** Retrieves all thoughts for a session in chronological order.

**Response:**
```json
[
  {
    "id": 1,
    "agentId": "my-sales-agent",
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "thoughtType": "PLANNING",
    "thoughtContent": "I need to fetch Q4 sales data from the database first",
    "confidenceScore": 0.95,
    "timestamp": "2026-01-19T10:30:05",
    "parentThoughtId": null,
    "metadata": null
  },
  {
    "id": 2,
    "agentId": "my-sales-agent",
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "thoughtType": "REASONING",
    "thoughtContent": "Sales increased 23% compared to Q3",
    "confidenceScore": 0.89,
    "timestamp": "2026-01-19T10:31:12",
    "parentThoughtId": null,
    "metadata": null
  }
]
```

**Example:**
```bash
curl http://localhost:8080/api/sessions/550e8400-e29b-41d4-a716-446655440000/thoughts | jq
```

##### 4. Get Session Actions

**Endpoint:** `GET /api/sessions/{sessionId}/actions`

**Description:** Retrieves all actions for a session in chronological order.

**Response:**
```json
[
  {
    "id": 1,
    "agentId": "my-sales-agent",
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "actionType": "API_CALL",
    "actionName": "fetchSalesData",
    "inputData": "{\"quarter\": \"Q4\", \"year\": 2024}",
    "outputData": "{\"revenue\": 5200000, \"growth\": 0.23}",
    "status": "COMPLETED",
    "startTime": "2026-01-19T10:30:10",
    "endTime": "2026-01-19T10:30:12",
    "durationMs": 2000,
    "errorMessage": null,
    "relatedThoughtId": null
  }
]
```

**Example:**
```bash
curl http://localhost:8080/api/sessions/550e8400-e29b-41d4-a716-446655440000/actions | jq
```

##### 5. Complete a Session

**Endpoint:** `POST /api/sessions/{sessionId}/complete`

**Description:** Marks a session as completed with a final response.

**Request Body:**
```json
{
  "finalResponse": "string (required)"
}
```

**Response:** `200 OK`

**Example:**
```bash
curl -X POST http://localhost:8080/api/sessions/550e8400-e29b-41d4-a716-446655440000/complete \
  -H "Content-Type: application/json" \
  -d '{
    "finalResponse": "Q4 sales analysis complete. Revenue grew 23% to $5.2M."
  }'
```

##### 6. Get All Sessions for an Agent

**Endpoint:** `GET /api/sessions/agent/{agentId}`

**Description:** Retrieves all sessions for a specific agent, ordered by start time (newest first).

**Response:**
```json
[
  {
    "id": 5,
    "sessionId": "550e8400-e29b-41d4-a716-446655440005",
    "agentId": "my-sales-agent",
    "userQuery": "Analyze Q4 sales data",
    "sessionStatus": "COMPLETED",
    "startTime": "2026-01-19T10:30:00",
    "endTime": "2026-01-19T10:32:15",
    "totalThoughts": 5,
    "totalActions": 3,
    "finalResponse": "Analysis complete."
  },
  {
    "id": 4,
    "sessionId": "550e8400-e29b-41d4-a716-446655440004",
    "agentId": "my-sales-agent",
    "userQuery": "Generate monthly report",
    "sessionStatus": "COMPLETED",
    "startTime": "2026-01-18T14:20:00",
    "endTime": "2026-01-18T14:25:30",
    "totalThoughts": 8,
    "totalActions": 5,
    "finalResponse": "Monthly report generated successfully."
  }
]
```

**Example:**
```bash
curl http://localhost:8080/api/sessions/agent/my-sales-agent | jq
```

#### Telemetry Endpoints

##### 7. Submit Generic Event

**Endpoint:** `POST /api/telemetry/events`

**Description:** Generic endpoint for submitting various types of telemetry events. The event type is determined by the `type` field.

**Request Body:**
```json
{
  "agentId": "string (required)",
  "sessionId": "string (required)",
  "type": "thought | action | metric (required)",
  
  // For type="thought"
  "thought_type": "PLANNING | REASONING | TOOL_SELECTION | EXECUTION | REFLECTION",
  "content": "string",
  "confidence": 0.0-1.0,
  
  // For type="action"
  "action_type": "API_CALL | TOOL_USE | COMPUTATION | DATA_RETRIEVAL",
  "action_name": "string",
  "input_data": "string (JSON)",
  
  // For type="metric"
  "metric_name": "string",
  "metric_value": "number",
  "metric_unit": "string",
  "metric_type": "LATENCY | TOKENS | COST | ERROR_RATE | CUSTOM"
}
```

**Examples:**

```bash
# Thought event
curl -X POST http://localhost:8080/api/telemetry/events \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "agent-123",
    "sessionId": "session-456",
    "type": "thought",
    "thought_type": "PLANNING",
    "content": "I will use the search tool first to gather information",
    "confidence": 0.92
  }'

# Action event
curl -X POST http://localhost:8080/api/telemetry/events \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "agent-123",
    "sessionId": "session-456",
    "type": "action",
    "action_type": "TOOL_USE",
    "action_name": "web_search",
    "input_data": "{\"query\": \"latest AI trends 2026\"}"
  }'

# Metric event
curl -X POST http://localhost:8080/api/telemetry/events \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "agent-123",
    "sessionId": "session-456",
    "type": "metric",
    "metric_name": "thinking_time",
    "metric_value": 1250.5,
    "metric_unit": "ms",
    "metric_type": "LATENCY"
  }'
```

##### 8. Log a Thought

**Endpoint:** `POST /api/telemetry/thoughts`

**Description:** Logs an agent thought or reasoning step.

**Request Body:**
```json
{
  "agentId": "string (required)",
  "sessionId": "string (required)",
  "thoughtType": "PLANNING | REASONING | TOOL_SELECTION | EXECUTION | REFLECTION | ERROR",
  "content": "string (required)",
  "confidence": "number (0.0-1.0, optional)"
}
```

**Response:** `200 OK` with message `"Thought logged"`

**Examples:**

```bash
# Planning thought
curl -X POST http://localhost:8080/api/telemetry/thoughts \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "research-agent",
    "sessionId": "session-789",
    "thoughtType": "PLANNING",
    "content": "To answer this question, I need to: 1) Search recent papers, 2) Summarize findings, 3) Compare approaches",
    "confidence": 0.88
  }'

# Reasoning thought
curl -X POST http://localhost:8080/api/telemetry/thoughts \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "research-agent",
    "sessionId": "session-789",
    "thoughtType": "REASONING",
    "content": "Based on the search results, transformer models dominate the landscape with 78% adoption",
    "confidence": 0.94
  }'

# Tool selection thought
curl -X POST http://localhost:8080/api/telemetry/thoughts \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "research-agent",
    "sessionId": "session-789",
    "thoughtType": "TOOL_SELECTION",
    "content": "I should use the academic_search tool because it has access to verified papers",
    "confidence": 0.91
  }'

# Error thought
curl -X POST http://localhost:8080/api/telemetry/thoughts \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "research-agent",
    "sessionId": "session-789",
    "thoughtType": "ERROR",
    "content": "API rate limit exceeded. Will retry after 60 seconds.",
    "confidence": 0.0
  }'
```

##### 9. Log an Action

**Endpoint:** `POST /api/telemetry/actions`

**Description:** Logs an agent action such as tool usage, API call, or computation.

**Request Body:**
```json
{
  "agentId": "string (required)",
  "sessionId": "string (required)",
  "actionType": "API_CALL | TOOL_USE | COMPUTATION | DATA_RETRIEVAL",
  "actionName": "string (required)",
  "inputData": "string (optional, JSON format)"
}
```

**Response:** `200 OK` with message `"Action logged"`

**Examples:**

```bash
# Tool usage
curl -X POST http://localhost:8080/api/telemetry/actions \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "coding-agent",
    "sessionId": "session-999",
    "actionType": "TOOL_USE",
    "actionName": "code_interpreter",
    "inputData": "{\"code\": \"import pandas as pd\\ndf = pd.read_csv(data.csv)\", \"language\": \"python\"}"
  }'

# API call
curl -X POST http://localhost:8080/api/telemetry/actions \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "weather-agent",
    "sessionId": "session-111",
    "actionType": "API_CALL",
    "actionName": "get_weather",
    "inputData": "{\"city\": \"San Francisco\", \"units\": \"metric\"}"
  }'

# Computation
curl -X POST http://localhost:8080/api/telemetry/actions \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "math-agent",
    "sessionId": "session-222",
    "actionType": "COMPUTATION",
    "actionName": "calculate_statistics",
    "inputData": "{\"dataset\": [1,2,3,4,5], \"operation\": \"mean\"}"
  }'
```

##### 10. Log a Metric

**Endpoint:** `POST /api/telemetry/metrics`

**Description:** Logs performance or usage metrics.

**Request Body:**
```json
{
  "agentId": "string (required)",
  "sessionId": "string (required)",
  "metricName": "string (required)",
  "metricValue": "number (required)",
  "metricUnit": "string (optional, default: '')",
  "metricType": "LATENCY | TOKENS | COST | ERROR_RATE | CUSTOM (optional, default: CUSTOM)"
}
```

**Response:** `200 OK` with message `"Metric logged"`

**Examples:**

```bash
# Latency metric
curl -X POST http://localhost:8080/api/telemetry/metrics \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "agent-123",
    "sessionId": "session-456",
    "metricName": "llm_response_time",
    "metricValue": 2350.5,
    "metricUnit": "ms",
    "metricType": "LATENCY"
  }'

# Token usage
curl -X POST http://localhost:8080/api/telemetry/metrics \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "agent-123",
    "sessionId": "session-456",
    "metricName": "prompt_tokens",
    "metricValue": 450,
    "metricUnit": "tokens",
    "metricType": "TOKENS"
  }'

# Cost tracking
curl -X POST http://localhost:8080/api/telemetry/metrics \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "agent-123",
    "sessionId": "session-456",
    "metricName": "api_cost",
    "metricValue": 0.0523,
    "metricUnit": "usd",
    "metricType": "COST"
  }'

# Error rate
curl -X POST http://localhost:8080/api/telemetry/metrics \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "agent-123",
    "sessionId": "session-456",
    "metricName": "retry_count",
    "metricValue": 2,
    "metricUnit": "count",
    "metricType": "ERROR_RATE"
  }'

# Custom metric
curl -X POST http://localhost:8080/api/telemetry/metrics \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "agent-123",
    "sessionId": "session-456",
    "metricName": "context_window_usage",
    "metricValue": 0.73,
    "metricUnit": "percentage",
    "metricType": "CUSTOM"
  }'
```

## Integration Patterns

### Pattern 1: Python Agent Integration

```python
import requests
import json
from datetime import datetime

class AgentWatcher:
    def __init__(self, base_url="http://localhost:8080/api", agent_id="my-agent"):
        self.base_url = base_url
        self.agent_id = agent_id
        self.session_id = None
    
    def start_session(self, user_query):
        """Start a new agent session"""
        response = requests.post(
            f"{self.base_url}/sessions/start",
            json={"agentId": self.agent_id, "userQuery": user_query}
        )
        data = response.json()
        self.session_id = data['sessionId']
        return self.session_id
    
    def log_thought(self, content, thought_type="REASONING", confidence=None):
        """Log an agent thought"""
        payload = {
            "agentId": self.agent_id,
            "sessionId": self.session_id,
            "thoughtType": thought_type,
            "content": content
        }
        if confidence is not None:
            payload["confidence"] = confidence
        
        requests.post(
            f"{self.base_url}/telemetry/thoughts",
            json=payload
        )
    
    def log_action(self, action_name, action_type="TOOL_USE", input_data=None):
        """Log an agent action"""
        payload = {
            "agentId": self.agent_id,
            "sessionId": self.session_id,
            "actionType": action_type,
            "actionName": action_name
        }
        if input_data:
            payload["inputData"] = json.dumps(input_data)
        
        requests.post(
            f"{self.base_url}/telemetry/actions",
            json=payload
        )
    
    def log_metric(self, metric_name, value, unit="", metric_type="CUSTOM"):
        """Log a metric"""
        requests.post(
            f"{self.base_url}/telemetry/metrics",
            json={
                "agentId": self.agent_id,
                "sessionId": self.session_id,
                "metricName": metric_name,
                "metricValue": value,
                "metricUnit": unit,
                "metricType": metric_type
            }
        )
    
    def complete_session(self, final_response):
        """Complete the session"""
        requests.post(
            f"{self.base_url}/sessions/{self.session_id}/complete",
            json={"finalResponse": final_response}
        )
    
    def get_session_timeline(self):
        """Retrieve complete session timeline"""
        session = requests.get(f"{self.base_url}/sessions/{self.session_id}").json()
        thoughts = requests.get(f"{self.base_url}/sessions/{self.session_id}/thoughts").json()
        actions = requests.get(f"{self.base_url}/sessions/{self.session_id}/actions").json()
        
        return {
            "session": session,
            "thoughts": thoughts,
            "actions": actions
        }

# Usage example
watcher = AgentWatcher(agent_id="research-assistant")

# Start tracking
session_id = watcher.start_session("Summarize the latest AI research papers")

# Log agent behavior
watcher.log_thought("I need to search for recent papers first", "PLANNING", 0.95)
watcher.log_action("search_papers", "TOOL_USE", {"query": "AI research 2026", "limit": 10})
watcher.log_metric("search_latency", 1250.5, "ms", "LATENCY")

watcher.log_thought("Found 10 relevant papers, will summarize key findings", "REASONING", 0.88)
watcher.log_action("summarize_papers", "COMPUTATION", {"count": 10})
watcher.log_metric("total_tokens", 3500, "tokens", "TOKENS")

# Complete
watcher.complete_session("Summary of 10 recent AI papers generated successfully")

# Retrieve timeline
timeline = watcher.get_session_timeline()
print(f"Session completed with {len(timeline['thoughts'])} thoughts and {len(timeline['actions'])} actions")
```

### Pattern 2: JavaScript/TypeScript Integration

```typescript
// agent-watcher-client.ts
interface SessionResponse {
  sessionId: string;
  agentId: string;
  userQuery: string;
  sessionStatus: string;
  startTime: string;
}

class AgentWatcherClient {
  private baseUrl: string;
  private agentId: string;
  private sessionId: string | null = null;

  constructor(baseUrl: string = 'http://localhost:8080/api', agentId: string = 'my-agent') {
    this.baseUrl = baseUrl;
    this.agentId = agentId;
  }

  async startSession(userQuery: string): Promise<string> {
    const response = await fetch(`${this.baseUrl}/sessions/start`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ agentId: this.agentId, userQuery })
    });
    const data: SessionResponse = await response.json();
    this.sessionId = data.sessionId;
    return this.sessionId;
  }

  async logThought(
    content: string,
    thoughtType: string = 'REASONING',
    confidence?: number
  ): Promise<void> {
    const payload: any = {
      agentId: this.agentId,
      sessionId: this.sessionId,
      thoughtType,
      content
    };
    if (confidence !== undefined) {
      payload.confidence = confidence;
    }

    await fetch(`${this.baseUrl}/telemetry/thoughts`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
  }

  async logAction(
    actionName: string,
    actionType: string = 'TOOL_USE',
    inputData?: any
  ): Promise<void> {
    const payload: any = {
      agentId: this.agentId,
      sessionId: this.sessionId,
      actionType,
      actionName
    };
    if (inputData) {
      payload.inputData = JSON.stringify(inputData);
    }

    await fetch(`${this.baseUrl}/telemetry/actions`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
  }

  async logMetric(
    metricName: string,
    value: number,
    unit: string = '',
    metricType: string = 'CUSTOM'
  ): Promise<void> {
    await fetch(`${this.baseUrl}/telemetry/metrics`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        agentId: this.agentId,
        sessionId: this.sessionId,
        metricName,
        metricValue: value,
        metricUnit: unit,
        metricType
      })
    });
  }

  async completeSession(finalResponse: string): Promise<void> {
    await fetch(`${this.baseUrl}/sessions/${this.sessionId}/complete`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ finalResponse })
    });
  }

  async getSessionTimeline() {
    const [session, thoughts, actions] = await Promise.all([
      fetch(`${this.baseUrl}/sessions/${this.sessionId}`).then(r => r.json()),
      fetch(`${this.baseUrl}/sessions/${this.sessionId}/thoughts`).then(r => r.json()),
      fetch(`${this.baseUrl}/sessions/${this.sessionId}/actions`).then(r => r.json())
    ]);
    return { session, thoughts, actions };
  }
}

// Usage
const watcher = new AgentWatcherClient('http://localhost:8080/api', 'customer-support-bot');

async function handleCustomerRequest(query: string) {
  await watcher.startSession(query);
  
  await watcher.logThought('Analyzing customer intent', 'PLANNING', 0.92);
  await watcher.logAction('classify_intent', 'TOOL_USE', { query });
  await watcher.logMetric('classification_time', 150, 'ms', 'LATENCY');
  
  await watcher.logThought('Customer wants a refund for order #12345', 'REASONING', 0.95);
  await watcher.logAction('fetch_order', 'API_CALL', { orderId: '12345' });
  
  await watcher.completeSession('Refund processed successfully');
  
  const timeline = await watcher.getSessionTimeline();
  console.log(`Session completed with ${timeline.thoughts.length} thoughts`);
}
```

### Pattern 3: Decorator Pattern for Automatic Logging

```python
from functools import wraps
import time

def track_action(action_type="TOOL_USE"):
    """Decorator to automatically log agent actions"""
    def decorator(func):
        @wraps(func)
        def wrapper(self, *args, **kwargs):
            if hasattr(self, 'watcher'):
                start_time = time.time()
                
                # Log action start
                self.watcher.log_action(
                    action_name=func.__name__,
                    action_type=action_type,
                    input_data={"args": args, "kwargs": kwargs}
                )
                
                # Execute function
                result = func(self, *args, **kwargs)
                
                # Log latency
                duration = (time.time() - start_time) * 1000
                self.watcher.log_metric(
                    f"{func.__name__}_latency",
                    duration,
                    "ms",
                    "LATENCY"
                )
                
                return result
            return func(self, *args, **kwargs)
        return wrapper
    return decorator

class MyAgent:
    def __init__(self):
        self.watcher = AgentWatcher(agent_id="my-smart-agent")
    
    def process_query(self, query):
        self.watcher.start_session(query)
        
        self.watcher.log_thought("Starting query analysis", "PLANNING")
        result = self._analyze(query)
        
        self.watcher.log_thought(f"Analysis complete: {result}", "REASONING")
        self.watcher.complete_session(result)
        
        return result
    
    @track_action("TOOL_USE")
    def _analyze(self, query):
        # Your analysis logic here
        return f"Analyzed: {query}"
    
    @track_action("API_CALL")
    def _fetch_data(self, source):
        # Your data fetching logic
        return {"data": "sample"}
```

## Reference Guide

### Event Types

#### Thought Types
- **`PLANNING`**: Strategic planning and approach determination
  - Example: "I need to break this problem into three steps"
- **`REASONING`**: Logical reasoning and deduction
  - Example: "Based on the data, the correlation suggests..."
- **`TOOL_SELECTION`**: Choosing appropriate tools or functions
  - Example: "The search tool is best suited for this task"
- **`EXECUTION`**: Execution planning and strategy
  - Example: "I will execute these steps sequentially"
- **`REFLECTION`**: Self-reflection and learning from outcomes
  - Example: "The previous approach didn't work because..."
- **`ERROR`**: Error states or exception handling
  - Example: "API rate limit exceeded, will retry"

#### Action Types
- **`API_CALL`**: External API calls and HTTP requests
- **`TOOL_USE`**: Tool or function invocation
- **`COMPUTATION`**: Internal computations and processing
- **`DATA_RETRIEVAL`**: Data fetching and retrieval operations

#### Metric Types
- **`LATENCY`**: Time-based performance metrics (ms, seconds)
- **`TOKENS`**: Token usage and consumption
- **`COST`**: Financial cost tracking (usd, cents)
- **`ERROR_RATE`**: Error and retry tracking
- **`CUSTOM`**: Custom application-specific metrics

### Session Status Values
- `ACTIVE`, `COMPLETED`, `FAILED`, `TIMEOUT`

### Action Status Values
- `STARTED`, `IN_PROGRESS`, `COMPLETED`, `FAILED`

## Monitoring & Observability

### Health Checks

```bash
# Application health
curl http://localhost:8080/api/actuator/health

# All metrics
curl http://localhost:8080/api/actuator/metrics

# Specific metrics
curl http://localhost:8080/api/actuator/metrics/agent.session.started
curl http://localhost:8080/api/actuator/metrics/agent.thought.logged

# Prometheus format
curl http://localhost:8080/api/actuator/prometheus
```

### Azure Monitor Integration

1. Get Connection String from Azure Portal
2. Set environment variable:
```bash
export APPINSIGHTS_CONNECTION_STRING="InstrumentationKey=xxx;..."
```
3. Restart application - traces will flow to Azure automatically

### Log Files

```bash
# Default location
logs/agent-watcher.log

# Tail logs
tail -f logs/agent-watcher.log
```

## Advanced Usage

### Querying Historical Data

```sql
-- Average session duration by agent
SELECT 
    agent_id,
    AVG(EXTRACT(EPOCH FROM (end_time - start_time))) as avg_duration_seconds,
    COUNT(*) as total_sessions
FROM agent_sessions
WHERE session_status = 'COMPLETED'
GROUP BY agent_id;

-- Token usage by agent
SELECT 
    agent_id,
    SUM(metric_value) as total_tokens
FROM agent_telemetry
WHERE metric_type = 'TOKENS'
GROUP BY agent_id;

-- Failed actions analysis
SELECT 
    action_type,
    action_name,
    COUNT(*) as failure_count
FROM agent_actions
WHERE status = 'FAILED'
GROUP BY action_type, action_name
ORDER BY failure_count DESC;
```

## Development

### Database Schema
Tables managed by Liquibase:
- `agent_sessions` - Session tracking
- `agent_thoughts` - Agent reasoning
- `agent_actions` - Agent operations
- `agent_telemetry` - Performance metrics

### Logging Configuration

Edit `application.properties`:
```properties
logging.level.root=INFO
logging.level.com.openai.agentwatcher=DEBUG
logging.level.org.springframework.web=INFO
logging.level.org.hibernate.SQL=DEBUG
```

## Troubleshooting

### Common Issues

#### 1. Port Already in Use

**Problem:** `Port 8080 was already in use`

**Solutions:**
```bash
# Option 1: Use a different port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9090"

# Option 2: Kill the process using port 8080
lsof -ti:8080 | xargs kill -9

# Option 3: Configure in application.properties
server.port=9090
```

#### 2. Database Connection Errors (PostgreSQL)

**Problem:** `Connection refused` or `Authentication failed`

**Solutions:**
```bash
# Verify PostgreSQL is running
brew services list | grep postgresql

# Start PostgreSQL
brew services start postgresql

# Check connection
psql -h localhost -U postgres -d agent_watcher

# Verify credentials in application-postgres.properties match your setup
```

#### 3. H2 Console Not Accessible

**Problem:** Cannot access `/api/h2-console`

**Solution:**
```properties
# Ensure in application.properties:
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
server.servlet.context-path=/api
```
Then access: `http://localhost:8080/api/h2-console`

#### 4. Azure Monitor Not Receiving Data

**Problem:** No traces/metrics in Azure Application Insights

**Solutions:**
```bash
# Verify connection string is set
echo $APPINSIGHTS_CONNECTION_STRING

# Check application logs for errors
grep -i "azure" logs/agent-watcher.log

# Ensure enabled in properties
azure.application-insights.enabled=true

# Verify network connectivity
curl https://dc.services.visualstudio.com/v2/track
```

#### 5. Liquibase Migration Fails

**Problem:** `Liquibase Update Failed`

**Solutions:**
```bash
# Clear Liquibase locks
# Connect to database and run:
DELETE FROM databasechangeloglock;

# Force re-run migrations
mvn liquibase:clearCheckSums
mvn spring-boot:run

# Check migration files exist
ls -la src/main/resources/db/changelog/v1.0.0/
```

#### 6. High Memory Usage

**Problem:** JVM using too much memory

**Solutions:**
```bash
# Set max heap size
java -Xmx512m -jar target/agent-watcher-1.0.0-SNAPSHOT.jar

# Or via Maven
export MAVEN_OPTS="-Xmx512m"
mvn spring-boot:run

# Monitor memory usage
curl http://localhost:8080/api/actuator/metrics/jvm.memory.used
```

#### 7. Session Not Found

**Problem:** `404 Not Found` when querying session

**Possible Causes:**
- Session ID is incorrect (check the exact ID returned from `/sessions/start`)
- Using H2 and application restarted (H2 is in-memory, data is lost)
- Session was created in different database/profile

**Solutions:**
```bash
# Verify session exists in database
# For H2: Check via H2 console
# For PostgreSQL:
psql -d agent_watcher -c "SELECT session_id FROM agent_sessions ORDER BY start_time DESC LIMIT 5;"

# Use the exact session ID returned from start endpoint
```

#### 8. CORS Errors from Browser

**Problem:** `CORS policy: No 'Access-Control-Allow-Origin' header`

**Solution:**
Edit `application.properties`:
```properties
cors.allowed-origins=http://localhost:3000,http://yourdomain.com
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
cors.allow-credentials=true
```

### Performance Tips

1. **Use PostgreSQL for Production**
   - H2 is great for development but PostgreSQL performs better under load
   - Connection pooling is configured with HikariCP

2. **Index Usage**
   - Indexes are created automatically on session_id, agent_id, timestamps
   - Query by these fields for fast lookups

3. **Batch Events**
   - For high-throughput scenarios, batch events client-side before sending
   - Reduces HTTP overhead

4. **Monitor Metrics**
   - Watch `/actuator/metrics` for JVM health
   - Set up alerts on high latency or error rates

5. **Log Rotation**
   - Configure log rotation to prevent disk space issues:
```properties
logging.file.max-size=10MB
logging.file.max-history=10
```

## Future Enhancements

- WebSocket support for real-time updates
- Advanced analytics and visualization dashboard
- Support for multiple LLM providers
- Enhanced search and filtering capabilities
- Data retention policies and archival
- Session replay and debugging tools
- Cost estimation and budget alerts

## Documentation

- [Liquibase Database Migrations](README-LIQUIBASE.md)
- [Azure Application Insights & OpenTelemetry Setup](README-AZURE-OPENTELEMETRY.md)

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

## License

Apache 2.0 License

---

**Questions or Issues?**
- GitHub Issues: https://github.com/sidmocodes/ai_agent_watcher/issues
- Documentation: See README files in the repository
