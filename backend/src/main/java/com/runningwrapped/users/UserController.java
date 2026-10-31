package com.runningwrapped.users;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
class UserController {

	private final UserService userService;

	UserController(UserService userService) {
		this.userService = userService;
	}

	// TODO(1.12): replace the X-User-Id header with the authenticated user. Never deploy with this header in place.
	@GetMapping("/me")
	UserResponse me(@RequestHeader("X-User-Id") UUID userId) {
		return userService.getUser(userId);
	}

	// @Valid runs the constraints on UpdateUserRequest before this method is called. On failure Spring throws
	// MethodArgumentNotValidException, so this method only ever sees valid input.
	@PatchMapping("/me")
	UserResponse updateMe(@RequestHeader("X-User-Id") UUID userId, @Valid @RequestBody UpdateUserRequest request) {
		return userService.updateUser(userId, request);
	}

}
