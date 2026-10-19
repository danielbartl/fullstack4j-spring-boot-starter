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

import java.util.List;

import com.tngtech.archunit.ArchConfiguration;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import dev.jbaby.fullstack4j.springboot.test.rules.conforming.ShopApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks each fullstack4j rule against three applications under {@code rules}: one that follows every
 * rule, one that breaks each rule at least once, and one with nothing to check yet. Taikai's own rules
 * are checked end to end by the integration tests, since Taikai leaves test classes like these out.
 *
 * @author Daniel Bartl
 */
class Fullstack4jRulesTest {

	static final String CONFORMING = "dev.jbaby.fullstack4j.springboot.test.rules.conforming";

	static final String VIOLATING = "dev.jbaby.fullstack4j.springboot.test.rules.violating";

	static final String EMPTY = "dev.jbaby.fullstack4j.springboot.test.rules.empty";

	@AfterEach
	void resetArchUnit() {
		ArchConfiguration.get().reset();
	}

	@Test
	void aConformingApplicationBreaksNoRule() {
		JavaClasses classes = classesIn(CONFORMING);

		assertThat(Fullstack4jRules.rules(CONFORMING))
			.allSatisfy((rule) -> assertThat(violations(rule, classes)).as(rule.getDescription()).isEmpty());
	}

	@Test
	void anApplicationWithNothingToCheckYetBreaksNoRule() {
		ArchConfiguration.get().setProperty("archRule.failOnEmptyShould", "true");
		JavaClasses classes = classesIn(EMPTY);

		assertThat(Fullstack4jRules.rules(EMPTY))
			.allSatisfy((rule) -> assertThat(violations(rule, classes)).as(rule.getDescription()).isEmpty());
	}

	@Test
	void slicesAreNamedAfterBusinessCapabilities() {
		assertThat(violations(Fullstack4jRules.slicesAreBusinessCapabilities(VIOLATING)))
			.satisfiesExactlyInAnyOrder(
					(violation) -> assertThat(violation)
						.contains("violating.shared.Money is in slice shared, which isn't named after"),
					(violation) -> assertThat(violation)
						.contains("violating.ordering.web.OrderForm is in ordering.web, but below a slice there is only"));
	}

	@Test
	void slicesShowOnlyTheirApi() {
		assertThat(violations(Fullstack4jRules.slicesShowOnlyTheirApi(VIOLATING)))
			.satisfiesExactly((violation) -> assertThat(violation)
				.contains("violating.billing.BillingHelper is in the base package of billing"));
	}

	@Test
	void documentsStayInternal() {
		assertThat(violations(Fullstack4jRules.documentsStayInternal(VIOLATING)))
			.satisfiesExactly((violation) -> assertThat(violation).contains("violating.billing.Note>"));
	}

	@Test
	void dataStaysBehindTheFacade() {
		assertThat(violations(Fullstack4jRules.dataStaysBehindTheFacade(VIOLATING)))
			.allSatisfy((violation) -> assertThat(violation).containsAnyOf("violating.ordering.OrdersPage",
					"violating.billing.Note"))
			.anySatisfy((violation) -> assertThat(violation).contains("violating.ordering.OrdersPage.repository")
				.contains("OrderRepository"))
			.anySatisfy((violation) -> assertThat(violation).contains("violating.billing.Note")
				.contains("org.springframework.data.mongodb.core.mapping.Document"));
	}

	@Test
	void listenersHearRecords() {
		assertThat(violations(Fullstack4jRules.listenersHearRecords()))
			.satisfiesExactlyInAnyOrder(
					(violation) -> assertThat(violation).contains("Cancellations.on(")
						.contains("listens to " + VIOLATING + ".ordering.internal.OrderCancelled, which isn't a record"),
					(violation) -> assertThat(violation).contains("Invoices.onRaised(")
						.contains("listens to " + VIOLATING + ".billing.internal.Invoice, which is a document"));
	}

	@Test
	void onlyTheOwningSliceCreatesItsEvents() {
		assertThat(violations(Fullstack4jRules.onlyTheOwningSliceCreatesItsEvents(VIOLATING)))
			.satisfiesExactly((violation) -> assertThat(violation).contains("Invoices.pretendShipped(")
				.contains("creates " + VIOLATING + ".ordering.OrderShipped, an event of ordering"));
	}

	@Test
	void componentsHoldNoDocuments() {
		assertThat(violations(Fullstack4jRules.componentsHoldNoDocuments()))
			.satisfiesExactlyInAnyOrder(
					(violation) -> assertThat(violation).contains("violating.ordering.OrdersPage.lastShipped>"),
					// a document captured by an anonymous component ends up in a field, too
					(violation) -> assertThat(violation).contains("violating.ordering.OrdersPage$1.val$first>"));
	}

	@Test
	void componentsAreNotSpringBeans() {
		assertThat(violations(Fullstack4jRules.componentsAreNotSpringBeans()))
			.satisfiesExactlyInAnyOrder(
					(violation) -> assertThat(violation)
						.contains("violating.billing.internal.InvoicePanel is annotated with @Component"),
					(violation) -> assertThat(violation)
						.contains("violating.billing.InvoicesPage.refresh() is annotated with @Transactional"));
	}

	@Test
	void checksTheApplicationsPackage() {
		assertThat(Fullstack4jRules.builder(ShopApplication.class).build().namespace()).isEqualTo(CONFORMING);
	}

	static List<String> violations(ArchRule rule) {
		return violations(rule, classesIn(VIOLATING));
	}

	static List<String> violations(ArchRule rule, JavaClasses classes) {
		return rule.evaluate(classes).getFailureReport().getDetails();
	}

	static JavaClasses classesIn(String application) {
		return new ClassFileImporter().importPackages(application);
	}
}
