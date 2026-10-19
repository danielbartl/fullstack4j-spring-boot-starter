package com.example.shop;

import dev.jbaby.fullstack4j.springboot.test.Fullstack4jRules;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ArchitectureTests {

	@Test
	void modulesOnlyUseEachOthersPublicApi() {
		ApplicationModules.of(ShopApplication.class).verify();
	}

	@Test
	void followsTheFullstack4jRules() {
		Fullstack4jRules.builder(ShopApplication.class)
			.build()
			.checkAll();
	}
}
