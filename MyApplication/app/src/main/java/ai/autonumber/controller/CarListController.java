package ai.autonumber.controller;


import java.util.List;

import ai.autonumber.AutoNumberChatActivity;

public class CarListController extends Controller {
    public CarListController(AutoNumberChatActivity activity, ControllerManager controllerManager) {
        super(activity, controllerManager);
    }

    @Override
    protected void initAsActiveController() {

    }

    @Override
    public List<Integer> getUsingControlsIds() {
        return null;
    }
}
