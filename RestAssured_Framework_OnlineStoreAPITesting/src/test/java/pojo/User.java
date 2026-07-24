package pojo;

/**
 * POJO mirroring the User request payload.
 *
 * Why POJOs instead of hand-built JSON strings? REST Assured + Jackson will
 * serialize this object to JSON for you (request body) and deserialize responses
 * back into it. That gives you compile-time safety: rename a field and the tests
 * that use it stop compiling, instead of silently sending the wrong key.
 */
public class User {

    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;

    public User() {
    }

    public User(String username, String password, String email,
                String firstName, String lastName, String phone) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
