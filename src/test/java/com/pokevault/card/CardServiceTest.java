package com.pokevault.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

	private static final Instant CREATED_AT = Instant.parse("2026-10-07T18:00:00Z");
	private static final Instant UPDATED_AT = Instant.parse("2026-10-07T18:30:00Z");

	@Mock
	private CardRepository cardRepository;

	private CardService cardService;

	@BeforeEach
	void setUp() {
		cardService = new CardService(cardRepository);
	}

	@Test
	void createReturnsThePersistedCard() {
		CreateCardRequest request = umbreonRequest();
		when(cardRepository.existsByExternalId("sv8-161")).thenReturn(false);
		when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
			Card savedCard = invocation.getArgument(0);
			ReflectionTestUtils.setField(savedCard, "id", 42L);
			ReflectionTestUtils.setField(savedCard, "createdAt", CREATED_AT);
			ReflectionTestUtils.setField(savedCard, "updatedAt", UPDATED_AT);
			return savedCard;
		});

		CardResponse response = cardService.create(request);

		assertThat(response).isEqualTo(new CardResponse(
				42L,
				"sv8-161",
				"Umbreon ex",
				"Prismatic Evolutions",
				"PRE",
				"161",
				"Special Illustration Rare",
				"https://example.com/umbreon.png",
				CREATED_AT,
				UPDATED_AT));
	}

	@Test
	void createRejectsDuplicateExternalIdWithoutSaving() {
		when(cardRepository.existsByExternalId("sv8-161")).thenReturn(true);

		assertThatThrownBy(() -> cardService.create(umbreonRequest()))
				.isInstanceOf(DuplicateCardException.class)
				.hasMessage("A card with external ID 'sv8-161' already exists");
		verify(cardRepository, never()).save(any());
	}

	@Test
	void createTranslatesExternalIdConstraintRaceToDuplicateCard() {
		when(cardRepository.existsByExternalId("sv8-161")).thenReturn(false);
		when(cardRepository.save(any(Card.class))).thenThrow(new DataIntegrityViolationException(
				"could not execute statement",
				new RuntimeException("duplicate key violates constraint uk_cards_external_id")));

		assertThatThrownBy(() -> cardService.create(umbreonRequest()))
				.isInstanceOf(DuplicateCardException.class)
				.hasMessage("A card with external ID 'sv8-161' already exists");
	}

	@Test
	void createPreservesUnrelatedIntegrityFailures() {
		DataIntegrityViolationException failure = new DataIntegrityViolationException(
				"null value violates constraint cards_name_not_null");
		when(cardRepository.existsByExternalId("sv8-161")).thenReturn(false);
		when(cardRepository.save(any(Card.class))).thenThrow(failure);

		assertThatThrownBy(() -> cardService.create(umbreonRequest())).isSameAs(failure);
	}

	@Test
	void getByIdReturnsTheExistingCard() {
		Card card = persistedUmbreon();
		when(cardRepository.findById(42L)).thenReturn(Optional.of(card));

		CardResponse response = cardService.getById(42L);

		assertThat(response.id()).isEqualTo(42L);
		assertThat(response.externalId()).isEqualTo("sv8-161");
		assertThat(response.name()).isEqualTo("Umbreon ex");
		assertThat(response.createdAt()).isEqualTo(CREATED_AT);
	}

	@Test
	void getByIdRejectsUnknownCard() {
		when(cardRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> cardService.getById(999L))
				.isInstanceOf(CardNotFoundException.class)
				.hasMessage("Card 999 was not found");
	}

	private static CreateCardRequest umbreonRequest() {
		return new CreateCardRequest(
				"sv8-161",
				"Umbreon ex",
				"Prismatic Evolutions",
				"PRE",
				"161",
				"Special Illustration Rare",
				"https://example.com/umbreon.png");
	}

	private static Card persistedUmbreon() {
		Card card = new Card(
				"sv8-161",
				"Umbreon ex",
				"Prismatic Evolutions",
				"PRE",
				"161",
				"Special Illustration Rare",
				"https://example.com/umbreon.png");
		ReflectionTestUtils.setField(card, "id", 42L);
		ReflectionTestUtils.setField(card, "createdAt", CREATED_AT);
		ReflectionTestUtils.setField(card, "updatedAt", UPDATED_AT);
		return card;
	}
}
