package com.example.demo;

import dev.jbaby.fullstack4j.springboot.test.Fullstack4jRules;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ArchitectureTests {

	@Test
	void modulesOnlyUseEachOthersPublicApi() {
		ApplicationModules.of(DemoApplication.class).verify();
	}

	@Test
	void followsTheFullstack4jRules() {
		Fullstack4jRules.builder(DemoApplication.class)
			.build()
			.checkAll();
	}
}
