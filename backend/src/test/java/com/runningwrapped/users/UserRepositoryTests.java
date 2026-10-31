package com.runningwrapped.users;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.runningwrapped.TestcontainersConfiguration;

import jakarta.persistence.EntityManager;

/**
 * Repository "slice" test: only JPA, Flyway and the DataSource start (no web layer, no services). Each test runs in a
 * transaction that is rolled back afterwards.
 */
@DataJpaTest
@Import(TestcontainersConfiguration.class)
class UserRepositoryTests {

	@Autowired
	UserRepository users;

	@Autowired
	EntityManager entityManager;

	@Test
	void savesNewUserWithDefaults() {
		var saved = users.saveAndFlush(new AppUser("runner@example.com", "{noop}hash"));
		// Detach everything so the next read really hits the database instead of Hibernate's first-level cache.
		entityManager.clear();

		var loaded = users.findById(saved.getId()).orElseThrow();

		assertThat(loaded.getId()).isNotNull();
		assertThat(loaded.getEmail()).isEqualTo("runner@example.com");
		assertThat(loaded.getPreferredUnits()).isEqualTo(Units.MILES);
		assertThat(loaded.getTimezone()).isEqualTo(ZoneId.of("UTC"));
		assertThat(loaded.getWeekStart()).isEqualTo(DayOfWeek.MONDAY);
		assertThat(loaded.getAiConsentAt()).isNull();
		assertThat(loaded.getCreatedAt()).isNotNull();
	}

	// 🧑‍💻 Your turn: tests for UserRepository.findByEmail. See docs/ROADMAP.md step 1.4.

}
