package com.runningwrapped.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * Web "slice" test: starts only the MVC layer for UserController (routing, JSON, error mapping). No database. The service
 * is replaced by a Mockito mock, so we test the HTTP contract in isolation.
 */
@WebMvcTest(UserController.class)
class UserControllerTests {

	@Autowired
	MockMvcTester mvc;

	@MockitoBean
	UserService userService;

	@Test
	void returnsCurrentUser() {
		var id = UUID.randomUUID();
		given(userService.getUser(id)).willReturn(new UserResponse(id, "runner@example.com", "Runner", Units.MILES,
				ZoneId.of("America/New_York"), DayOfWeek.MONDAY, null, Instant.parse("2026-10-31T12:00:00Z")));

		var result = mvc.get().uri("/api/v1/users/me").header("X-User-Id", id.toString());

		assertThat(result).hasStatusOk();
		assertThat(result).bodyJson().extractingPath("$.id").isEqualTo(id.toString());
		assertThat(result).bodyJson().extractingPath("$.email").isEqualTo("runner@example.com");
		assertThat(result).bodyJson().extractingPath("$.preferredUnits").isEqualTo("MILES");
		assertThat(result).bodyJson().extractingPath("$.timezone").isEqualTo("America/New_York");
		assertThat(result).bodyJson().extractingPath("$.createdAt").isEqualTo("2026-10-31T12:00:00Z");
		assertThat(result).bodyJson().doesNotHavePath("$.passwordHash");
	}

	@Test
	void returns404WhenUserDoesNotExist() {
		var id = UUID.randomUUID();
		given(userService.getUser(id)).willThrow(new UserNotFoundException(id));

		assertThat(mvc.get().uri("/api/v1/users/me").header("X-User-Id", id.toString()))
				.hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void returns400WithoutUserHeader() {
		assertThat(mvc.get().uri("/api/v1/users/me")).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	void returns400ForMalformedUserId() {
		assertThat(mvc.get().uri("/api/v1/users/me").header("X-User-Id", "not-a-uuid"))
				.hasStatus(HttpStatus.BAD_REQUEST);
	}

}
