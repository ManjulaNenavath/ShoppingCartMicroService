package testcases;

import io.restassured.response.Response;
import org.testng.annotations.Test;
import payloads.Payload;
import pojo.User;
import routes.UserEndpoints;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * End-to-end coverage of the User service.
 *
 * Test design notes:
 *  - Each run creates a fresh random user (Payload.newUser) so tests are repeatable.
 *  - We cover the full lifecycle (register -> login -> get -> update -> delete)
 *    AND the negative branches (duplicate=409, bad input=400, bad creds=401,
 *    missing id=404). Positive-only suites give false confidence.
 *  - priority controls order within the class; the created user's id is shared
 *    through an instance field so later tests operate on a known record.
 */
public class UserTests extends BaseClass {

    private final User user = Payload.newUser();
    private long userId;

    @Test(priority = 1, description = "Register a new user -> 201 Created")
    public void registerUser() {
        Response response = UserEndpoints.registerUser(user);
        response.then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("username", equalTo(user.getUsername()))
                .body("email", equalTo(user.getEmail()));
        userId = response.jsonPath().getLong("id");
        assertNotNull(userId);
    }

    @Test(priority = 2, description = "Registering the same username again -> 409 Conflict")
    public void registerDuplicate() {
        Response response = UserEndpoints.registerUser(user);
        assertEquals(response.statusCode(), 409);
    }

    @Test(priority = 2, description = "Register with invalid body -> 400 Bad Request")
    public void registerInvalid() {
        User bad = new User("ab", "", "not-an-email", null, null, null);
        Response response = UserEndpoints.registerUser(bad);
        response.then().statusCode(400).body("status", equalTo(400));
    }

    @Test(priority = 3, description = "Login with correct credentials -> 200 + token")
    public void loginSuccess() {
        String body = "{\"username\":\"" + user.getUsername() + "\",\"password\":\"" + user.getPassword() + "\"}";
        Response response = UserEndpoints.login(body);
        response.then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("message", equalTo("login successful"));
    }

    @Test(priority = 3, description = "Login with wrong password -> 401 Unauthorized")
    public void loginWrongPassword() {
        String body = "{\"username\":\"" + user.getUsername() + "\",\"password\":\"wrong-pass\"}";
        Response response = UserEndpoints.login(body);
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
        user.setFirstName("Updated");
        user.setPhone("999-0000");
        Response response = UserEndpoints.updateUser(userId, user);
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
