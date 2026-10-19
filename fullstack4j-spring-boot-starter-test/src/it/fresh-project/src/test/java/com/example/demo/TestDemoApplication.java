package com.example.demo;

import dev.jbaby.fullstack4j.springboot.test.MongoDbReplicaSetConfiguration;
import org.springframework.boot.SpringApplication;

public class TestDemoApplication {

	public static void main(String[] args) {
		SpringApplication.from(DemoApplication::main).with(MongoDbReplicaSetConfiguration.class).run(args);
	}
}
