package com.pokevault.card;

public class CardNotFoundException extends RuntimeException {

	public CardNotFoundException(long id) {
		super("Card " + id + " was not found");
	}
}
