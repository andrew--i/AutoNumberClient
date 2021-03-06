package ai.autonumber.controller;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import ai.autonumber.activiti.AutoNumberChatActivity;
import ai.autonumber.R;
import ai.autonumber.cache.FileCache;
import ai.autonumber.gcm.ServerUtilities;
import ai.autonumber.model.CarMessage;
import ai.autonumber.util.DownloadImageForImageViewTask;
import ai.autonumber.util.UploadImageTask;
import ai.autonumber.util.UploadTaskParam;

public class MainController extends Controller {

    public static final int PHOTO_INTENT_REQUEST_CODE = 123;
    private Uri mUri;
    private CarMessage lastCarMessage;
    private FileCache fileCache;

    public MainController(final AutoNumberChatActivity activity, final ControllerManager controllerManager) {
        super(activity, controllerManager);
        fileCache = new FileCache(activity.getApplicationContext());
    }

    private Uri generateFileUri() {
        // Проверяем доступность SD карты
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return null;
        // Проверяем и создаем директорию
        File path = new File(Environment.getExternalStorageDirectory(), activity.getString(R.string.app_name));
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
    protected void onHandleStartActive() {

        final Button camButton = (Button) findViewById(R.id.camButton);
        final Button chatButton = (Button) findViewById(R.id.chatButton);

        chatButton.setText(R.string.chatButtonName);
        camButton.setText(R.string.camButtonName);

        camButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));
        chatButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));


        camButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUri = generateFileUri();
                if (mUri == null) {
                    Toast.makeText(activity.getApplicationContext(), "SD card not available", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
                activity.startActivityForResult(intent, PHOTO_INTENT_REQUEST_CODE);
            }
        });

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controllerManager.setChatControllerActive();
            }
        });

        updateCarPreview();
    }

    @Override
    protected void onHandleEndActive() {

    }

    @Override
    public List<Integer> getUsingControlsIds() {
        return Arrays.asList(R.id.imagePreview, R.id.chatButton, R.id.camButton);
    }

    public void handleCarMessage(CarMessage carMessage) {
        lastCarMessage = carMessage;
        if (controllerManager.isActiveController(thisController()))
            updateCarPreview();
    }

    private void updateCarPreview() {
        ImageView imageView = (ImageView) activity.findViewById(R.id.imagePreview);
        if (lastCarMessage == null)
            imageView.setImageBitmap(null);
        else {
            String id = lastCarMessage.getId();
            new DownloadImageForImageViewTask(imageView, fileCache).execute(id);
        }
    }

    @Override
    public void resumeController() {
        restoreLastCarMessage();
    }

    private void restoreLastCarMessage() {
        runAsync(new Action() {
            @Override
            public void doAction() throws IOException {
                ServerUtilities.restoreLastCarResult(activity.regid);
            }
        });
    }

    public void sendImageToServer() {
        ProgressDialog dialog = ProgressDialog.show(activity, "Загрузка", "Загрузка изображения");
        UploadTaskParam taskParam = new UploadTaskParam(mUri, activity.getContentResolver(),
                dialog,
                ServerUtilities.SERVER_URL + "/newimage",
                activity.regid);
        new UploadImageTask().execute(taskParam);
    }
}
