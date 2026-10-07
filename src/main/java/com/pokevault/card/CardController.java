package com.pokevault.card;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/cards")
public class CardController {

	private final CardService cardService;

	CardController(CardService cardService) {
		this.cardService = cardService;
	}

	@PostMapping
	ResponseEntity<CardResponse> create(@Valid @RequestBody CreateCardRequest request) {
		CardResponse response = cardService.create(request);
		return ResponseEntity.created(URI.create("/api/cards/" + response.id())).body(response);
	}

	@GetMapping("/{id}")
	CardResponse getById(@PathVariable @Positive long id) {
		return cardService.getById(id);
	}
}
