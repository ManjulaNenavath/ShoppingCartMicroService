package utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.DataProvider;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Data-driven testing WITHOUT POJOs.
 *
 * We read the JSON arrays under /testdata and hand each element to the test as an
 * org.json.JSONObject. The test sends product.toString() as the body and reads
 * fields (product.getString("title")) for assertions - no Java model class needed.
 * Adding a case is a pure data edit.
 */
public class DataProviders {

    @DataProvider(name = "products")
    public Object[][] products() throws Exception {
        return rows("testdata/Product.json");
    }

    @DataProvider(name = "users")
    public Object[][] users() throws Exception {
        return rows("testdata/User.json");
    }

    private Object[][] rows(String file) throws Exception {
        String content = Files.readString(Path.of(file));
        JSONArray array = new JSONArray(content);
        Object[][] rows = new Object[array.length()][1];
        for (int i = 0; i < array.length(); i++) {
            rows[i][0] = array.getJSONObject(i);
        }
        return rows;
    }
}
