package com.gemini.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class GeminiService {

        @Autowired
        private RestTemplate restTemplate;

        @Value("${gemini.api.key}")
        private String geminiKey;

        private final String API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=%s";

        public String callApi(JsonNode inputJson, JsonNode targetJson) {
                String prompt = createPrompt(inputJson, targetJson);
                String apiUrl = String.format(API_URL_TEMPLATE, geminiKey);

                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");

                ObjectMapper objectMapper = new ObjectMapper();
                ObjectNode contentNode = objectMapper.createObjectNode();
                ObjectNode partsNode = objectMapper.createObjectNode();
                partsNode.put("text", prompt);
                contentNode.set("parts", objectMapper.createArrayNode().add(partsNode));
                ObjectNode requestBodyNode = objectMapper.createObjectNode();
                requestBodyNode.set("contents", objectMapper.createArrayNode().add(contentNode));

                String requestBody;
                try {
                        requestBody = objectMapper.writeValueAsString(requestBodyNode);
                } catch (Exception e) {
                        throw new RuntimeException("Failed to construct JSON request body", e);
                }

                HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

                try {
                        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);
                        return extractTextFromResponse(response.getBody());
                } catch (Exception e) {
                        throw new RuntimeException("API call failed", e);
                }
        }

        private String createPrompt(JsonNode inputJson, JsonNode targetJson) {
                return String.format(
                        "Given the input JSON %s and the target JSON %s, suggest the most appropriate field mappings. Don't give any explanation, just give me the answer.",
                        inputJson.toString(),
                        targetJson.toString()
                );
        }

        private String extractTextFromResponse(String responseBody) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                        JsonNode responseNode = objectMapper.readTree(responseBody);
                        JsonNode candidatesNode = responseNode.get("candidates");
                        if (candidatesNode != null && candidatesNode.isArray() && candidatesNode.size() > 0) {
                                JsonNode contentNode = candidatesNode.get(0).get("content");
                                if (contentNode != null) {
                                        JsonNode partsNode = contentNode.get("parts");
                                        if (partsNode != null && partsNode.isArray() && partsNode.size() > 0) {
                                                JsonNode textNode = partsNode.get(0).get("text");
                                                if (textNode != null) {
                                                        String text = textNode.asText();
                                                        // Remove the surrounding triple backticks and `json` keyword
                                                        text = text.replace("```json\n", "").replace("\n```", "");
                                                        return text;
                                                }
                                        }
                                }
                        }
                        throw new RuntimeException("Failed to extract the text content from API response");
                } catch (Exception e) {
                        throw new RuntimeException("Failed to parse API response", e);
                }
        }

}
