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

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Stores UUIDs in MongoDB in the standard binary representation, unless the application says
 * otherwise.
 * <p>
 * Spring Modulith's event publication registry stores a UUID with every event, and the MongoDB
 * driver refuses to encode one while the representation is left unspecified, which is Spring Boot's
 * default. Without this default, publishing the first event fails with "The uuidRepresentation has
 * not been specified".
 * </p>
 * <p>
 * The default is added as the property source with the lowest precedence, so a value set anywhere
 * else, in {@code application.properties}, in the environment or on the command line, still wins.
 * </p>
 *
 * @author Daniel Bartl
 */
public class MongoUuidRepresentationDefault implements EnvironmentPostProcessor {

	static final String PROPERTY_SOURCE_NAME = "fullstack4jDefaults";

	static final String UUID_REPRESENTATION = "spring.mongodb.representation.uuid";

	/**
	 * Creates the post-processor. Spring Boot does that, from {@code META-INF/spring.factories}.
	 */
	public MongoUuidRepresentationDefault() {
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		environment.getPropertySources()
				.addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, Map.of(UUID_REPRESENTATION, "standard")));
	}
}
