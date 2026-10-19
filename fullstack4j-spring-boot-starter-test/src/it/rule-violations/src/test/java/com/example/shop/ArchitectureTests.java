package com.example.shop;

import dev.jbaby.fullstack4j.springboot.test.Fullstack4jRules;
import org.junit.jupiter.api.Test;

class ArchitectureTests {

	@Test
	void followsTheFullstack4jRules() {
		Fullstack4jRules.builder(ShopApplication.class)
			.build()
			.checkAll();
	}
}
