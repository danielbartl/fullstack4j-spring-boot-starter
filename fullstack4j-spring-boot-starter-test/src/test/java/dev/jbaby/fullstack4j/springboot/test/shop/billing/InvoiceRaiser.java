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
package dev.jbaby.fullstack4j.springboot.test.shop.billing;

import dev.jbaby.fullstack4j.springboot.test.shop.ordering.OrderPlaced;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
class InvoiceRaiser {

	private final InvoiceRepository invoices;

	InvoiceRaiser(InvoiceRepository invoices) {
		this.invoices = invoices;
	}

	@ApplicationModuleListener
	void on(OrderPlaced event) {
		invoices.save(new Invoice(null, event.customer()));
	}
}
