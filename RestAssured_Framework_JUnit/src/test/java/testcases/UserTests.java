package testcases;

import base.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import payloads.PayloadManager;
import routes.UserEndpoints;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * User service lifecycle + negative paths, JUnit 5 style.
 *
 * @TestMethodOrder + @Order replace TestNG's priority attribute. The chained
 * userId/userData instance state survives across methods thanks to PER_CLASS
 * (inherited from BaseTest).
 *
 * NOTE the assertion order flips vs TestNG: JUnit is assertEquals(EXPECTED, ACTUAL).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserTests extends BaseTest {

    private final Map<String, Object> userData = PayloadManager.randomUserData();
    private long userId;

    @Test
    @Order(1)
    void registerUser() {
        String payload = PayloadManager.build("registerUser.json", userData);
        Response response = UserEndpoints.registerUser(payload);
        response.then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("username", equalTo(userData.get("username")))
                .body("email", equalTo(userData.get("email")));
        userId = response.jsonPath().getLong("id");
        assertTrue(userId > 0);
    }

    @Test
    @Order(2)
    void registerDuplicate() {
        String payload = PayloadManager.build("registerUser.json", userData);
        assertEquals(409, UserEndpoints.registerUser(payload).statusCode());
    }

    @Test
    @Order(3)
    void registerInvalid() {
        String payload = PayloadManager.build("registerUser.json", Map.of(
                "username", "ab", "password", "", "email", "not-an-email",
                "firstName", "x", "lastName", "y", "phone", "0"
        ));
        UserEndpoints.registerUser(payload).then().statusCode(400).body("status", equalTo(400));
    }

    @Test
    @Order(4)
    void loginSuccess() {
        String payload = PayloadManager.build("loginUser.json", userData);
        UserEndpoints.login(payload).then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("message", equalTo("login successful"));
    }

    @Test
    @Order(5)
    void loginWrongPassword() {
        String payload = PayloadManager.build("loginUser.json", Map.of(
                "username", userData.get("username"), "password", "wrong-pass"
        ));
        assertEquals(401, UserEndpoints.login(payload).statusCode());
    }

    @Test
    @Order(6)
    void getUser() {
        UserEndpoints.getUser(userId).then().statusCode(200).body("id", equalTo((int) userId));
    }

    @Test
    @Order(7)
    void getUserNotFound() {
        assertEquals(404, UserEndpoints.getUser(999999).statusCode());
    }

    @Test
    @Order(8)
    void updateUser() {
        userData.put("firstName", "Updated");
        userData.put("phone", "999-0000");
        String payload = PayloadManager.build("registerUser.json", userData);
        UserEndpoints.updateUser(userId, payload).then().statusCode(200).body("firstName", equalTo("Updated"));
    }

    @Test
    @Order(9)
    void deleteUser() {
        assertEquals(204, UserEndpoints.deleteUser(userId).statusCode());
        assertEquals(404, UserEndpoints.getUser(userId).statusCode());
    }
}
