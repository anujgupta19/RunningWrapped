package com.runningwrapped.users;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UserService {

	private final UserRepository users;

	UserService(UserRepository users) {
		this.users = users;
	}

	@Transactional(readOnly = true)
	UserResponse getUser(UUID id) {
		return users.findById(id)
				.map(UserResponse::from)
				.orElseThrow(() -> new UserNotFoundException(id));
	}

	/**
	 * No {@code users.save(user)} call: the entity was loaded in this transaction, so Hibernate is tracking it. At commit
	 * it compares the fields to the loaded snapshot and issues an UPDATE if anything changed ("dirty checking").
	 */
	@Transactional
	UserResponse updateUser(UUID id, UpdateUserRequest request) {
		var user = users.findById(id).orElseThrow(() -> new UserNotFoundException(id));
		user.updatePreferences(request.displayName(), request.preferredUnits(), request.timezone(), request.weekStart());
		return UserResponse.from(user);
	}

}
