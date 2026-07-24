package routes;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public final class CartEndpoints {

    private CartEndpoints() {
    }

    private static RequestSpecification base() {
        return given().baseUri(Routes.CART_BASE).header("Content-Type", "application/json");
    }

    public static Response getCart(Object userId) {
        return base().pathParam("userId", userId).when().get(Routes.CART_BY_USER);
    }

    public static Response addItem(Object userId, Object payload) {
        return base().pathParam("userId", userId).body(payload).when().post(Routes.CART_ITEMS);
    }

    public static Response removeItem(Object userId, Object itemId) {
        return base().pathParam("userId", userId).pathParam("itemId", itemId)
                .when().delete(Routes.CART_ITEM_BY_ID);
    }

    public static Response clearCart(Object userId) {
        return base().pathParam("userId", userId).when().delete(Routes.CART_BY_USER);
    }
}
