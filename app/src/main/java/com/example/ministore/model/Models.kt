package com.example.ministore.model

import com.google.gson.annotations.SerializedName

data class Product(
    val id: Int,
    val name: String,
    val category: String,
    val price: Double,
    val rating: Double,
    val description: String,
    val iconEmoji: String
)

data class CartItem(
    val product: Product,
    var quantity: Int = 1
)

data class User(
    val id: Long? = null,
    val username: String,
    val login: String
)

data class SignUpRecord(
    val id: Long? = null,
    val username: String,
    val login: String,
    val password: String,
    @SerializedName("created_at")
    val createdAt: String? = null
)

data class LoginRecord(
    val id: Long? = null,
    val login: String,
    val password: String,
    val status: String,
    @SerializedName("created_at")
    val createdAt: String? = null
)
