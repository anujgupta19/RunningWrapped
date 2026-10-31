package com.runningwrapped.users;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")
class AppUser {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String passwordHash;

	private String displayName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Units preferredUnits = Units.MILES;

	@Column(nullable = false)
	private ZoneId timezone = ZoneId.of("UTC");

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DayOfWeek weekStart = DayOfWeek.MONDAY;

	private Instant aiConsentAt;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private Instant updatedAt;

	/** Required by JPA, which instantiates entities by reflection. Not for application code. */
	protected AppUser() {
	}

	AppUser(String email, String passwordHash) {
		this.email = email;
		this.passwordHash = passwordHash;
	}

	/**
	 * Applies a partial update: null arguments leave the field unchanged, and an empty display name clears it. No setters
	 * on purpose. Changes go through methods that state the intent and keep the rules in one place.
	 */
	void updatePreferences(String displayName, Units preferredUnits, ZoneId timezone, DayOfWeek weekStart) {
		if (displayName != null) {
			this.displayName = displayName.isEmpty() ? null : displayName.strip();
		}
		if (preferredUnits != null) {
			this.preferredUnits = preferredUnits;
		}
		if (timezone != null) {
			this.timezone = timezone;
		}
		if (weekStart != null) {
			this.weekStart = weekStart;
		}
	}

	UUID getId() {
		return id;
	}

	String getEmail() {
		return email;
	}

	String getPasswordHash() {
		return passwordHash;
	}

	String getDisplayName() {
		return displayName;
	}

	Units getPreferredUnits() {
		return preferredUnits;
	}

	ZoneId getTimezone() {
		return timezone;
	}

	DayOfWeek getWeekStart() {
		return weekStart;
	}

	Instant getAiConsentAt() {
		return aiConsentAt;
	}

	Instant getCreatedAt() {
		return createdAt;
	}

	Instant getUpdatedAt() {
		return updatedAt;
	}

}
