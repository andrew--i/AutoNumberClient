package ai.autonumber.util;

import android.app.ProgressDialog;
import android.net.Uri;

public class UploadTaskParam {
    private Uri uri;
    private ProgressDialog dialog;
    private String uploadServerUri;
    private String regId;

    public UploadTaskParam(Uri uri, ProgressDialog dialog, String uploadServerUri, String regId) {
        this.uri = uri;
        this.dialog = dialog;
        this.uploadServerUri = uploadServerUri;
        this.regId = regId;
    }

    public String getRegId() {
        return regId;
    }

    public Uri getUri() {
        return uri;
    }

    public ProgressDialog getDialog() {
        return dialog;
    }

    public String getUploadServerUri() {
        return uploadServerUri;
    }
}
