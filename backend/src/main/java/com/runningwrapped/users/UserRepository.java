package com.runningwrapped.users;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface UserRepository extends JpaRepository<AppUser, UUID> {

	/**
	 * Case-insensitive lookup. Written by hand instead of the derived {@code findByEmailIgnoreCase}, because Spring Data
	 * would generate {@code upper(email) = upper(?)}, which can't use our {@code lower(email)} unique index.
	 */
	@Query("select u from AppUser u where lower(u.email) = lower(:email)")
	Optional<AppUser> findByEmail(String email);

}
