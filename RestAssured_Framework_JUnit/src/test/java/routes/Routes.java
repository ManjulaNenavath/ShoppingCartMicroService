package routes;

import utils.ConfigReader;

/**
 * Single source of truth for base URLs and relative paths.
 *
 * If an endpoint path changes, you edit it HERE once - not in twenty test files.
 * That is the whole point of a routes layer.
 */
public final class Routes {

    private Routes() {
    }

    // Base URLs (resolved from Config.properties / -D overrides)
    public static final String USER_BASE = ConfigReader.get("user.baseUrl");
    public static final String PRODUCT_BASE = ConfigReader.get("product.baseUrl");
    public static final String CART_BASE = ConfigReader.get("cart.baseUrl");

    // User service paths
    public static final String USER_REGISTER = "/api/users/register";
    public static final String USER_LOGIN = "/api/users/login";
    public static final String USER_BY_ID = "/api/users/{id}";

    // Product service paths
    public static final String PRODUCTS = "/api/products";
    public static final String PRODUCT_BY_ID = "/api/products/{id}";

    // Cart service paths
    public static final String CART_BY_USER = "/api/cart/{userId}";
    public static final String CART_ITEMS = "/api/cart/{userId}/items";
    public static final String CART_ITEM_BY_ID = "/api/cart/{userId}/items/{itemId}";
}
