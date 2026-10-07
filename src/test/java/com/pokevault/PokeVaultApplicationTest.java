package com.pokevault;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
class PokeVaultApplicationTest {

	@Container
	@ServiceConnection
	static final PostgreSQLContainer POSTGRES =
			new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

	@Autowired
	private JdbcClient jdbcClient;

	@Test
	void contextLoadsWithCardsTable() {
		Integer cardCount = jdbcClient.sql("select count(*) from cards").query(Integer.class).single();

		assertThat(cardCount).isZero();
	}
}
