package com.runningwrapped.users;

import java.util.UUID;

import com.runningwrapped.common.NotFoundException;

class UserNotFoundException extends NotFoundException {

	UserNotFoundException(UUID id) {
		super("User " + id + " not found");
	}

}
