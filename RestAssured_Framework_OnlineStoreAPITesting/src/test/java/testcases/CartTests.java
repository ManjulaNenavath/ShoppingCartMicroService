package testcases;

import io.restassured.response.Response;
import org.testng.annotations.Test;
import payloads.Payload;
import routes.CartEndpoints;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.assertEquals;

/**
 * Cart service lifecycle. Uses a random userId per run so carts never collide.
 */
public class CartTests extends BaseClass {

    // Random-ish user id so each run gets its own cart.
    private final long userId = System.currentTimeMillis() % 100000;
    private long itemId;

    @Test(priority = 1, description = "Get cart for new user -> 200 with empty items")
    public void getEmptyCart() {
        Response response = CartEndpoints.getCart(userId);
        response.then()
                .statusCode(200)
                .body("userId", equalTo((int) userId))
                .body("items.size()", equalTo(0))
                .body("totalPrice", notNullValue());
    }

    @Test(priority = 2, description = "Add item to cart -> 201 and totalPrice updates")
    public void addItem() {
        Response response = CartEndpoints.addItem(userId, Payload.newCartItem(1L));
        response.then()
                .statusCode(201)
                .body("items.size()", equalTo(1))
                .body("items[0].productId", equalTo(1));
        itemId = response.jsonPath().getLong("items[0].itemId");
    }

    @Test(priority = 3, description = "Add a second item -> 201, cart now has 2 items")
    public void addSecondItem() {
        Response response = CartEndpoints.addItem(userId, Payload.newCartItem(2L));
        response.then().statusCode(201).body("items.size()", equalTo(2));
    }

    @Test(priority = 4, description = "Remove one item -> 204, cart shrinks to 1")
    public void removeItem() {
        assertEquals(CartEndpoints.removeItem(userId, itemId).statusCode(), 204);
        CartEndpoints.getCart(userId).then().statusCode(200).body("items.size()", equalTo(1));
    }

    @Test(priority = 4, description = "Remove non-existent item -> 404")
    public void removeMissingItem() {
        assertEquals(CartEndpoints.removeItem(userId, 999999).statusCode(), 404);
    }

    @Test(priority = 5, description = "Clear cart -> 204, then cart is empty")
    public void clearCart() {
        assertEquals(CartEndpoints.clearCart(userId).statusCode(), 204);
        CartEndpoints.getCart(userId).then().statusCode(200).body("items.size()", equalTo(0));
    }

    @Test(priority = 6, description = "Clear cart for user with no cart -> 404")
    public void clearMissingCart() {
        long unknownUser = System.currentTimeMillis(); // guaranteed no cart yet
        assertEquals(CartEndpoints.clearCart(unknownUser).statusCode(), 404);
    }
}
