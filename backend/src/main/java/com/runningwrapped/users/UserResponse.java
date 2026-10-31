package com.runningwrapped.users;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

/** What the API exposes about a user. Deliberately excludes passwordHash and internal timestamps like updatedAt. */
record UserResponse(
		UUID id,
		String email,
		String displayName,
		Units preferredUnits,
		ZoneId timezone,
		DayOfWeek weekStart,
		Instant aiConsentAt,
		Instant createdAt) {

	static UserResponse from(AppUser user) {
		return new UserResponse(
				user.getId(),
				user.getEmail(),
				user.getDisplayName(),
				user.getPreferredUnits(),
				user.getTimezone(),
				user.getWeekStart(),
				user.getAiConsentAt(),
				user.getCreatedAt());
	}

}
