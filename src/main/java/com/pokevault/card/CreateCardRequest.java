package com.pokevault.card;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCardRequest(
		@NotBlank @Size(max = 100) String externalId,
		@NotBlank @Size(max = 200) String name,
		@NotBlank @Size(max = 200) String setName,
		@NotBlank @Size(max = 50) String setCode,
		@NotBlank @Size(max = 50) String cardNumber,
		@Size(max = 100) String rarity,
		@Size(max = 2048) @URL String imageUrl) {
}
