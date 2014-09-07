package ai.autonumber;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import ai.autonumber.controller.ControllerManager;
import ai.autonumber.controller.MainController;
import ai.autonumber.gcm.GoogleCloudMessageActivity;
import ai.autonumber.model.CarMessage;
import ai.autonumber.model.ChatMessage;
import ai.autonumber.state.ActivitiStateHolder;


public class AutoNumberChatActivity extends GoogleCloudMessageActivity {
    private ControllerManager controllerManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auto_number_activiti);
        controllerManager = new ControllerManager(this);
    }


    @Override
    protected String getMainActivityClassName() {
        return AutoNumberChatActivity.class.getSimpleName();
    }


    @Override
    protected void handleChatMessage(ChatMessage message) {
        controllerManager.handleChatMessage(message, userName.equalsIgnoreCase(message.getUserName()));
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.camera_test, menu);
        return true;
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
        ActivitiStateHolder.activityResumed();
        final NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null)
            mNotificationManager.cancelAll();
        controllerManager.resumeControllers();
    }


    @Override
    protected void onPause() {
        super.onPause();
        ActivitiStateHolder.activityPaused();
        controllerManager.pauseControllers();
    }
}
