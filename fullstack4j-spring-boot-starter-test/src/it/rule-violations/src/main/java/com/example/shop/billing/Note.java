package com.example.shop.billing;

import org.springframework.data.mongodb.core.mapping.Document;

// a document in the slice's public API
@Document
public record Note(String id, String text) {
}
