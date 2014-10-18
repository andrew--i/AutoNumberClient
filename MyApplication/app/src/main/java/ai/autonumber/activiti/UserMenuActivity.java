package ai.autonumber.activiti;


import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;

import ai.autonumber.R;
import ai.autonumber.controller.Controller;
import ai.autonumber.gcm.ServerUtilities;
import ai.autonumber.model.User;
import ai.autonumber.state.AppStateHolder;

public class UserMenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_menu_activiti);

        User currentUser = AppStateHolder.currentUser;
        if (currentUser != null) {
            TextView userNameInput = (TextView) findViewById(R.id.userNameEdit);
            userNameInput.setText(currentUser.getViewName());
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        TextView userNameInput = (TextView) findViewById(R.id.userNameEdit);
        CharSequence text = userNameInput.getText();
        if (text != null) {
            final String userName = text.toString().trim();
            final User currentUser = AppStateHolder.currentUser;
            if (currentUser != null) {
                if (!userName.equals(currentUser.getHumanName()))
                    Controller.runAsync(new Controller.Action() {
                        @Override
                        public void doAction() throws IOException {
                            ServerUtilities.changeCurrentUserName(currentUser.getId(), userName);
                        }
                    });
            }
        }
    }
}
