package com.pokevault.card;

import java.time.Instant;

public record CardResponse(
		Long id,
		String externalId,
		String name,
		String setName,
		String setCode,
		String cardNumber,
		String rarity,
		String imageUrl,
		Instant createdAt,
		Instant updatedAt) {
}
