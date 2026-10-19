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
package dev.jbaby.fullstack4j.springboot.test.rules.conforming.billing.internal;

import java.util.List;

import dev.jbaby.fullstack4j.springboot.test.rules.conforming.ordering.OrderShipped;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
public class Invoices {

	private final InvoiceRepository repository;

	Invoices(InvoiceRepository repository) {
		this.repository = repository;
	}

	@ApplicationModuleListener
	void on(OrderShipped event) {
		if (!this.repository.existsByOrderId(event.orderId())) {
			this.repository.save(new Invoice(null, event.orderId()));
		}
	}

	public List<Invoice> all() {
		return this.repository.findAll();
	}
}
