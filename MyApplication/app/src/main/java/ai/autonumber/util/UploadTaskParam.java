package ai.autonumber.util;

import android.app.ProgressDialog;
import android.net.Uri;

/**
 * Created by Andrew on 07.09.2014.
 */
public class UploadTaskParam {
    Uri uri;
    ProgressDialog dialog;
    String uploadServerUri;

    public UploadTaskParam(Uri uri, ProgressDialog dialog, String uploadServerUri) {
        this.uri = uri;
        this.dialog = dialog;
        this.uploadServerUri = uploadServerUri;
    }
}
