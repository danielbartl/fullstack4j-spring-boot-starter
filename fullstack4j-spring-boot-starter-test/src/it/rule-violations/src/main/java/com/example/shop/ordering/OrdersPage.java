package com.example.shop.ordering;

import com.example.shop.ordering.internal.Order;
import com.example.shop.ordering.internal.OrderRepository;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.transaction.annotation.Transactional;

public class OrdersPage extends WebPage {

	// a repository instead of the facade
	@SpringBean
	private OrderRepository repository;

	// a document kept in the page between requests
	private Order lastShipped;

	// a transaction on something Spring never proxies
	@Transactional
	void refresh() {
	}
}
