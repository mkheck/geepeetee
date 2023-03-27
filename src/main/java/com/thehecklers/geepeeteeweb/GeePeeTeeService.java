package com.thehecklers.geepeeteeweb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.thehecklers.geepeeteeweb.data.CompletionRequest;
import com.thehecklers.geepeeteeweb.data.EventData;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
public class GeePeeTeeService {
    private final Logger logger = LoggerFactory.getLogger(GeePeeTeeService.class);

    private final String prompt = """
            Which actor was the best Spock in the various Star Trek shows and movies?
            """;

    @Value("${application.openai.url}")
    private String openAiUrl;

    @Value("${application.openai.deployment}")
    private String openAiDeployment;

    @Value("${application.openai.key}")
    private String openAiKey;

    private WebClient client;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    @PostConstruct
    public void init() {
        client = WebClient.builder()
                .baseUrl(openAiUrl + "/openai/deployments/" + openAiDeployment + "/completions?api-version=2022-12-01")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("api-key", openAiKey)
                .build();
    }

    public Flux<String> getData(String queryPrompt) throws JsonProcessingException {
        CompletionRequest request = new CompletionRequest();
        request.setPrompt(null == queryPrompt ? prompt : queryPrompt);
        request.setMaxTokens(2048);
        request.setTemperature(1.0);
        request.setFrequencyPenalty(0.0);
        request.setPresencePenalty(0.0);
        request.setTopP(0.5);
        request.setBestOf(1);
        request.setStream(true);
        request.setStop(null);

        String requestValue = objectMapper.writeValueAsString(request);

        logger.info("Request: " + requestValue);

        return client.post()
                .bodyValue(requestValue)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .mapNotNull(event -> {
                    try {
                        String jsonData = event.substring(event.indexOf("{"), event.lastIndexOf("}") + 1);
                        return objectMapper.readValue(jsonData, EventData.class);
                    } catch (JsonProcessingException | StringIndexOutOfBoundsException e) {
                        return null;
                    }
                })
                .skipUntil(event -> !event.getChoices().get(0).getText().equals("\n"))
                .map(event -> event.getChoices().get(0).getText());
    }

}
