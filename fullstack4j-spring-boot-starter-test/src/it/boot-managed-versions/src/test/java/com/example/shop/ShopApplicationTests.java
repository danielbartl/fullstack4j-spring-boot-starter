package com.example.shop;

import java.time.Duration;

import com.example.shop.billing.InvoicesPage;
import com.example.shop.billing.internal.InvoiceRepository;
import com.example.shop.ordering.internal.OrderRepository;
import com.example.shop.ordering.internal.Orders;
import dev.jbaby.fullstack4j.springboot.test.MongoDbReplicaSetConfiguration;
import dev.jbaby.wicket.oat.components.OatEmptyState;
import dev.jbaby.wicket.springboot.test.WicketTest;
import dev.jbaby.wicket.testkit.WicketTestkit;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import static dev.jbaby.wicket.testkit.locator.Locator.locate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Needs Docker. Runs the whole stack the starters bring, at the versions a Spring Boot application
 * resolves: a Wicket page built from Wicket Oat components and tested with wicket-testkit, MongoDB, and
 * an event from one Spring Modulith module handled by another in a transaction of its own.
 */
@WicketTest
@Import(MongoDbReplicaSetConfiguration.class)
class ShopApplicationTests {

	@Autowired
	Orders orders;

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	InvoiceRepository invoiceRepository;

	@BeforeEach
	void startEmpty() {
		orderRepository.deleteAll();
		invoiceRepository.deleteAll();
	}

	@Test
	void showsAnEmptyStateWithoutInvoices(WicketTester tester, WicketTestkit kit) {
		tester.startPage(InvoicesPage.class);

		tester.assertRenderedPage(InvoicesPage.class);
		kit.assertVisible(locate(OatEmptyState.class));
		tester.assertContains("No invoices yet");
	}

	@Test
	void placingAnOrderRaisesAnInvoiceThatThePageLists(WicketTester tester) {
		orders.place("ACME", 42);

		await().atMost(Duration.ofSeconds(10)).until(() -> invoiceRepository.count() == 1);

		tester.startPage(InvoicesPage.class);
		tester.assertContains("ACME");
		tester.assertContains("42");
	}
}
