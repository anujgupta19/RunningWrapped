package com.runningwrapped;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Starts a throwaway Postgres in Docker for tests. {@code @ServiceConnection} tells Spring Boot to
 * build the DataSource from this container's random port and credentials, so no test config is needed.
 */
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		// Same major version as compose.yaml so tests match dev and prod.
		return new PostgreSQLContainer(DockerImageName.parse("postgres:18"));
	}

}
