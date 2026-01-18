package com.openai.agentwatcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for OpenAI Agent Watcher Service
 * This service monitors and logs OpenAI agent telemetry and observability data
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class AgentWatcherApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentWatcherApplication.class, args);
    }
}
