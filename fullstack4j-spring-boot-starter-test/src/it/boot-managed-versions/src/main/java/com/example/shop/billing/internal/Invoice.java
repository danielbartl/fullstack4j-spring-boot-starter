package com.example.shop.billing.internal;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public record Invoice(@Id String id, String customer, int amount) implements Serializable {
}
