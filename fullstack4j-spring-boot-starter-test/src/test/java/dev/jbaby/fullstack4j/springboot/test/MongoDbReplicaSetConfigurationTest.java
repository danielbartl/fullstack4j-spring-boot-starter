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
import org.springframework.modulith.events.core.EventPublicationRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Needs Docker. An {@code @ApplicationModuleListener} runs in a transaction of its own, and Spring
 * Modulith records every publication, with a UUID, in the publishing transaction: both only work on a
 * replica set with the UUID representation the starter sets.
 *
 * @author Daniel Bartl
 */
@SpringBootTest(classes = ShopApplication.class)
@Import(MongoDbReplicaSetConfiguration.class)
class MongoDbReplicaSetConfigurationTest {

	@Autowired
	Orders orders;

	@Autowired
	InvoiceRepository invoices;

	@Autowired
	EventPublicationRegistry publications;

	@Test
	void anEventPublishedInATransactionReachesItsListener() {
		orders.place("ACME");

		await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
			assertThat(invoices.findAll()).extracting(Invoice::customer).containsExactly("ACME");
			assertThat(publications.findIncompletePublications()).isEmpty();
		});
	}
}
