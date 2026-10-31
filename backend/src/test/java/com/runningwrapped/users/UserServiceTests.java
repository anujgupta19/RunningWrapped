package com.runningwrapped.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.runningwrapped.TestcontainersConfiguration;

import jakarta.persistence.EntityManager;

/**
 * Service against a real database: the JPA slice plus UserService ({@code @Import} adds it to the slice). Mocking the
 * repository here would hide the thing we want to prove: that dirty checking persists the update without save().
 */
@DataJpaTest
@Import({ TestcontainersConfiguration.class, UserService.class })
class UserServiceTests {

	@Autowired
	UserService userService;

	@Autowired
	UserRepository users;

	@Autowired
	EntityManager entityManager;

	@Test
	void persistsPartialUpdateWithoutCallingSave() {
		var id = users.saveAndFlush(new AppUser("runner@example.com", "{noop}hash")).getId();
		entityManager.clear();

		userService.updateUser(id, new UpdateUserRequest("  Speedy ", Units.KILOMETERS, null, DayOfWeek.SUNDAY));
		// The test's surrounding transaction never commits, so flush to send the pending UPDATE, then clear the cache.
		entityManager.flush();
		entityManager.clear();

		var reloaded = users.findById(id).orElseThrow();
		assertThat(reloaded.getDisplayName()).isEqualTo("Speedy");
		assertThat(reloaded.getPreferredUnits()).isEqualTo(Units.KILOMETERS);
		assertThat(reloaded.getWeekStart()).isEqualTo(DayOfWeek.SUNDAY);
		assertThat(reloaded.getTimezone()).as("null means unchanged").isEqualTo(ZoneId.of("UTC"));
	}

	@Test
	void emptyDisplayNameClearsIt() {
		var user = new AppUser("runner@example.com", "{noop}hash");
		user.updatePreferences("Speedy", null, null, null);
		var id = users.saveAndFlush(user).getId();

		var response = userService.updateUser(id, new UpdateUserRequest("", null, null, null));

		assertThat(response.displayName()).isNull();
	}

	@Test
	void throwsNotFoundForUnknownUser() {
		var id = UUID.randomUUID();

		assertThatThrownBy(() -> userService.updateUser(id, new UpdateUserRequest(null, null, null, null)))
				.isInstanceOf(UserNotFoundException.class);
	}

}
