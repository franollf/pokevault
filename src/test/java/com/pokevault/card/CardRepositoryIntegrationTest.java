package com.pokevault.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import jakarta.persistence.EntityManager;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CardRepositoryIntegrationTest {

	@Container
	@ServiceConnection
	static final PostgreSQLContainer POSTGRES =
			new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

	@Autowired
	private CardRepository cardRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void savesAndReloadsEveryCardField() {
		Card saved = cardRepository.saveAndFlush(umbreon("sv8-161"));
		Long id = saved.getId();
		entityManager.clear();

		Card reloaded = cardRepository.findById(id).orElseThrow();

		assertThat(reloaded.getId()).isNotNull();
		assertThat(reloaded.getExternalId()).isEqualTo("sv8-161");
		assertThat(reloaded.getName()).isEqualTo("Umbreon ex");
		assertThat(reloaded.getSetName()).isEqualTo("Prismatic Evolutions");
		assertThat(reloaded.getSetCode()).isEqualTo("PRE");
		assertThat(reloaded.getCardNumber()).isEqualTo("161");
		assertThat(reloaded.getRarity()).isEqualTo("Special Illustration Rare");
		assertThat(reloaded.getImageUrl()).isEqualTo("https://example.com/umbreon.png");
		assertThat(reloaded.getCreatedAt()).isNotNull();
		assertThat(reloaded.getUpdatedAt()).isNotNull();
	}

	@Test
	void enforcesUniqueExternalId() {
		cardRepository.saveAndFlush(umbreon("sv8-161"));

		assertThatThrownBy(() -> cardRepository.saveAndFlush(umbreon("sv8-161")))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	private static Card umbreon(String externalId) {
		return new Card(
				externalId,
				"Umbreon ex",
				"Prismatic Evolutions",
				"PRE",
				"161",
				"Special Illustration Rare",
				"https://example.com/umbreon.png");
	}
}
