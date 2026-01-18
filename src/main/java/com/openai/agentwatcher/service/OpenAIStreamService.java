package com.openai.agentwatcher.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * Service for connecting to OpenAI API and streaming agent events
 */
@Service
@Slf4j
public class OpenAIStreamService {

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.api.url:https://api.openai.com/v1}")
    private String apiUrl;

    private final WebClient webClient;

    @Autowired
    private AgentEventParserService eventParser;

    public OpenAIStreamService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Stream agent events from OpenAI API
     * This is a placeholder implementation - actual implementation depends on OpenAI's agent API
     */
    public Flux<Map<String, Object>> streamAgentEvents(String agentId, String sessionId) {
        log.info("Starting event stream for agent: {}, session: {}", agentId, sessionId);
        
        // This is a placeholder - replace with actual OpenAI streaming endpoint
        return webClient.get()
                .uri(apiUrl + "/agents/{agentId}/events", agentId)
                .header("Authorization", "Bearer " + apiKey)
                .header("OpenAI-Beta", "assistants=v2")
                .retrieve()
                .bodyToFlux(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnNext(event -> {
                    OpenAIStreamService.log.debug("Received event: {}", event);
                    processEvent(agentId, sessionId, event);
                })
                .doOnError(error -> log.error("Error streaming events", error))
                .doOnComplete(() -> log.info("Event stream completed for session: {}", sessionId));
    }

    private void processEvent(String agentId, String sessionId, Map<String, Object> event) {
        try {
            eventParser.processAgentEvent(agentId, sessionId, event);
        } catch (Exception e) {
            log.error("Error processing event", e);
        }
    }

    /**
     * Subscribe to agent telemetry stream
     */
    public void subscribeToAgent(String agentId, String sessionId) {
        streamAgentEvents(agentId, sessionId)
                .subscribe(
                    event -> log.debug("Event processed: {}", event.get("type")),
                    error -> log.error("Subscription error", error),
                    () -> log.info("Subscription completed")
                );
    }
}
