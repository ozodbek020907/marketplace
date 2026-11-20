package com.example.ministore

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.ministore.model.CartItem
import com.example.ministore.model.Product
import com.example.ministore.model.User
import com.example.ministore.ui.screens.CartScreen
import com.example.ministore.ui.screens.LoginLogsScreen
import com.example.ministore.ui.screens.LoginScreen
import com.example.ministore.ui.screens.RegisterScreen
import com.example.ministore.ui.screens.RegisteredUsersScreen
import com.example.ministore.ui.screens.StoreHomeScreen
import com.example.ministore.ui.theme.MiniStoreTheme

enum class Screen {
    LOGIN,
    REGISTER,
    HOME,
    CART,
    USERS_LIST,
    LOGS_LIST
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiniStoreTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MiniShopApp()
                }
            }
        }
    }
}

@Composable
fun MiniShopApp() {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
    var previousScreen by remember { mutableStateOf(Screen.LOGIN) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    val cartItems = remember { mutableStateListOf<CartItem>() }

    fun navigateTo(screen: Screen) {
        previousScreen = currentScreen
        currentScreen = screen
    }

    fun addToCart(product: Product) {
        val existingIndex = cartItems.indexOfFirst { it.product.id == product.id }
        if (existingIndex != -1) {
            val existing = cartItems[existingIndex]
            cartItems[existingIndex] = existing.copy(quantity = existing.quantity + 1)
        } else {
            cartItems.add(CartItem(product = product, quantity = 1))
        }
        Toast.makeText(context, "${product.name} savatchaga qo'shildi!", Toast.LENGTH_SHORT).show()
    }

    fun increaseQuantity(item: CartItem) {
        val index = cartItems.indexOfFirst { it.product.id == item.product.id }
        if (index != -1) {
            cartItems[index] = item.copy(quantity = item.quantity + 1)
        }
    }

    fun decreaseQuantity(item: CartItem) {
        val index = cartItems.indexOfFirst { it.product.id == item.product.id }
        if (index != -1) {
            if (item.quantity > 1) {
                cartItems[index] = item.copy(quantity = item.quantity - 1)
            } else {
                cartItems.removeAt(index)
            }
        }
    }

    Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
        when (screen) {
            Screen.LOGIN -> {
                LoginScreen(
                    onLoginSuccess = { signedUser ->
                        currentUser = User(
                            id = signedUser.id,
                            username = signedUser.username,
                            login = signedUser.login
                        )
                        navigateTo(Screen.HOME)
                        Toast.makeText(context, "Xush kelibsiz, ${signedUser.username}!", Toast.LENGTH_SHORT).show()
                    },
                    onNavigateToRegister = {
                        navigateTo(Screen.REGISTER)
                    },
                    onViewUsers = {
                        navigateTo(Screen.USERS_LIST)
                    },
                    onViewLogs = {
                        navigateTo(Screen.LOGS_LIST)
                    }
                )
            }
            Screen.REGISTER -> {
                RegisterScreen(
                    onRegisterSuccess = { signedUser ->
                        currentUser = User(
                            id = signedUser.id,
                            username = signedUser.username,
                            login = signedUser.login
                        )
                        navigateTo(Screen.HOME)
                        Toast.makeText(context, "Ro'yxatdan muvaffaqiyatli o'tdingiz, ${signedUser.username}!", Toast.LENGTH_LONG).show()
                    },
                    onNavigateToLogin = {
                        navigateTo(Screen.LOGIN)
                    },
                    onViewUsers = {
                        navigateTo(Screen.USERS_LIST)
                    },
                    onViewLogs = {
                        navigateTo(Screen.LOGS_LIST)
                    }
                )
            }
            Screen.HOME -> {
                val user = currentUser
                if (user != null) {
                    StoreHomeScreen(
                        user = user,
                        cartItems = cartItems,
                        onAddToCart = { product ->
                            addToCart(product)
                        },
                        onViewCart = {
                            navigateTo(Screen.CART)
                        },
                        onViewUsers = {
                            navigateTo(Screen.USERS_LIST)
                        },
                        onViewLogs = {
                            navigateTo(Screen.LOGS_LIST)
                        },
                        onLogout = {
                            currentUser = null
                            cartItems.clear()
                            navigateTo(Screen.LOGIN)
                            Toast.makeText(context, "Tizimdan muvaffaqiyatli chiqildi", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    navigateTo(Screen.LOGIN)
                }
            }
            Screen.CART -> {
                CartScreen(
                    cartItems = cartItems,
                    onIncreaseQuantity = { item -> increaseQuantity(item) },
                    onDecreaseQuantity = { item -> decreaseQuantity(item) },
                    onClearCart = { cartItems.clear() },
                    onBack = { navigateTo(Screen.HOME) }
                )
            }
            Screen.USERS_LIST -> {
                RegisteredUsersScreen(
                    onBack = { navigateTo(previousScreen) }
                )
            }
            Screen.LOGS_LIST -> {
                LoginLogsScreen(
                    onBack = { navigateTo(previousScreen) }
                )
            }
        }
    }
}
