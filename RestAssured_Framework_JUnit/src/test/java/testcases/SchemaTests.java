package testcases;

import base.BaseTest;
import org.junit.jupiter.api.Test;
import payloads.PayloadManager;
import routes.CartEndpoints;
import routes.ProductEndpoints;
import routes.UserEndpoints;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/**
 * Contract (schema) tests - identical intent to the TestNG version. Validates the
 * SHAPE of responses against JSON Schemas, independent of business values.
 */
class SchemaTests extends BaseTest {

    @Test
    void userSchema() {
        String payload = PayloadManager.build("registerUser.json", PayloadManager.randomUserData());
        UserEndpoints.registerUser(payload).then().assertThat()
                .body(matchesJsonSchemaInClasspath("UserSchema.json"));
    }

    @Test
    void productSchema() {
        ProductEndpoints.createProduct(PayloadManager.randomProduct()).then().assertThat()
                .body(matchesJsonSchemaInClasspath("ProductSchema.json"));
    }

    @Test
    void cartSchema() {
        long userId = System.nanoTime() % 100000;
        CartEndpoints.addItem(userId, PayloadManager.randomCartItem(1L)).then().assertThat()
                .body(matchesJsonSchemaInClasspath("CartSchema.json"));
    }
}
