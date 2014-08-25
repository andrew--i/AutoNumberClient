package ai.autonumber;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;

import ai.autonumber.state.ActivitiState;


public class CameraTestActivity extends Activity {
    private static final int PHOTO_INTENT_REQUEST_CODE = 123;
    private Context context;
    private Uri mUri;
    private ActivitiState currentActivitiState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_test);
        updateAppearance(ActivitiState.MAIN_STATE);
        context = this;
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
                    updateAppearance(ActivitiState.MAIN_STATE);
                }
            }
        });

        Button chatButton = (Button) findViewById(R.id.chatButton);
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentActivitiState == ActivitiState.CAM_STATE)
                    updateAppearance(ActivitiState.MAIN_STATE);
            }
        });
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private Uri generateFileUri() {
        // Проверяем доступность SD карты
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return null;

        // Проверяем и создаем директорию
        File path = new File(Environment.getExternalStorageDirectory(), "CameraTest");
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
                //Log.w ("MY", "bitmap: " + data.getExtras().get("data"));
                ((ImageView) findViewById(R.id.camPreview)).setImageURI(mUri);
                updateAppearance(ActivitiState.CAM_STATE);

            } else if (resultCode == RESULT_CANCELED)
                Toast.makeText(this, "Capture cancelled", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, "Capture failed", Toast.LENGTH_LONG).show();
        }
    }

}
