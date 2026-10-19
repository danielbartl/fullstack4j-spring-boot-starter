package com.example.shop.billing;

// a helper in the slice's public API
public class BillingHelper {

	public String number(String orderId) {
		return "INV-" + orderId;
	}
}
