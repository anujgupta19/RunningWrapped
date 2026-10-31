package com.runningwrapped.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Creates a user to experiment with locally. Only exists in the dev profile; never runs in tests or prod. */
@Component
@Profile("dev")
class DevUserSeeder implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DevUserSeeder.class);

	private static final String DEV_EMAIL = "dev@runningwrapped.local";

	private final UserRepository users;

	DevUserSeeder(UserRepository users) {
		this.users = users;
	}

	@Override
	public void run(ApplicationArguments args) {
		var user = users.findByEmail(DEV_EMAIL)
				.orElseGet(() -> users.save(new AppUser(DEV_EMAIL, "{noop}dev-password")));
		log.info("Dev user ready. Try: curl -H 'X-User-Id: {}' localhost:8081/api/v1/users/me", user.getId());
	}

}
