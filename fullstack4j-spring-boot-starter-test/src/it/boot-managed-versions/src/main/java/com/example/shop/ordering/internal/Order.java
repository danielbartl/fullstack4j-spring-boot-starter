package com.example.shop.ordering.internal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public record Order(@Id String id, String customer, int amount) {
}
