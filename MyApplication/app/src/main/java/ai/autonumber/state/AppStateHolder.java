package ai.autonumber.state;


import ai.autonumber.model.User;

public class AppStateHolder {
    public static boolean isMainActivityVisible() {
        return activityVisible;
    }

    public static void mainActivityResumed() {
        activityVisible = true;
    }

    public static void mainActivityPaused() {
        activityVisible = false;
    }

    private static boolean activityVisible;

    public static User currentUser;
}
