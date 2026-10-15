/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.jbaby.fullstack4j.springboot;

import java.util.Map;

import org.bson.UuidRepresentation;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.mongodb.autoconfigure.MongoProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Daniel Bartl
 */
class MongoUuidRepresentationDefaultTest {

	@Test
	void defaultsToTheStandardRepresentation() {
		StandardEnvironment environment = new StandardEnvironment();

		new MongoUuidRepresentationDefault().postProcessEnvironment(environment, new SpringApplication());

		assertThat(environment.getProperty("spring.mongodb.representation.uuid")).isEqualTo("standard");
	}

	@Test
	void leavesAPropertyOfTheApplicationAlone() {
		StandardEnvironment environment = new StandardEnvironment();
		environment.getPropertySources().addFirst(new MapPropertySource("application",
				Map.of("spring.mongodb.representation.uuid", "java-legacy")));

		new MongoUuidRepresentationDefault().postProcessEnvironment(environment, new SpringApplication());

		assertThat(environment.getProperty("spring.mongodb.representation.uuid")).isEqualTo("java-legacy");
	}

	@Test
	void leavesAnEnvironmentVariableAlone() {
		StandardEnvironment environment = new StandardEnvironment();
		environment.getPropertySources().replace(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
				new SystemEnvironmentPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
						Map.of("SPRING_MONGODB_REPRESENTATION_UUID", "c-sharp-legacy")));

		new MongoUuidRepresentationDefault().postProcessEnvironment(environment, new SpringApplication());

		assertThat(environment.getProperty("spring.mongodb.representation.uuid")).isEqualTo("c-sharp-legacy");
	}

	@Test
	void reachesSpringBootsMongoPropertiesThroughSpringFactories() {
		SpringApplication application = new SpringApplication(MongoPropertiesOnly.class);
		application.setWebApplicationType(WebApplicationType.NONE);

		try (ConfigurableApplicationContext context = application.run()) {
			assertThat(context.getBean(MongoProperties.class).getRepresentation().getUuid())
					.isEqualTo(UuidRepresentation.STANDARD);
		}
	}

	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties(MongoProperties.class)
	static class MongoPropertiesOnly {
	}
}
