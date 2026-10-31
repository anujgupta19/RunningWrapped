package com.runningwrapped.users;

import java.time.DayOfWeek;
import java.time.ZoneId;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Partial update of the current user. PATCH rule: a {@code null} (or absent) field means "leave unchanged".
 * {@code displayName = ""} clears the display name; the other fields can't be cleared.
 *
 * <p>An invalid {@code timezone} (e.g. "Mars/Olympus") never reaches validation: Jackson fails to build the
 * {@link ZoneId} while reading the JSON, which the error handler also turns into a 400.
 */
record UpdateUserRequest(
		// Empty, or 1-100 chars that aren't all whitespace. Bean Validation treats null as valid, which is what PATCH needs.
		@Size(max = 100) @Pattern(regexp = "^$|.*\\S.*", message = "must not be only whitespace") String displayName,
		Units preferredUnits,
		ZoneId timezone,
		DayOfWeek weekStart) {
}
