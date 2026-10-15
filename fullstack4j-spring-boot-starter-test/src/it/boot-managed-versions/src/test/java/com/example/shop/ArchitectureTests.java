package com.example.shop;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ArchitectureTests {

	@Test
	void modulesOnlyUseEachOthersPublicApi() {
		ApplicationModules.of(ShopApplication.class).verify();
	}
}
