package testcases;

import org.testng.annotations.Test;
import payloads.PayloadManager;
import routes.CartEndpoints;
import routes.ProductEndpoints;
import routes.UserEndpoints;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/**
 * Contract tests. These do NOT check business values - they check the SHAPE of the
 * response against a JSON Schema. If a developer renames a field or changes a type,
 * these fail loudly even when the status code is still 200.
 *
 * Payloads are rendered from the same file templates the other tests use.
 */
public class SchemaTests extends BaseClass {

    @Test(description = "Register response matches UserSchema.json")
    public void userSchema() {
        String payload = PayloadManager.build("registerUser.json", PayloadManager.randomUserData());
        UserEndpoints.registerUser(payload)
                .then().assertThat().body(matchesJsonSchemaInClasspath("UserSchema.json"));
    }

    @Test(description = "Create-product response matches ProductSchema.json")
    public void productSchema() {
        ProductEndpoints.createProduct(PayloadManager.randomProduct())
                .then().assertThat().body(matchesJsonSchemaInClasspath("ProductSchema.json"));
    }

    @Test(description = "Cart response matches CartSchema.json")
    public void cartSchema() {
        long userId = System.nanoTime() % 100000;
        CartEndpoints.addItem(userId, PayloadManager.randomCartItem(1L))
                .then().assertThat().body(matchesJsonSchemaInClasspath("CartSchema.json"));
    }
}
