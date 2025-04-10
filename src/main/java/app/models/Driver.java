package app.models;

public class Driver extends User {
    public Driver(String username, String password) {
        super(username, password);
    }
    @Override
    public String getRole() {
        return "Driver";
    }
}
