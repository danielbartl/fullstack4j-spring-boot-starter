package com.example.shop.billing.internal;

import com.example.shop.ordering.OrderShipped;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
class Invoices {

	@ApplicationModuleListener
	void on(OrderShipped event) {
	}

	// a document sent as an event
	@ApplicationModuleListener
	void onRaised(Invoice invoice) {
	}

	// another slice's event, made up here
	OrderShipped pretendShipped(String orderId) {
		return new OrderShipped(orderId);
	}
}
