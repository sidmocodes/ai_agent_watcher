# Liquibase Database Migration Guide

## Overview

This project uses Liquibase for database version control and schema management. All database changes are tracked through versioned changelog files.

## Directory Structure

```
src/main/resources/db/changelog/
├── db.changelog-master.xml          # Master changelog file
└── v1.0.0/                          # Version 1.0.0 changesets
    ├── 01-create-agent-sessions-table.xml
    ├── 02-create-agent-thoughts-table.xml
    ├── 03-create-agent-actions-table.xml
    ├── 04-create-agent-telemetry-table.xml
    └── 05-create-indexes.xml
```

## Tables Created

### 1. agent_sessions
Tracks agent execution sessions with metrics like total thoughts, actions, and session status.

**Columns:**
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `session_id` (VARCHAR(255), UNIQUE, NOT NULL)
- `agent_id` (VARCHAR(255), NOT NULL)
- `user_query` (TEXT)
- `session_status` (VARCHAR(50))
- `start_time` (TIMESTAMP)
- `end_time` (TIMESTAMP)
- `total_thoughts` (INTEGER)
- `total_actions` (INTEGER)
- `final_response` (TEXT)

### 2. agent_thoughts
Captures agent reasoning processes including thought type, content, and confidence scores.

**Columns:**
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `agent_id` (VARCHAR(255), NOT NULL)
- `session_id` (VARCHAR(255), NOT NULL)
- `thought_type` (VARCHAR(100))
- `thought_content` (TEXT)
- `confidence_score` (DOUBLE)
- `timestamp` (TIMESTAMP)
- `parent_thought_id` (BIGINT, FK to agent_thoughts)
- `metadata` (TEXT)

### 3. agent_actions
Tracks agent operations and tool usage with input/output data and execution metrics.

**Columns:**
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `agent_id` (VARCHAR(255), NOT NULL)
- `session_id` (VARCHAR(255), NOT NULL)
- `action_type` (VARCHAR(100))
- `action_name` (VARCHAR(255))
- `input_data` (TEXT)
- `output_data` (TEXT)
- `status` (VARCHAR(50))
- `start_time` (TIMESTAMP)
- `end_time` (TIMESTAMP)
- `duration_ms` (BIGINT)
- `error_message` (TEXT)
- `related_thought_id` (BIGINT, FK to agent_thoughts)

### 4. agent_telemetry
Stores performance metrics and monitoring data for agents.

**Columns:**
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `agent_id` (VARCHAR(255), NOT NULL)
- `session_id` (VARCHAR(255), NOT NULL)
- `metric_name` (VARCHAR(255))
- `metric_value` (DOUBLE)
- `metric_unit` (VARCHAR(50))
- `metric_type` (VARCHAR(100))
- `timestamp` (TIMESTAMP)
- `tags` (TEXT)

## Indexes

Performance-optimized indexes are created on:
- Session IDs, Agent IDs
- Timestamps for time-based queries
- Status and type columns for filtering
- Composite indexes for common query patterns

## Running Migrations

### Development (H2 Database)

```bash
# Default profile uses H2 in-memory database
mvn spring-boot:run
```

### Production (PostgreSQL)

```bash
# Using PostgreSQL profile
mvn spring-boot:run -Dspring-boot.run.profiles=postgres

# Or set environment variable
export SPRING_PROFILES_ACTIVE=postgres
mvn spring-boot:run
```

### Manual Liquibase Commands

```bash
# Update database to latest version
mvn liquibase:update

# Rollback last changeset
mvn liquibase:rollback -Dliquibase.rollbackCount=1

# Generate SQL without applying changes
mvn liquibase:updateSQL

# Validate changelog files
mvn liquibase:validate

# Generate database documentation
mvn liquibase:dbDoc

# Check pending changesets
mvn liquibase:status

# Clear checksums (use with caution)
mvn liquibase:clearCheckSums
```

## Configuration

### application.properties (Default - H2)
```properties
spring.jpa.hibernate.ddl-auto=none
spring.liquibase.enabled=true
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml
```

### application-postgres.properties (PostgreSQL)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/agent_watcher
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.liquibase.enabled=true
```

## Creating New Migrations

### Step 1: Create a New Version Directory
```bash
mkdir -p src/main/resources/db/changelog/v1.1.0
```

### Step 2: Create Changelog File
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">

    <changeSet id="v1.1.0-add-new-column" author="your-name">
        <comment>Add new column to agent_sessions</comment>
        
        <addColumn tableName="agent_sessions">
            <column name="new_column" type="VARCHAR(255)">
                <constraints nullable="true"/>
            </column>
        </addColumn>
        
        <rollback>
            <dropColumn tableName="agent_sessions" columnName="new_column"/>
        </rollback>
    </changeSet>

</databaseChangeLog>
```

### Step 3: Update Master Changelog
Add include statement to `db.changelog-master.xml`:
```xml
<include file="db/changelog/v1.1.0/01-add-new-column.xml"/>
```

## Best Practices

1. **Never modify existing changesets** - Create new ones instead
2. **Always provide rollback instructions** for production deployments
3. **Use meaningful IDs and comments** for changesets
4. **Test migrations** on a copy of production data before deploying
5. **Version your changes** using semantic versioning (v1.0.0, v1.1.0, etc.)
6. **Keep changesets small** and focused on single logical changes
7. **Use preconditions** when necessary to ensure safe migrations
8. **Document breaking changes** in commit messages

## Troubleshooting

### Checksum Validation Failed
```bash
# Clear checksums (development only)
mvn liquibase:clearCheckSums

# Or update checksums in database
UPDATE DATABASECHANGELOG SET MD5SUM = NULL;
```

### Lock Timeout
```bash
# Release stuck locks (use with caution)
mvn liquibase:releaseLocks
```

### Migration Failed
```bash
# Check status
mvn liquibase:status

# Rollback to previous version
mvn liquibase:rollback -Dliquibase.rollbackCount=1

# Force release and retry
mvn liquibase:releaseLocks
mvn liquibase:update
```

## Database Compatibility

Liquibase changesets in this project are compatible with:
- H2 Database (development)
- PostgreSQL 12+ (production)
- MySQL 8.0+ (with dialect changes)
- SQL Server (with dialect changes)

## References

- [Liquibase Documentation](https://docs.liquibase.com/)
- [Spring Boot Liquibase Integration](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.liquibase)
- [Database Changelog XML Format](https://docs.liquibase.com/concepts/changelogs/xml-format.html)
