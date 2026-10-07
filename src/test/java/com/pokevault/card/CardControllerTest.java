package com.pokevault.card;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CardController.class)
class CardControllerTest {

	private static final Instant CREATED_AT = Instant.parse("2026-10-07T18:00:00Z");
	private static final Instant UPDATED_AT = Instant.parse("2026-10-07T18:30:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CardService cardService;

	@Test
	void createCardReturnsCreatedCardAndLocation() throws Exception {
		when(cardService.create(any(CreateCardRequest.class))).thenReturn(umbreonResponse());

		mockMvc.perform(post("/api/cards")
				.contentType(MediaType.APPLICATION_JSON)
				.content(validRequestJson()))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/cards/42"))
				.andExpect(jsonPath("$.id").value(42))
				.andExpect(jsonPath("$.externalId").value("sv8-161"))
				.andExpect(jsonPath("$.name").value("Umbreon ex"))
				.andExpect(jsonPath("$.setName").value("Prismatic Evolutions"));
	}

	@Test
	void getCardReturnsExistingCard() throws Exception {
		when(cardService.getById(42L)).thenReturn(umbreonResponse());

		mockMvc.perform(get("/api/cards/42"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(42))
				.andExpect(jsonPath("$.cardNumber").value("161"))
				.andExpect(jsonPath("$.createdAt").value("2026-10-07T18:00:00Z"));
	}

	@Test
	void createCardRejectsBlankRequiredFields() throws Exception {
		String json = """
				{
				  "externalId": " ",
				  "name": "",
				  "setName": "Prismatic Evolutions",
				  "setCode": "PRE",
				  "cardNumber": "161"
				}
				""";

		mockMvc.perform(post("/api/cards").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.fieldErrors.externalId").exists())
				.andExpect(jsonPath("$.fieldErrors.name").exists())
				.andExpect(jsonPath("$.path").value("/api/cards"));
	}

	@Test
	void createCardRejectsMalformedImageUrl() throws Exception {
		String json = validRequestJson().replace("https://example.com/umbreon.png", "not-a-url");

		mockMvc.perform(post("/api/cards").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.fieldErrors.imageUrl").exists());
	}

	@Test
	void createCardRejectsMalformedJsonWithStructuredError() throws Exception {
		mockMvc.perform(post("/api/cards")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"externalId\": \"sv8-161\""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
				.andExpect(jsonPath("$.message").value("Request body is missing or malformed"))
				.andExpect(jsonPath("$.path").value("/api/cards"));
	}

	@Test
	void createCardRejectsMissingBodyWithStructuredError() throws Exception {
		mockMvc.perform(post("/api/cards").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
				.andExpect(jsonPath("$.message").value("Request body is missing or malformed"))
				.andExpect(jsonPath("$.fieldErrors").isEmpty());
	}

	@Test
	void createCardReturnsConflictForDuplicateExternalId() throws Exception {
		when(cardService.create(any(CreateCardRequest.class))).thenThrow(new DuplicateCardException("sv8-161"));

		mockMvc.perform(post("/api/cards")
				.contentType(MediaType.APPLICATION_JSON)
				.content(validRequestJson()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("DUPLICATE_CARD"))
				.andExpect(jsonPath("$.message").value("A card with external ID 'sv8-161' already exists"));
	}

	@Test
	void getCardReturnsNotFoundForUnknownId() throws Exception {
		when(cardService.getById(999L)).thenThrow(new CardNotFoundException(999L));

		mockMvc.perform(get("/api/cards/999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("CARD_NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("Card 999 was not found"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"0", "-1", "not-a-number"})
	void getCardRejectsInvalidId(String id) throws Exception {
		mockMvc.perform(get("/api/cards/{id}", id))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
				.andExpect(jsonPath("$.path").value("/api/cards/" + id));
	}

	private static CardResponse umbreonResponse() {
		return new CardResponse(
				42L,
				"sv8-161",
				"Umbreon ex",
				"Prismatic Evolutions",
				"PRE",
				"161",
				"Special Illustration Rare",
				"https://example.com/umbreon.png",
				CREATED_AT,
				UPDATED_AT);
	}

	private static String validRequestJson() {
		return """
				{
				  "externalId": "sv8-161",
				  "name": "Umbreon ex",
				  "setName": "Prismatic Evolutions",
				  "setCode": "PRE",
				  "cardNumber": "161",
				  "rarity": "Special Illustration Rare",
				  "imageUrl": "https://example.com/umbreon.png"
				}
				""";
	}
}
