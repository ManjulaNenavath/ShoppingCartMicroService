package routes;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public final class ProductEndpoints {

    private ProductEndpoints() {
    }

    private static RequestSpecification base() {
        return given().baseUri(Routes.PRODUCT_BASE).header("Content-Type", "application/json");
    }

    public static Response getAllProducts() {
        return base().when().get(Routes.PRODUCTS);
    }

    public static Response getProduct(Object id) {
        return base().pathParam("id", id).when().get(Routes.PRODUCT_BY_ID);
    }

    public static Response createProduct(Object payload) {
        return base().body(payload).when().post(Routes.PRODUCTS);
    }

    public static Response updateProduct(Object id, Object payload) {
        return base().pathParam("id", id).body(payload).when().put(Routes.PRODUCT_BY_ID);
    }

    public static Response deleteProduct(Object id) {
        return base().pathParam("id", id).when().delete(Routes.PRODUCT_BY_ID);
    }
}
