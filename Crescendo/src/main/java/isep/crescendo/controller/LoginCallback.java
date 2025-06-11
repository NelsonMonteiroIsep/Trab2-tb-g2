package isep.crescendo.controller;

public interface LoginCallback {
    void onLoginSuccess(boolean isAdmin);

    void onLoginFailure(String message);
}