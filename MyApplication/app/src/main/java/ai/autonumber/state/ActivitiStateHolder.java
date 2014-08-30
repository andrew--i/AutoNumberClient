package ai.autonumber.state;

/**
 * Created by Andrew on 30.08.2014.
 */
public class ActivitiStateHolder {
    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    private static boolean activityVisible;
}
