package com.example.ministore.data

import com.example.ministore.model.Product

object DummyData {
    val categories = listOf("All", "Tech", "Audio", "Wearables", "Accessories")

    val sampleProducts = listOf(
        Product(
            id = 1,
            name = "Wireless Noise-Canceling Headphones",
            category = "Audio",
            price = 99.99,
            rating = 4.8,
            description = "Premium over-ear headphones with active noise cancellation, deep bass, and up to 40 hours of battery life.",
            iconEmoji = "🎧"
        ),
        Product(
            id = 2,
            name = "Smart Fitness Watch V2",
            category = "Wearables",
            price = 149.50,
            rating = 4.7,
            description = "Track workouts, heart rate, sleep metrics, and notifications with a crystal-clear AMOLED display.",
            iconEmoji = "⌚"
        ),
        Product(
            id = 3,
            name = "Mechanical Gaming Keyboard",
            category = "Tech",
            price = 79.99,
            rating = 4.9,
            description = "Compact 75% mechanical keyboard with smooth linear switches and customizable RGB backlighting.",
            iconEmoji = "⌨️"
        ),
        Product(
            id = 4,
            name = "Ergonomic Wireless Mouse",
            category = "Tech",
            price = 39.99,
            rating = 4.6,
            description = "Comfortable natural handshake grip designed to reduce wrist fatigue with ultra-precise optical tracking.",
            iconEmoji = "🖱️"
        ),
        Product(
            id = 5,
            name = "Portable Bluetooth Speaker",
            category = "Audio",
            price = 49.00,
            rating = 4.5,
            description = "Compact 360-degree sound speaker, waterproof rating IPX7, perfect for outdoor travel and home.",
            iconEmoji = "🔊"
        ),
        Product(
            id = 6,
            name = "Fast USB-C 65W GaN Charger",
            category = "Accessories",
            price = 29.99,
            rating = 4.9,
            description = "Pocket-sized ultra-fast GaN wall charger supporting laptops, phones, and tablets with multi-port charging.",
            iconEmoji = "⚡"
        )
    )
}
