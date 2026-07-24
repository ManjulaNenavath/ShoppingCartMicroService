package testcases;

import base.BaseTest;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import payloads.PayloadManager;
import routes.ProductEndpoints;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductTests extends BaseTest {

    private long productId;

    @Test
    @Order(1)
    void listProducts() {
        ProductEndpoints.getAllProducts().then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("[0].title", notNullValue());
    }

    @Test
    @Order(2)
    void createProduct() {
        String payload = PayloadManager.randomProduct();
        String expectedTitle = new JSONObject(payload).getString("title");
        Response response = ProductEndpoints.createProduct(payload);
        response.then().statusCode(201).body("id", notNullValue()).body("title", equalTo(expectedTitle));
        productId = response.jsonPath().getLong("id");
    }

    @Test
    @Order(3)
    void createInvalidProduct() {
        String payload = PayloadManager.build("createProduct.json", Map.of(
                "title", "", "price", "-1", "description", "x", "category", "y", "stockQuantity", "null"
        ));
        ProductEndpoints.createProduct(payload).then().statusCode(400).body("status", equalTo(400));
    }

    @Test
    @Order(4)
    void getProduct() {
        ProductEndpoints.getProduct(productId).then().statusCode(200).body("id", equalTo((int) productId));
    }

    @Test
    @Order(5)
    void getProductNotFound() {
        assertEquals(404, ProductEndpoints.getProduct(999999).statusCode());
    }

    @Test
    @Order(6)
    void updateProduct() {
        String payload = PayloadManager.build("createProduct.json", Map.of(
                "title", "Updated Title", "price", "19.99", "description", "updated desc",
                "category", "electronics", "stockQuantity", "10"
        ));
        ProductEndpoints.updateProduct(productId, payload).then()
                .statusCode(200)
                .body("title", equalTo("Updated Title"))
                .body("stockQuantity", equalTo(10));
    }

    @Test
    @Order(7)
    void deleteProduct() {
        assertEquals(204, ProductEndpoints.deleteProduct(productId).statusCode());
        assertEquals(404, ProductEndpoints.getProduct(productId).statusCode());
    }

    // ---- Data-driven (JUnit @ParameterizedTest + @MethodSource replaces @DataProvider) ----

    @ParameterizedTest(name = "create from data: {0}")
    @MethodSource("productData")
    @Order(8)
    void createProductsFromData(JSONObject product) {
        ProductEndpoints.createProduct(product.toString()).then()
                .statusCode(201)
                .body("title", equalTo(product.getString("title")));
    }

    // Non-static is allowed because BaseTest sets PER_CLASS lifecycle.
    Stream<JSONObject> productData() throws Exception {
        String content = Files.readString(Path.of("testdata/Product.json"));
        JSONArray array = new JSONArray(content);
        List<JSONObject> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            list.add(array.getJSONObject(i));
        }
        return list.stream();
    }
}
