package api;

public interface UsersSubSystem {

    String login(String username, String password);

    Long userIdFrom(String token);

    void changePassword(Long userId, String currentPassword,
                        String newPassword1,
                        String newPassword2);

    UserProfile profileFrom(Long userId);

    Long registerUser(String name, String surname, String email,
                      String userName,
                      String password, String repeatPassword);
}
