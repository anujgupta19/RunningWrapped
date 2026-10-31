package com.runningwrapped.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/**
 * Web "slice" test: starts only the MVC layer for UserController (routing, JSON, validation, and the
 * {@code @RestControllerAdvice} error mapping). No database. The service is replaced by a Mockito mock, so we test the
 * HTTP contract in isolation.
 */
@WebMvcTest(UserController.class)
class UserControllerTests {

	private static final String PROBLEM_JSON = "application/problem+json";

	@Autowired
	MockMvcTester mvc;

	@MockitoBean
	UserService userService;

	@Test
	void returnsCurrentUser() {
		var id = UUID.randomUUID();
		given(userService.getUser(id)).willReturn(user(id, "Runner", Units.MILES, "America/New_York"));

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
	void returns404ProblemWhenUserDoesNotExist() {
		var id = UUID.randomUUID();
		given(userService.getUser(id)).willThrow(new UserNotFoundException(id));

		var result = mvc.get().uri("/api/v1/users/me").header("X-User-Id", id.toString()).exchange();

		assertThat(result).hasStatus(HttpStatus.NOT_FOUND).hasContentType(PROBLEM_JSON);
		assertThat(result).bodyJson().extractingPath("$.status").isEqualTo(404);
		assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("User " + id + " not found");
		assertThat(result).bodyJson().extractingPath("$.instance").isEqualTo("/api/v1/users/me");
	}

	@Test
	void returns400ProblemWithoutUserHeader() {
		var result = mvc.get().uri("/api/v1/users/me").exchange();

		assertThat(result).hasStatus(HttpStatus.BAD_REQUEST).hasContentType(PROBLEM_JSON);
	}

	@Test
	void returns400ForMalformedUserId() {
		assertThat(mvc.get().uri("/api/v1/users/me").header("X-User-Id", "not-a-uuid"))
				.hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	void returnsGeneric500ProblemWithoutLeakingInternals() {
		var id = UUID.randomUUID();
		given(userService.getUser(id)).willThrow(new IllegalStateException("connection to db-internal:5432 refused"));

		var result = mvc.get().uri("/api/v1/users/me").header("X-User-Id", id.toString()).exchange();

		assertThat(result).hasStatus(HttpStatus.INTERNAL_SERVER_ERROR).hasContentType(PROBLEM_JSON);
		assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("An unexpected error occurred.");
		assertThat(result).bodyText().doesNotContain("db-internal");
	}

	@Test
	void updatesCurrentUser() {
		var id = UUID.randomUUID();
		var expectedRequest = new UpdateUserRequest("Speedy", Units.KILOMETERS, ZoneId.of("Europe/London"), null);
		given(userService.updateUser(id, expectedRequest))
				.willReturn(user(id, "Speedy", Units.KILOMETERS, "Europe/London"));

		var result = patchMe(id, """
				{"displayName": "Speedy", "preferredUnits": "KILOMETERS", "timezone": "Europe/London"}
				""");

		assertThat(result).hasStatusOk();
		assertThat(result).bodyJson().extractingPath("$.displayName").isEqualTo("Speedy");
		assertThat(result).bodyJson().extractingPath("$.preferredUnits").isEqualTo("KILOMETERS");
	}

	@Test
	void acceptsEmptyDisplayNameToClearIt() {
		var id = UUID.randomUUID();
		given(userService.updateUser(eq(id), any())).willReturn(user(id, null, Units.MILES, "UTC"));

		assertThat(patchMe(id, """
				{"displayName": ""}
				""")).hasStatusOk();
	}

	@Test
	void rejectsInvalidFieldsWithPerFieldErrors() {
		var id = UUID.randomUUID();

		var result = patchMe(id, """
				{"displayName": "%s"}
				""".formatted("x".repeat(101)));

		assertThat(result).hasStatus(HttpStatus.BAD_REQUEST).hasContentType(PROBLEM_JSON);
		assertThat(result).bodyJson().extractingPath("$.errors.displayName").isEqualTo("size must be between 0 and 100");
		verifyNoInteractions(userService);
	}

	@Test
	void rejectsWhitespaceOnlyDisplayName() {
		var result = patchMe(UUID.randomUUID(), """
				{"displayName": "   "}
				""");

		assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
		assertThat(result).bodyJson().extractingPath("$.errors.displayName").isEqualTo("must not be only whitespace");
	}

	@Test
	void rejectsUnknownTimezoneAsBadRequest() {
		var result = patchMe(UUID.randomUUID(), """
				{"timezone": "Mars/Olympus"}
				""");

		// Fails while Jackson reads the JSON (before validation), handled by ResponseEntityExceptionHandler.
		assertThat(result).hasStatus(HttpStatus.BAD_REQUEST).hasContentType(PROBLEM_JSON);
		verifyNoInteractions(userService);
	}

	@Test
	void rejectsUnknownUnitsAsBadRequest() {
		assertThat(patchMe(UUID.randomUUID(), """
				{"preferredUnits": "FURLONGS"}
				""")).hasStatus(HttpStatus.BAD_REQUEST);
	}

	private MvcTestResult patchMe(UUID id, String json) {
		return mvc.patch().uri("/api/v1/users/me")
				.header("X-User-Id", id.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json)
				.exchange();
	}

	private static UserResponse user(UUID id, String displayName, Units units, String zone) {
		return new UserResponse(id, "runner@example.com", displayName, units, ZoneId.of(zone), DayOfWeek.MONDAY, null,
				Instant.parse("2026-10-31T12:00:00Z"));
	}

}
