package com.example.shop.billing.internal;

import com.example.shop.ordering.OrderPlaced;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
class InvoiceRaiser {

	private final InvoiceRepository invoices;

	InvoiceRaiser(InvoiceRepository invoices) {
		this.invoices = invoices;
	}

	@ApplicationModuleListener
	void on(OrderPlaced order) {
		invoices.save(new Invoice(null, order.customer(), order.amount()));
	}
}
