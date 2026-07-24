package payloads;

import com.github.javafaker.Faker;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * File-based payload strategy (instead of POJO serialization).
 *
 * Request bodies live as JSON templates under src/test/resources/payloads/ with
 * {{placeholder}} tokens. This class:
 *   1. reads a template off the classpath, and
 *   2. substitutes the tokens with values, returning a ready-to-send JSON String.
 *
 * Why templates + placeholders (not plain static JSON)? A hardcoded username would
 * hit "409 already taken" on the second run. Filling {{username}} with a unique
 * value each run keeps the suite repeatable - the same reason the old POJO factory
 * used random data.
 *
 * Why keep the values in a Map you pass in? Because a test often needs to REUSE a
 * value later (e.g. register then log in with the same username/password). The test
 * owns the data; this class just renders it into the template.
 */
public final class PayloadManager {

    private static final Faker FAKER = new Faker();

    private PayloadManager() {
    }

    /** Reads a raw template (with {{tokens}} intact) from src/test/resources/payloads/. */
    public static String read(String fileName) {
        String path = "payloads/" + fileName;
        try (InputStream in = PayloadManager.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Payload template not found on classpath: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read payload template: " + path, e);
        }
    }

    /** Reads a template and replaces every {{key}} with the matching value. */
    public static String build(String fileName, Map<String, Object> values) {
        String content = read(fileName);
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            content = content.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        }
        return content;
    }

    // ---- Convenience: generate unique data maps / ready payloads ----

    /**
     * Returns a fresh, unique user data map. Return the MAP (not just the JSON) so a
     * test can build the register body AND reuse username/password for the login body.
     */
    public static Map<String, Object> randomUserData() {
        String suffix = FAKER.regexify("[a-z]{6}") + FAKER.number().digits(4);
        Map<String, Object> data = new HashMap<>();
        data.put("username", "user_" + suffix);
        data.put("password", "Pass@" + FAKER.number().digits(4));
        data.put("email", suffix + "@example.com");
        data.put("firstName", FAKER.name().firstName());
        data.put("lastName", FAKER.name().lastName());
        data.put("phone", FAKER.phoneNumber().subscriberNumber(7));
        return data;
    }

    /** Ready-to-send create-product body with random values. */
    public static String randomProduct() {
        Map<String, Object> data = new HashMap<>();
        data.put("title", clean(FAKER.commerce().productName()));
        data.put("price", FAKER.commerce().price(5.0, 500.0).replace(",", "."));
        data.put("description", clean(FAKER.lorem().sentence()));
        data.put("category", "electronics");
        data.put("stockQuantity", FAKER.number().numberBetween(1, 1000));
        return build("createProduct.json", data);
    }

    /** Ready-to-send add-to-cart body for a given product id. */
    public static String randomCartItem(long productId) {
        Map<String, Object> data = new HashMap<>();
        data.put("productId", productId);
        data.put("quantity", FAKER.number().numberBetween(1, 5));
        data.put("price", FAKER.commerce().price(5.0, 200.0).replace(",", "."));
        return build("addCartItem.json", data);
    }

    /** Strip characters that would break a raw JSON string value. */
    private static String clean(String s) {
        return s.replace("\"", "").replace("\\", "");
    }
}
