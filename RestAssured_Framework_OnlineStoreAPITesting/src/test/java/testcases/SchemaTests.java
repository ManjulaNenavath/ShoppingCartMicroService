package testcases;

import org.testng.annotations.Test;
import payloads.Payload;
import routes.CartEndpoints;
import routes.ProductEndpoints;
import routes.UserEndpoints;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/**
 * Contract tests. These do NOT check business values - they check the SHAPE of the
 * response against a JSON Schema. If a developer renames a field or changes a type,
 * these fail loudly even when the status code is still 200.
 *
 * This is the cheapest early-warning system against accidental breaking changes to
 * an API contract, which is exactly what a senior tester wants guarding a service.
 */
public class SchemaTests extends BaseClass {

    @Test(description = "Register response matches UserSchema.json")
    public void userSchema() {
        UserEndpoints.registerUser(Payload.newUser())
                .then().assertThat().body(matchesJsonSchemaInClasspath("UserSchema.json"));
    }

    @Test(description = "Create-product response matches ProductSchema.json")
    public void productSchema() {
        ProductEndpoints.createProduct(Payload.newProduct())
                .then().assertThat().body(matchesJsonSchemaInClasspath("ProductSchema.json"));
    }

    @Test(description = "Cart response matches CartSchema.json")
    public void cartSchema() {
        long userId = System.nanoTime() % 100000;
        CartEndpoints.addItem(userId, Payload.newCartItem(1L))
                .then().assertThat().body(matchesJsonSchemaInClasspath("CartSchema.json"));
    }
}
