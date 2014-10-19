package ai.autonumber.activiti;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.IOException;

import ai.autonumber.R;
import ai.autonumber.adapter.LazyPhotoLoadAdapter;
import ai.autonumber.controller.Controller;
import ai.autonumber.gcm.GcmIntentService;
import ai.autonumber.gcm.GoogleCloudMessageActivity;
import ai.autonumber.gcm.ServerUtilities;
import ai.autonumber.model.User;
import ai.autonumber.state.AppStateHolder;

public class PhotosListActivity extends Activity {

    private GridView getPhotosListView() {
        return (GridView) findViewById(R.id.photosListView);
    }

    private LazyPhotoLoadAdapter getListViewAdapter() {
        return (LazyPhotoLoadAdapter) getPhotosListView().getAdapter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photos_view_activiti);
        getPhotosListView().setAdapter(new LazyPhotoLoadAdapter(this));
        final Context context = this;
        getPhotosListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Dialog settingsDialog = new Dialog(context);
                LazyPhotoLoadAdapter adapter = (LazyPhotoLoadAdapter) adapterView.getAdapter();
                settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                View inflate = getLayoutInflater().inflate(R.layout.photo_view_layout, null);
                settingsDialog.setContentView(inflate);
                ((ImageView) inflate.findViewById(R.id.popupPhotoView)).setImageBitmap(adapter.getImageBitmap(position));
                settingsDialog.show();
            }
        });
    }

    @Override
    public void onDestroy() {
        getPhotosListView().setAdapter(null);
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.photosviewmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clearCacheItem:
                getListViewAdapter().clearCaches();
                Controller.runAsync(new Controller.Action() {
                    @Override
                    public void doAction() throws IOException {
                        final User currentUser = AppStateHolder.currentUser;
                        if (currentUser != null)
                            ServerUtilities.restoreAllPhotos(currentUser.getRegId());
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mMessageReceiver, new IntentFilter(GoogleCloudMessageActivity.INTENT_ACTION));
        Controller.runAsync(new Controller.Action() {
            @Override
            public void doAction() throws IOException {
                final User currentUser = AppStateHolder.currentUser;
                if (currentUser != null)
                    ServerUtilities.restoreAllPhotos(currentUser.getRegId());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mMessageReceiver);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(GcmIntentService.CARS_MESSAGE_TOKEN);
            if (message != null) {
                getListViewAdapter().add(Integer.parseInt(message));
            }
        }
    };
}
