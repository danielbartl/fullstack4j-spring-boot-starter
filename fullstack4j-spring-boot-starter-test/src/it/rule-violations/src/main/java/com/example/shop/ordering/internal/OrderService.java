package com.example.shop.ordering.internal;

import java.util.List;

import com.example.shop.ordering.OrderShipped;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// named after its stereotype, injected through a field, and calling its own transactional method
@Service
public class OrderService {

	@Autowired
	private ApplicationEventPublisher events;

	public void shipAll(List<String> ids) {
		for (String id : ids) {
			ship(id);
		}
	}

	@Transactional
	public void ship(String id) {
		events.publishEvent(new OrderShipped(id));
	}
}
