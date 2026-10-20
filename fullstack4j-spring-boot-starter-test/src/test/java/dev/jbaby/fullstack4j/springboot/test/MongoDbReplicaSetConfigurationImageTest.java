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

import java.time.Duration;

import dev.jbaby.fullstack4j.springboot.test.shop.ShopApplication;
import dev.jbaby.fullstack4j.springboot.test.shop.billing.Invoice;
import dev.jbaby.fullstack4j.springboot.test.shop.billing.InvoiceRepository;
import dev.jbaby.fullstack4j.springboot.test.shop.ordering.Orders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.modulith.events.core.EventPublicationRegistry;

import static dev.jbaby.fullstack4j.springboot.test.MongoDbReplicaSetConfigurationTest.serverVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Needs Docker. Runs MongoDB 7.0, the image that still starts on Linux kernels 6.19 to 7.0.13, which
 * MongoDB 8 refuses: the events have to reach their listener on it, too.
 *
 * @author Daniel Bartl
 */
@SpringBootTest(classes = ShopApplication.class,
		properties = MongoDbReplicaSetConfiguration.IMAGE_PROPERTY + "=mongo:7.0")
@Import(MongoDbReplicaSetConfiguration.class)
class MongoDbReplicaSetConfigurationImageTest {

	@Autowired
	Orders orders;

	@Autowired
	InvoiceRepository invoices;

	@Autowired
	EventPublicationRegistry publications;

	@Autowired
	MongoTemplate mongo;

	@Test
	void runsTheConfiguredImage() {
		assertThat(serverVersion(mongo)).startsWith("7.0.");
	}

	@Test
	void anEventPublishedInATransactionReachesItsListener() {
		invoices.deleteAll();
		orders.place("ACME");

		await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
			assertThat(invoices.findAll()).extracting(Invoice::customer).containsExactly("ACME");
			assertThat(publications.findIncompletePublications()).isEmpty();
		});
	}
}
