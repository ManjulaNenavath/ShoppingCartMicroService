package payloads;

import com.github.javafaker.Faker;
import pojo.Cart;
import pojo.Product;
import pojo.User;

import java.math.BigDecimal;

/**
 * Central factory for request payloads.
 *
 * Using JavaFaker for random-but-valid data means every run uses a fresh username/
 * email, so re-running the suite never trips the "username already taken" (409)
 * rule. Deterministic tests that fight over the same fixture are a classic flaky-
 * test smell; randomized identities avoid it.
 */
public final class Payload {

    private static final Faker FAKER = new Faker();

    private Payload() {
    }

    public static User newUser() {
        String uniq = FAKER.regexify("[a-z]{8}") + FAKER.number().digits(4);
        return new User(
                "user_" + uniq,
                "Pass@" + FAKER.number().digits(4),
                uniq + "@example.com",
                FAKER.name().firstName(),
                FAKER.name().lastName(),
                FAKER.phoneNumber().subscriberNumber(7)
        );
    }

    public static Product newProduct() {
        return new Product(
                FAKER.commerce().productName(),
                new BigDecimal(FAKER.commerce().price(5.0, 500.0).replace(",", ".")),
                FAKER.lorem().sentence(),
                FAKER.commerce().department(),
                FAKER.number().numberBetween(1, 1000)
        );
    }

    public static Cart newCartItem(long productId) {
        return new Cart(
                productId,
                FAKER.number().numberBetween(1, 5),
                new BigDecimal(FAKER.commerce().price(5.0, 200.0).replace(",", "."))
        );
    }
}
