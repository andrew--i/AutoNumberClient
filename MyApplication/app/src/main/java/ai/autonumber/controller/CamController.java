package ai.autonumber.controller;


import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import ai.autonumber.AutoNumberChatActivity;
import ai.autonumber.R;
import ai.autonumber.gcm.ServerUtilities;

public class CamController extends Controller {

    public CamController(AutoNumberChatActivity activity, final ControllerManager controllerManager) {
        super(activity, controllerManager);

    }

    protected void sendImage(final byte[] byteArray) {
        runAsync(new Action() {
            @Override
            public void doAction() throws IOException {
                ServerUtilities.sendNewGameResultImage(activity.regid, byteArray);
            }
        });
    }

    @Override
    public List<Integer> getUsingControlsIds() {
        return Arrays.asList(R.id.imagePreview, R.id.chatButton, R.id.camButton);
    }

    @Override
    protected void initAsActiveController() {

        final Button camButton = (Button) findViewById(R.id.camButton);
        final Button chatButton = (Button) findViewById(R.id.chatButton);
        final EditText chatInput = (EditText) findViewById(R.id.chatInput);

        camButton.setText(R.string.sendButtonName);
        chatButton.setText(R.string.mainButtonName);

        chatInput.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.0f));
        camButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));
        chatButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));


        camButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView imageView = (ImageView) findViewById(R.id.imagePreview);
                imageView.setDrawingCacheEnabled(true);
                Bitmap bitmap = imageView.getDrawingCache();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                sendImage(byteArray);
            }
        });


        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controllerManager.setMainControllerActive();
            }
        });
    }

    public void updateImage(Uri imageFile) {
        ((ImageView) findViewById(R.id.imagePreview)).setImageURI(imageFile);
    }
}
