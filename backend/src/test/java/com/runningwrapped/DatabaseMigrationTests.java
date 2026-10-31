package com.runningwrapped;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * Checks that Flyway applied the migrations and that the schema enforces its constraints.
 * {@code @Transactional} on a test rolls back after each test method, so rows never leak between tests.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class DatabaseMigrationTests {

	@Autowired
	JdbcTemplate jdbc;

	@Test
	void appliesMigrationsInOrder() {
		var versions = jdbc.queryForList(
				"select version from flyway_schema_history where success order by installed_rank", String.class);

		assertThat(versions).startsWith("1");
	}

	@Test
	void appUserEmailIsUniqueIgnoringCase() {
		insertUser("Runner@Example.com");

		assertThatThrownBy(() -> insertUser("runner@example.com"))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void appUserRejectsUnknownUnits() {
		assertThatThrownBy(() -> jdbc.update("""
				insert into app_user (id, email, password_hash, preferred_units)
				values (gen_random_uuid(), 'a@example.com', 'x', 'FURLONGS')
				"""))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	private void insertUser(String email) {
		jdbc.update("insert into app_user (id, email, password_hash) values (gen_random_uuid(), ?, 'x')", email);
	}

}
