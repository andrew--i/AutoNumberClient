package ai.autonumber.util;

import android.app.ProgressDialog;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class UploadImageTask extends AsyncTask<UploadTaskParam, Void, String> {

    @Override
    protected String doInBackground(UploadTaskParam... taskParams) {

        UploadTaskParam taskParam = taskParams[0];
        return upload(taskParam);
    }

    private String upload(UploadTaskParam taskParam) {
        HttpURLConnection conn;
        DataOutputStream dos;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        int serverResponseCode;

        String message = "";
        final ProgressDialog dialog = taskParam.getDialog();
        {
            try {

                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                final AssetFileDescriptor fileDescriptor = taskParam.getContentResolver().openAssetFileDescriptor( taskParam.getUri(), "r");

                final Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

                // open a URL connection to the Servlet
                byte[] byteArray = stream.toByteArray();
                InputStream fileInputStream = new ByteArrayInputStream(byteArray);
                final String uploadServerUri = taskParam.getUploadServerUri();
                URL url = new URL(uploadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                dos = new DataOutputStream(conn.getOutputStream());
                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                dos.write(taskParam.getRegId().getBytes("utf-8"));

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if (serverResponseCode == 200) {

                    message = "File Upload Complete";
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                dialog.dismiss();
                ex.printStackTrace();

                message = "MalformedURLException";
                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                dialog.dismiss();
                e.printStackTrace();

                message = "Got Exception : see logcat ";
                Log.e("Upload file to server Exception", "Exception : "
                        + e.getMessage(), e);
            } finally {
                dialog.dismiss();
            }
            dialog.dismiss();
            File file = new File(taskParam.getUri().getPath());
            if (file.exists())
                file.deleteOnExit();
            return message;

        } // End else block
    }
}
