package com.pokevault.card;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "cards")
class Card {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "external_id", nullable = false, unique = true, length = 100)
	private String externalId;

	@Column(nullable = false, length = 200)
	private String name;

	@Column(name = "set_name", nullable = false, length = 200)
	private String setName;

	@Column(name = "set_code", nullable = false, length = 50)
	private String setCode;

	@Column(name = "card_number", nullable = false, length = 50)
	private String cardNumber;

	@Column(length = 100)
	private String rarity;

	@Column(name = "image_url", length = 2048)
	private String imageUrl;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Card() {
	}

	Card(String externalId, String name, String setName, String setCode, String cardNumber,
			String rarity, String imageUrl) {
		this.externalId = externalId;
		this.name = name;
		this.setName = setName;
		this.setCode = setCode;
		this.cardNumber = cardNumber;
		this.rarity = rarity;
		this.imageUrl = imageUrl;
	}

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}

	Long getId() {
		return id;
	}

	String getExternalId() {
		return externalId;
	}

	String getName() {
		return name;
	}

	String getSetName() {
		return setName;
	}

	String getSetCode() {
		return setCode;
	}

	String getCardNumber() {
		return cardNumber;
	}

	String getRarity() {
		return rarity;
	}

	String getImageUrl() {
		return imageUrl;
	}

	Instant getCreatedAt() {
		return createdAt;
	}

	Instant getUpdatedAt() {
		return updatedAt;
	}
}
