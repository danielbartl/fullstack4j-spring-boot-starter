package com.example.shop.billing.internal;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class Invoices {

	private final InvoiceRepository invoices;

	Invoices(InvoiceRepository invoices) {
		this.invoices = invoices;
	}

	public List<Invoice> page(long first, long count) {
		return invoices.findAll().stream().skip(first).limit(count).toList();
	}

	public long count() {
		return invoices.count();
	}
}
