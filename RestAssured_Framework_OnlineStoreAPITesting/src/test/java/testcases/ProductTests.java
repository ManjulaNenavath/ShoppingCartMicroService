package testcases;

import io.restassured.response.Response;
import org.testng.annotations.Test;
import payloads.Payload;
import pojo.Product;
import routes.ProductEndpoints;
import utils.DataProviders;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.assertEquals;

public class ProductTests extends BaseClass {

    private long productId;

    @Test(priority = 1, description = "List products -> 200 and non-empty (seeded)")
    public void listProducts() {
        Response response = ProductEndpoints.getAllProducts();
        response.then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("[0].title", notNullValue());
    }

    @Test(priority = 2, description = "Create product -> 201")
    public void createProduct() {
        Product product = Payload.newProduct();
        Response response = ProductEndpoints.createProduct(product);
        response.then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("title", equalTo(product.getTitle()));
        productId = response.jsonPath().getLong("id");
    }

    @Test(priority = 2, description = "Create product with invalid body -> 400")
    public void createInvalidProduct() {
        Product bad = new Product(null, new BigDecimal("-1"), "x", "y", null);
        Response response = ProductEndpoints.createProduct(bad);
        response.then().statusCode(400).body("status", equalTo(400));
    }

    @Test(priority = 3, description = "Get product by id -> 200")
    public void getProduct() {
        Response response = ProductEndpoints.getProduct(productId);
        response.then().statusCode(200).body("id", equalTo((int) productId));
    }

    @Test(priority = 3, description = "Get non-existent product -> 404")
    public void getProductNotFound() {
        Response response = ProductEndpoints.getProduct(999999);
        assertEquals(response.statusCode(), 404);
    }

    @Test(priority = 4, description = "Update product -> 200")
    public void updateProduct() {
        Product update = new Product("Updated Title", new BigDecimal("19.99"),
                "updated desc", "electronics", 10);
        Response response = ProductEndpoints.updateProduct(productId, update);
        response.then().statusCode(200)
                .body("title", equalTo("Updated Title"))
                .body("stockQuantity", equalTo(10));
    }

    @Test(priority = 5, description = "Delete product -> 204, then GET -> 404")
    public void deleteProduct() {
        assertEquals(ProductEndpoints.deleteProduct(productId).statusCode(), 204);
        assertEquals(ProductEndpoints.getProduct(productId).statusCode(), 404);
    }

    @Test(priority = 6, dataProvider = "products", dataProviderClass = DataProviders.class,
            description = "Data-driven create from testdata/Product.json -> 201")
    public void createProductsFromData(Product product) {
        Response response = ProductEndpoints.createProduct(product);
        response.then().statusCode(201).body("title", equalTo(product.getTitle()));
    }
}
