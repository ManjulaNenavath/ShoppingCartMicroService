package testcases;

import base.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import payloads.PayloadManager;
import routes.CartEndpoints;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CartTests extends BaseTest {

    private final long userId = System.currentTimeMillis() % 100000;
    private long itemId;

    @Test
    @Order(1)
    void getEmptyCart() {
        CartEndpoints.getCart(userId).then()
                .statusCode(200)
                .body("userId", equalTo((int) userId))
                .body("items.size()", equalTo(0))
                .body("totalPrice", notNullValue());
    }

    @Test
    @Order(2)
    void addItem() {
        Response response = CartEndpoints.addItem(userId, PayloadManager.randomCartItem(1L));
        response.then().statusCode(201).body("items.size()", equalTo(1)).body("items[0].productId", equalTo(1));
        itemId = response.jsonPath().getLong("items[0].itemId");
    }

    @Test
    @Order(3)
    void addSecondItem() {
        CartEndpoints.addItem(userId, PayloadManager.randomCartItem(2L)).then()
                .statusCode(201).body("items.size()", equalTo(2));
    }

    @Test
    @Order(4)
    void removeItem() {
        assertEquals(204, CartEndpoints.removeItem(userId, itemId).statusCode());
        CartEndpoints.getCart(userId).then().statusCode(200).body("items.size()", equalTo(1));
    }

    @Test
    @Order(5)
    void removeMissingItem() {
        assertEquals(404, CartEndpoints.removeItem(userId, 999999).statusCode());
    }

    @Test
    @Order(6)
    void clearCart() {
        assertEquals(204, CartEndpoints.clearCart(userId).statusCode());
        CartEndpoints.getCart(userId).then().statusCode(200).body("items.size()", equalTo(0));
    }

    @Test
    @Order(7)
    void clearMissingCart() {
        long unknownUser = System.currentTimeMillis(); // guaranteed no cart yet
        assertEquals(404, CartEndpoints.clearCart(unknownUser).statusCode());
    }
}
