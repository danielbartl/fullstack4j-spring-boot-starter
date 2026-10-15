package com.example.shop.ordering.internal;

import com.example.shop.ordering.OrderPlaced;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class Orders {

	private final OrderRepository orders;

	private final ApplicationEventPublisher events;

	Orders(OrderRepository orders, ApplicationEventPublisher events) {
		this.orders = orders;
		this.events = events;
	}

	public Order place(String customer, int amount) {
		Order order = orders.save(new Order(null, customer, amount));
		events.publishEvent(new OrderPlaced(customer, amount));
		return order;
	}
}
