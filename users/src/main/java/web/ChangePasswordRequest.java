package web;

public record ChangePasswordRequest(String currentPassword, String newPassword1,
                                    String newPassword2) {

}
