package web;

public record UserRegistrationRequest(String name, String surname, String email,
                                      String username,
                                      String password, String repeatPassword) {
}
