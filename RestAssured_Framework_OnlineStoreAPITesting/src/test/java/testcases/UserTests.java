package testcases;

import io.restassured.response.Response;
import org.testng.annotations.Test;
import payloads.PayloadManager;
import routes.UserEndpoints;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * End-to-end coverage of the User service - using FILE-BASED payloads.
 *
 * Bodies come from src/test/resources/payloads/*.json via PayloadManager. We keep
 * the generated data map (userData) on the instance so register, login and update
 * all render from the same values - e.g. login reuses the exact username/password
 * we registered with.
 */
public class UserTests extends BaseClass {

    private final Map<String, Object> userData = PayloadManager.randomUserData();
    private long userId;

    @Test(priority = 1, description = "Register a new user -> 201 Created")
    public void registerUser() {
        String payload = PayloadManager.build("registerUser.json", userData);
        Response response = UserEndpoints.registerUser(payload);
        response.then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("username", equalTo(userData.get("username")))
                .body("email", equalTo(userData.get("email")));
        userId = response.jsonPath().getLong("id");
        assertNotNull(userId);
    }

    @Test(priority = 2, description = "Registering the same username again -> 409 Conflict")
    public void registerDuplicate() {
        String payload = PayloadManager.build("registerUser.json", userData);
        Response response = UserEndpoints.registerUser(payload);
        assertEquals(response.statusCode(), 409);
    }

    @Test(priority = 2, description = "Register with invalid body -> 400 Bad Request")
    public void registerInvalid() {
        // Build straight from a template with deliberately bad values.
        String payload = PayloadManager.build("registerUser.json", Map.of(
                "username", "ab",          // too short
                "password", "",            // blank
                "email", "not-an-email",   // invalid
                "firstName", "x",
                "lastName", "y",
                "phone", "0"
        ));
        Response response = UserEndpoints.registerUser(payload);
        response.then().statusCode(400).body("status", equalTo(400));
    }

    @Test(priority = 3, description = "Login with correct credentials -> 200 + token")
    public void loginSuccess() {
        String payload = PayloadManager.build("loginUser.json", userData);
        Response response = UserEndpoints.login(payload);
        response.then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("message", equalTo("login successful"));
    }

    @Test(priority = 3, description = "Login with wrong password -> 401 Unauthorized")
    public void loginWrongPassword() {
        String payload = PayloadManager.build("loginUser.json", Map.of(
                "username", userData.get("username"),
                "password", "wrong-pass"
        ));
        Response response = UserEndpoints.login(payload);
        assertEquals(response.statusCode(), 401);
    }

    @Test(priority = 4, description = "Fetch existing user by id -> 200")
    public void getUser() {
        Response response = UserEndpoints.getUser(userId);
        response.then().statusCode(200).body("id", equalTo((int) userId));
    }

    @Test(priority = 4, description = "Fetch non-existent user -> 404")
    public void getUserNotFound() {
        Response response = UserEndpoints.getUser(999999);
        assertEquals(response.statusCode(), 404);
    }

    @Test(priority = 5, description = "Update user profile -> 200")
    public void updateUser() {
        // Re-render the register template with one changed field.
        userData.put("firstName", "Updated");
        userData.put("phone", "999-0000");
        String payload = PayloadManager.build("registerUser.json", userData);
        Response response = UserEndpoints.updateUser(userId, payload);
        response.then().statusCode(200).body("firstName", equalTo("Updated"));
    }

    @Test(priority = 6, description = "Delete user -> 204, then GET -> 404")
    public void deleteUser() {
        Response del = UserEndpoints.deleteUser(userId);
        assertEquals(del.statusCode(), 204);
        Response afterDelete = UserEndpoints.getUser(userId);
        assertEquals(afterDelete.statusCode(), 404);
    }
}
