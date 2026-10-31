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

}
