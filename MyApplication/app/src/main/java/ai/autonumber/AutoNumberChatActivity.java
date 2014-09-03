package ai.autonumber;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import ai.autonumber.controller.ControllerManager;
import ai.autonumber.controller.MainController;
import ai.autonumber.gcm.GoogleCloudMessageActivity;
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
    protected void handleChatMessage(String message) {
        ChatMessage chatMessage = ChatMessage.fromJson(message);
        if (chatMessage != null)
            controllerManager.handleChatMessage(chatMessage, userName.equalsIgnoreCase(chatMessage.getUserName()));
    }

    @Override
    protected void handleSearchCarNumber(String searchCarNumber) {

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
                controllerManager.setCamControllerActive();
            } else if (resultCode == RESULT_CANCELED)
                Toast.makeText(this, "Capture cancelled", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, "Capture failed", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActivitiStateHolder.activityResumed();
        controllerManager.resumeControllers();
    }


    @Override
    protected void onPause() {
        super.onPause();
        ActivitiStateHolder.activityPaused();
        controllerManager.pauseControllers();
    }
}
