package com.example.shop.billing.internal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public record Invoice(@Id String id, String orderId) {
}
