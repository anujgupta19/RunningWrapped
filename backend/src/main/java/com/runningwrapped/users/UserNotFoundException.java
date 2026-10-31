package com.runningwrapped.users;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// @ResponseStatus is the simplest way to map an exception to a status. Step 1.6 replaces it with a central handler.
@ResponseStatus(HttpStatus.NOT_FOUND)
class UserNotFoundException extends RuntimeException {

	UserNotFoundException(UUID id) {
		super("User " + id + " not found");
	}

}
