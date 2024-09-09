package com.gemini.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gemini.serviceImpl.GeminiService;
import com.gemini.dto.PromptRequest;

@RestController
public class GeminiController {

	@Autowired
	GeminiService geminiService;

	@PostMapping("/prompt")
	public String getResponse(@RequestBody PromptRequest promptRequest) {
		return geminiService.callApi(promptRequest.getPrompt());
	}
}
