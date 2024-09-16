package com.gemini.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gemini.serviceImpl.GeminiService;
import com.gemini.dto.PromptRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GeminiController {

	@Autowired
	private GeminiService geminiService;

	@PostMapping("/prompt")
	public String getResponse(@RequestBody PromptRequest promptRequest) {
		String payload = promptRequest.getPayload();

		ObjectMapper objectMapper = new ObjectMapper();
		try {
			// Parse the payload into a JsonNode
			JsonNode rootNode = objectMapper.readTree(payload);
			JsonNode inputJson = rootNode.get("input");
			JsonNode targetJson = rootNode.get("target");

			if (inputJson == null || targetJson == null) {
				throw new IllegalArgumentException("Payload must contain 'input' and 'target' fields.");
			}

			return geminiService.callApi(inputJson, targetJson);
		} catch (Exception e) {
			throw new RuntimeException("Failed to process payload", e);
		}
	}
}
