package routes;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * Static wrapper methods that hide the raw RestAssured.given() plumbing for the
 * User service. Tests read like plain English:  UserEndpoints.createUser(payload).
 *
 * Each method sets baseUri per-call so the three services can live on different
 * hosts/ports without a global RestAssured.baseURI fight.
 */
public final class UserEndpoints {

    private UserEndpoints() {
    }

    private static RequestSpecification base() {
        return given().baseUri(Routes.USER_BASE).header("Content-Type", "application/json");
    }

    public static Response registerUser(Object payload) {
        return base().body(payload).when().post(Routes.USER_REGISTER);
    }

    public static Response login(Object payload) {
        return base().body(payload).when().post(Routes.USER_LOGIN);
    }

    public static Response getUser(Object id) {
        return base().pathParam("id", id).when().get(Routes.USER_BY_ID);
    }

    public static Response updateUser(Object id, Object payload) {
        return base().pathParam("id", id).body(payload).when().put(Routes.USER_BY_ID);
    }

    public static Response deleteUser(Object id) {
        return base().pathParam("id", id).when().delete(Routes.USER_BY_ID);
    }
}
