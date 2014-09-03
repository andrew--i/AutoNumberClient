package ai.autonumber.controller;


import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;

import java.io.IOException;
import java.util.List;

import ai.autonumber.AutoNumberChatActivity;

public abstract class Controller {
    protected AutoNumberChatActivity activity;
    protected ControllerManager controllerManager;

    public Controller(AutoNumberChatActivity activity, ControllerManager controllerManager) {
        this.activity = activity;
        this.controllerManager = controllerManager;
    }

    protected abstract void initAsActiveController();

    protected View findViewById(int id) {
        return activity.findViewById(id);
    }


    protected void runAsync(final Action action) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void[] params) {
                try {
                    action.doAction();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    public void resumeController() {

    }

    public void pauseController() {

    }

    protected Controller thisController() {
        return this;
    }

    public abstract List<Integer> getUsingControlsIds();

    interface Action {
        void doAction() throws IOException;
    }
}
