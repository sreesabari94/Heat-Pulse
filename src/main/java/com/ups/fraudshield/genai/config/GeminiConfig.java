package com.ups.fraudshield.genai.config;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.api.GenerationConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GeminiConfig {

    @Value("${fraudshield.gemini.project-id}")
    private String projectId;

    @Value("${fraudshield.gemini.location}")
    private String location;

    @Value("${fraudshield.gemini.model-name}")
    private String modelName;

    @Value("${fraudshield.gemini.max-output-tokens}")
    private int maxOutputTokens;

    @Value("${fraudshield.gemini.temperature}")
    private float temperature;

    @Bean
    public VertexAI vertexAI() throws IOException {
        return new VertexAI(projectId, location);
    }

    @Bean
    public GenerativeModel generativeModel(VertexAI vertexAI) {
        GenerationConfig generationConfig = GenerationConfig.newBuilder()
                .setMaxOutputTokens(maxOutputTokens)
                .setTemperature(temperature)
                .build();

        return new GenerativeModel.Builder()
                .setModelName(modelName)
                .setVertexAi(vertexAI)
                .setGenerationConfig(generationConfig)
                .build();
    }
}
