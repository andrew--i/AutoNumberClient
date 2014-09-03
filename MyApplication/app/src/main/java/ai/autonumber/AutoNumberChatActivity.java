package ai.autonumber;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;

import ai.autonumber.adapter.ChatItemsArrayAdapter;
import ai.autonumber.gcm.GoogleCloudMessageActiviti;
import ai.autonumber.model.ChatMessage;
import ai.autonumber.state.ActivitiState;
import ai.autonumber.state.ActivitiStateHolder;


public class AutoNumberChatActivity extends GoogleCloudMessageActiviti {
    private static final int PHOTO_INTENT_REQUEST_CODE = 123;
    private Uri mUri;
    private ActivitiState currentActivitiState;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_test);
        updateAppearance(ActivitiState.MAIN_STATE);
        context = this;

        initChatView();
        Button camButton = (Button) findViewById(R.id.camButton);
        camButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (currentActivitiState == ActivitiState.MAIN_STATE) {
                    mUri = generateFileUri();
                    if (mUri == null) {
                        Toast.makeText(context, "SD card not available", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
                    startActivityForResult(intent, PHOTO_INTENT_REQUEST_CODE);
                } else {
                    ImageView imageView = (ImageView) findViewById(R.id.camPreview);
                    imageView.setDrawingCacheEnabled(true);
                    Bitmap bitmap = imageView.getDrawingCache();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    updateAppearance(ActivitiState.MAIN_STATE);
                    sendImage(byteArray);
                }
            }
        });

        Button chatButton = (Button) findViewById(R.id.chatButton);
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentActivitiState == ActivitiState.CAM_STATE)
                    updateAppearance(ActivitiState.MAIN_STATE);
                else if (currentActivitiState == ActivitiState.MAIN_STATE) {
                    addChatMessageFromInput();
                }
            }
        });

        chatButton.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    addChatMessageFromInput();
                    return true;
                }
                return false;
            }
        });
    }


    @Override
    protected String getMainActivitiClassName() {
        return AutoNumberChatActivity.class.getSimpleName();
    }

    private void addChatMessageFromInput() {
        final EditText chatInput = (EditText) findViewById(R.id.chatInput);
        String comment = chatInput.getText().toString();
        chatInput.setText("");
        sendMessage(comment);
    }

    @Override
    protected void handleChatMessage(String message) {
        ChatMessage chatMessage = ChatMessage.fromJson(message);
        if (chatMessage != null) {
            chatMessage.left = userName.equalsIgnoreCase(chatMessage.getUserName());
            ListView chatView = (ListView) findViewById(R.id.chatView);
            ((ChatItemsArrayAdapter) chatView.getAdapter()).add(chatMessage);
        }
    }

    @Override
    protected void handleSearchCarNumber(String searchCarNumber) {
        TextView textView = (TextView) findViewById(R.id.searchNumberTextField);
        if (textView != null)
            textView.setText(getString(R.string.searchCarNumberTemplate) + searchCarNumber);
    }

    private void initChatView() {
        ListView chatView = (ListView) findViewById(R.id.chatView);
        ChatItemsArrayAdapter adapter = new ChatItemsArrayAdapter(getApplicationContext(), R.layout.chat_item);
        chatView.setAdapter(adapter);

    }

    private void updateAppearance(ActivitiState state) {
        if (currentActivitiState == state)
            return;
        currentActivitiState = state;
        final Button camButton = (Button) findViewById(R.id.camButton);
        final Button chatButton = (Button) findViewById(R.id.chatButton);
        final EditText chatInput = (EditText) findViewById(R.id.chatInput);
        switch (currentActivitiState) {
            case MAIN_STATE:
                findViewById(R.id.chatView).setVisibility(View.VISIBLE);
                findViewById(R.id.camPreview).setVisibility(View.GONE);
                chatButton.setText(R.string.chatButtonName);
                chatButton.setVisibility(View.VISIBLE);
                camButton.setText(R.string.camButtonName);
                camButton.setVisibility(View.VISIBLE);
                chatButton.setText(R.string.sendButtonName);
                chatInput.setVisibility(View.VISIBLE);
                chatInput.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.2f));
                camButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.4f));
                chatButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.4f));

                break;
            case CAM_STATE:
                findViewById(R.id.chatView).setVisibility(View.GONE);
                findViewById(R.id.camPreview).setVisibility(View.VISIBLE);
                chatButton.setVisibility(View.VISIBLE);
                chatButton.setText(R.string.backButtonName);
                camButton.setVisibility(View.VISIBLE);
                camButton.setText(R.string.sendButtonName);
                chatInput.setVisibility(View.GONE);

                chatInput.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.0f));
                camButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));
                chatButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.camera_test, menu);
        return true;
    }

    private Uri generateFileUri() {
        // Проверяем доступность SD карты
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return null;

        // Проверяем и создаем директорию
        File path = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
        if (!path.exists()) {
            if (!path.mkdirs()) {
                return null;
            }
        }

        // Создаем имя файла
        String timeStamp = String.valueOf(System.currentTimeMillis());
        File newFile = new File(path.getPath() + File.separator + timeStamp + ".jpg");
        return Uri.fromFile(newFile);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_INTENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                ((ImageView) findViewById(R.id.camPreview)).setImageURI(mUri);
                updateAppearance(ActivitiState.CAM_STATE);

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
        restoreChatMessages();
        restoreSearchCarNumber();
    }


    @Override
    protected void onPause() {
        super.onPause();
        ActivitiStateHolder.activityPaused();
    }
}
