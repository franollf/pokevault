package com.pokevault.card;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CardService {

	private final CardRepository cardRepository;

	CardService(CardRepository cardRepository) {
		this.cardRepository = cardRepository;
	}

	@Transactional
	public CardResponse create(CreateCardRequest request) {
		if (cardRepository.existsByExternalId(request.externalId())) {
			throw new DuplicateCardException(request.externalId());
		}

		Card card = new Card(
				request.externalId(),
				request.name(),
				request.setName(),
				request.setCode(),
				request.cardNumber(),
				request.rarity(),
				request.imageUrl());
		try {
			return toResponse(cardRepository.save(card));
		} catch (DataIntegrityViolationException exception) {
			if (causedByConstraint(exception, "uk_cards_external_id")) {
				throw new DuplicateCardException(request.externalId());
			}
			throw exception;
		}
	}

	@Transactional(readOnly = true)
	public CardResponse getById(long id) {
		return cardRepository.findById(id)
				.map(CardService::toResponse)
				.orElseThrow(() -> new CardNotFoundException(id));
	}

	private static CardResponse toResponse(Card card) {
		return new CardResponse(
				card.getId(),
				card.getExternalId(),
				card.getName(),
				card.getSetName(),
				card.getSetCode(),
				card.getCardNumber(),
				card.getRarity(),
				card.getImageUrl(),
				card.getCreatedAt(),
				card.getUpdatedAt());
	}

	private static boolean causedByConstraint(Throwable exception, String constraintName) {
		for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
			if (cause.getMessage() != null && cause.getMessage().contains(constraintName)) {
				return true;
			}
		}
		return false;
	}
}
