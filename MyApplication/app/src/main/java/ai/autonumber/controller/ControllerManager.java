package ai.autonumber.controller;

import android.app.Activity;
import android.view.View;

import java.util.Arrays;
import java.util.List;

import ai.autonumber.activiti.AutoNumberChatActivity;
import ai.autonumber.R;
import ai.autonumber.model.CarMessage;
import ai.autonumber.model.ChatMessage;
import ai.autonumber.model.User;
import ai.autonumber.state.AppStateHolder;


public class ControllerManager {
    private final Activity activity;
    private final MainController mainController;
    private final ChatController chatController;
    private final List<Controller> controllers;
    private Controller activeController = null;


    public ControllerManager(AutoNumberChatActivity activity) {
        this.activity = activity;
        chatController = new ChatController(activity, this);
        mainController = new MainController(activity, this);
        controllers = Arrays.asList(mainController, chatController);
    }

    public boolean isActiveController(Controller controller) {
        return controller == activeController;
    }

    public void setActiveController(Controller activeController) {
        this.activeController = activeController;
        hideAllControls();
        setVisibleControllerControls(activeController.getUsingControlsIds());
        activeController.onHandleStartActive();
        for (Controller controller : controllers) {
            if (activeController == controller)
                continue;
            controller.onHandleEndActive();
        }
    }

    private void setVisibleControllerControls(List<Integer> usingControlsIds) {
        for (Integer id : usingControlsIds) {
            activity.findViewById(id).setVisibility(View.VISIBLE);
        }
    }

    private void hideAllControls() {
        List<Integer> ids = Arrays.asList(
                R.id.imagePreview,
                R.id.chatView,
                R.id.camButton,
                R.id.chatInput,
                R.id.chatButton);
        for (Integer id : ids) {
            activity.findViewById(id).setVisibility(View.GONE);
        }

    }

    public void handleChatMessage(ChatMessage message) {
        chatController.handleChatMessage(message);
    }

    public void resumeControllers() {
        for (Controller controller : controllers) {
            controller.resumeController();
        }
    }

    public void pauseControllers() {
        for (Controller controller : controllers) {
            controller.pauseController();
        }
    }

    public void setMainControllerActive() {
        setActiveController(mainController);
    }

    public void setChatControllerActive() {
        setActiveController(chatController);
    }

    public void handleNewCarMessage(CarMessage carMessage) {
        mainController.handleCarMessage(carMessage);
    }

    public void handleLastCarMessage(CarMessage carMessage) {
        mainController.handleCarMessage(carMessage);
    }

    public void sendImageToServer() {
        mainController.sendImageToServer();
    }

    public void handleChangeCurrentUser() {
        chatController.handleChangeUserInfo(AppStateHolder.currentUser);
    }

    public void handleChangeUserInfo(User user) {
        chatController.handleChangeUserInfo(user);
    }
}
