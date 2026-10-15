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
package dev.jbaby.fullstack4j.springboot.test;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * A real MongoDB in Docker, started as a replica set and connected to the application through
 * {@link ServiceConnection @ServiceConnection}.
 * <p>
 * It has to be a replica set: MongoDB only supports transactions on one, and Spring Modulith runs
 * every {@code @ApplicationModuleListener} in a transaction. A single MongoDB node would fail the
 * first event a listener receives.
 * </p>
 * <p>
 * Import it into tests, and into the application started for development from the test classpath:
 * </p>
 * <pre class="code">
 * &#064;WicketTest
 * &#064;Import(MongoDbReplicaSetConfiguration.class)
 * class OrdersPageTests { ... }
 *
 * public static void main(String[] args) {
 *     SpringApplication.from(ShopApplication::main)
 *         .with(MongoDbReplicaSetConfiguration.class)
 *         .run(args);
 * }
 * </pre>
 * <p>
 * Spring keeps test contexts cached, so all tests sharing a context share one container.
 * </p>
 *
 * @author Daniel Bartl
 */
@TestConfiguration(proxyBeanMethods = false)
public class MongoDbReplicaSetConfiguration {

	/**
	 * Creates the configuration. Spring does that, for {@code @Import} and
	 * {@code SpringApplication.from(...).with(...)}.
	 */
	public MongoDbReplicaSetConfiguration() {
	}

	@Bean
	@ServiceConnection
	MongoDBContainer mongoDbContainer() {
		return new MongoDBContainer(DockerImageName.parse("mongo:8")).withReplicaSet();
	}
}
