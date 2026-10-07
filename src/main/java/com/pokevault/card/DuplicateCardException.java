package com.pokevault.card;

public class DuplicateCardException extends RuntimeException {

	public DuplicateCardException(String externalId) {
		super("A card with external ID '" + externalId + "' already exists");
	}
}
