package com.pokevault.card;

import org.springframework.data.jpa.repository.JpaRepository;

interface CardRepository extends JpaRepository<Card, Long> {

	boolean existsByExternalId(String externalId);
}
