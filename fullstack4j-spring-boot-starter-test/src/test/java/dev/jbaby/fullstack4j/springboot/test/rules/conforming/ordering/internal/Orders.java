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
package dev.jbaby.fullstack4j.springboot.test.rules.conforming.ordering.internal;

import java.time.Instant;
import java.util.List;

import dev.jbaby.fullstack4j.springboot.test.rules.conforming.ordering.OpenOrder;
import dev.jbaby.fullstack4j.springboot.test.rules.conforming.ordering.OpenOrders;
import dev.jbaby.fullstack4j.springboot.test.rules.conforming.ordering.OrderShipped;
import dev.jbaby.fullstack4j.springboot.test.rules.conforming.ordering.OrderStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class Orders implements OpenOrders {

	private final OrderRepository repository;

	private final ApplicationEventPublisher events;

	Orders(OrderRepository repository, ApplicationEventPublisher events) {
		this.repository = repository;
		this.events = events;
	}

	public List<Order> open() {
		return this.repository.findByShippedAtIsNull();
	}

	public void ship(String id) {
		Order order = this.repository.findById(id).orElseThrow();
		this.repository.save(new Order(order.id(), order.customer(), Instant.now()));
		this.events.publishEvent(new OrderShipped(id, OrderStatus.SHIPPED));
	}

	@Override
	public List<OpenOrder> forCustomer(String customer) {
		return open().stream()
			.filter((order) -> order.customer().equals(customer))
			.map((order) -> new OpenOrder(order.id(), order.customer()))
			.toList();
	}
}
