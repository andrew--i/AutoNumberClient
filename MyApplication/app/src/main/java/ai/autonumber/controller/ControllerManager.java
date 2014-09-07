package ai.autonumber.controller;

import android.app.Activity;
import android.view.View;

import java.util.Arrays;
import java.util.List;

import ai.autonumber.AutoNumberChatActivity;
import ai.autonumber.R;
import ai.autonumber.model.CarMessage;
import ai.autonumber.model.ChatMessage;


public class ControllerManager {
    private Activity activity;
    private MainController mainController;
    private CarListController carListController;
    private ChatController chatController;

    private Controller activeController = null;

    public ControllerManager(AutoNumberChatActivity activity) {
        this.activity = activity;
        mainController = new MainController(activity, this);
        carListController = new CarListController(activity, this);
        chatController = new ChatController(activity, this);
    }

    public boolean isActiveController(Controller controller) {
        return controller == activeController;
    }

    public void setActiveController(Controller activeController) {
        this.activeController = activeController;
        hideAllControls();
        setVisibleControllerControls(activeController.getUsingControlsIds());
        activeController.initAsActiveController();
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

    public void handleChatMessage(ChatMessage message, boolean isCurrentUser) {
        chatController.handleChatMessage(message, isCurrentUser);
    }

    public void resumeControllers() {
        mainController.resumeController();
        carListController.resumeController();
        chatController.resumeController();
    }

    public void pauseControllers() {
        mainController.pauseController();
        carListController.pauseController();
        chatController.pauseController();
    }

    public void setMainControllerActive() {
        setActiveController(mainController);
    }

    public void setChatControllerActive() {
        setActiveController(chatController);
    }

    public void setActiveMainController() {
        setActiveController(mainController);
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
}
