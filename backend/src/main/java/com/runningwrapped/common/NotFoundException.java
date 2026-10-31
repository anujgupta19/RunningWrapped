package com.runningwrapped.common;

/**
 * Base class for "this resource doesn't exist (for this user)". {@link GlobalExceptionHandler} maps every subclass to a
 * 404, so features only need {@code class ActivityNotFoundException extends NotFoundException}.
 *
 * <p>Unchecked (extends RuntimeException) like nearly all Spring app exceptions: callers can't do anything useful with
 * it except let it bubble up to the handler, so forcing every layer to declare {@code throws} would add noise.
 *
 * <p>The message is sent to the client as the Problem Details "detail", so it must never contain health data or secrets.
 */
public abstract class NotFoundException extends RuntimeException {

	protected NotFoundException(String message) {
		super(message);
	}

}
