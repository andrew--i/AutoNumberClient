package ai.autonumber.activiti;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import ai.autonumber.R;
import ai.autonumber.controller.ControllerManager;
import ai.autonumber.controller.MainController;
import ai.autonumber.gcm.GoogleCloudMessageActivity;
import ai.autonumber.model.CarMessage;
import ai.autonumber.model.User;
import ai.autonumber.state.AppStateHolder;


public class AutoNumberChatActivity extends GoogleCloudMessageActivity {
    private ControllerManager controllerManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auto_number_activiti);
        controllerManager = new ControllerManager(this);
        controllerManager.setMainControllerActive();
    }


    @Override
    protected String getMainActivityClassName() {
        return AutoNumberChatActivity.class.getSimpleName();
    }


    @Override
    protected void handleChatMessageId(String messageId) {
        controllerManager.handleChatMessageId(messageId);
    }

    @Override
    protected void handleNewCarMessage(CarMessage carMessage) {
        controllerManager.handleNewCarMessage(carMessage);
    }

    @Override
    protected void handleLastCarMessage(CarMessage carMessage) {
        controllerManager.handleLastCarMessage(carMessage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.autonumbermenu, menu);
        return true;
    }

    @Override
    protected void handleChangeCurrentUser() {
        controllerManager.handleChangeCurrentUser();
    }

    @Override
    protected void handleChangeUserInfo(User user) {
        controllerManager.handleChangeUserInfo(user);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.userMenuItem:
                startActivity(new Intent(this, UserMenuActivity.class));
                return true;
            case R.id.photosMenuItem:
                startActivity(new Intent(this, PhotosListActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainController.PHOTO_INTENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                controllerManager.sendImageToServer();
                controllerManager.setMainControllerActive();
            } else if (resultCode == RESULT_CANCELED) {
                controllerManager.setMainControllerActive();
                Toast.makeText(this, "Снимок отменен", Toast.LENGTH_LONG).show();
            } else {
                controllerManager.setMainControllerActive();
                Toast.makeText(this, "Ошибка при получении снимка", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppStateHolder.mainActivityResumed();
        final NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null)
            mNotificationManager.cancelAll();
        controllerManager.resumeControllers();
    }


    @Override
    protected void onPause() {
        super.onPause();
        AppStateHolder.mainActivityPaused();
        controllerManager.pauseControllers();
    }
}
