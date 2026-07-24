package utils;

import com.google.gson.Gson;
import org.testng.annotations.DataProvider;
import pojo.Product;
import pojo.User;

import java.io.FileReader;
import java.io.Reader;

/**
 * Data-driven testing: feed multiple rows into one @Test method.
 *
 * The JSON files under /testdata are read once and turned into POJO arrays. Each
 * element becomes one invocation of the test, so adding a case is a data edit,
 * not a code change.
 */
public class DataProviders {

    private static final Gson GSON = new Gson();

    @DataProvider(name = "products")
    public Object[][] products() throws Exception {
        try (Reader reader = new FileReader("testdata/Product.json")) {
            Product[] products = GSON.fromJson(reader, Product[].class);
            Object[][] rows = new Object[products.length][1];
            for (int i = 0; i < products.length; i++) {
                rows[i][0] = products[i];
            }
            return rows;
        }
    }

    @DataProvider(name = "users")
    public Object[][] users() throws Exception {
        try (Reader reader = new FileReader("testdata/User.json")) {
            User[] users = GSON.fromJson(reader, User[].class);
            Object[][] rows = new Object[users.length][1];
            for (int i = 0; i < users.length; i++) {
                rows[i][0] = users[i];
            }
            return rows;
        }
    }
}
