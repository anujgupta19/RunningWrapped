package com.runningwrapped.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

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

	@Test
	void findsUserByEmailIgnoringCase() {
		var saved = users.saveAndFlush(new AppUser("Runner@Example.com", "{noop}hash"));
		entityManager.clear();

		var found = users.findByEmail("runner@EXAMPLE.com");

		assertThat(found).hasValueSatisfying(user -> assertThat(user.getId()).isEqualTo(saved.getId()));
	}

	@Test
	void returnsEmptyForUnknownEmail() {
		users.saveAndFlush(new AppUser("runner@example.com", "{noop}hash"));

		assertThat(users.findByEmail("nobody@example.com")).isEmpty();
	}

	@Test
	void rejectsDuplicateEmailDifferingOnlyInCase() {
		users.saveAndFlush(new AppUser("runner@example.com", "{noop}hash"));

		// saveAndFlush, not save: save() only queues the INSERT in the persistence context, so the
		// unique-index violation wouldn't surface until commit, which never happens in a rolled-back test.
		assertThatThrownBy(() -> users.saveAndFlush(new AppUser("RUNNER@example.com", "{noop}hash")))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

}
