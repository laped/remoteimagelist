package dk.remoteimagelist.image;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import dk.remoteimagelist.cache.ImageCache;

public class RemoteImageLoaderJob implements Runnable {

    private static final String LOG_TAG = "Ignition/ImageLoader";

    private static final int DEFAULT_RETRY_HANDLER_SLEEP_TIME = 1000;

    private String imageUrl;
    private RemoteImageLoaderHandler handler;
    private ImageCache imageCache;
    private int numRetries, defaultBufferSize;

    public RemoteImageLoaderJob(String imageUrl, RemoteImageLoaderHandler handler, ImageCache imageCache,
            int numRetries, int defaultBufferSize) {
        this.imageUrl = imageUrl;
        this.handler = handler;
        this.imageCache = imageCache;
        this.numRetries = numRetries;
    }

    /**
     * The job method run on a worker thread. It will first query the image cache, and on a miss,
     * download the image from the Web.
     */
    @Override
    public void run() {
        Bitmap bitmap = null;

        if (imageCache != null) {
            // at this point we know the image is not in memory, but it could be cached to SD card
            bitmap = imageCache.getBitmap(imageUrl);
        }

        if (bitmap == null) {
            bitmap = downloadImage();
        }

        notifyImageLoaded(imageUrl, bitmap);
    }

    // TODO: we could probably improve performance by re-using connections instead of closing them
    // after each and every download
    protected Bitmap downloadImage() {
        int timesTried = 1;

        while (timesTried <= numRetries) {
            try {
                byte[] imageData = retrieveImageData();

                if (imageData == null) {
                    break;
                }

                if (imageCache != null) {
                    imageCache.put(imageUrl, imageData);
                }

                return BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

            } catch (Throwable e) {
                Log.w(LOG_TAG, "download for " + imageUrl + " failed (attempt " + timesTried + ")");
                e.printStackTrace();
                SystemClock.sleep(DEFAULT_RETRY_HANDLER_SLEEP_TIME);
                timesTried++;
            }
        }

        return null;
    }

    protected byte[] retrieveImageData() throws IOException {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // determine the image size and allocate a buffer
        int fileSize = connection.getContentLength();
        if (fileSize <= 0) {
            fileSize = defaultBufferSize;
            Log.w(LOG_TAG,
                    "Server did not set a Content-Length header, will default to buffer size of "
                            + defaultBufferSize + " bytes");
        }
        byte[] imageData = new byte[fileSize];

        // download the file
        Log.d(LOG_TAG, "fetching image " + imageUrl + " (" + fileSize + ")");
        BufferedInputStream istream = new BufferedInputStream(connection.getInputStream());
        int bytesRead = 0;
        int offset = 0;
        while (bytesRead != -1 && offset < fileSize) {
            bytesRead = istream.read(imageData, offset, fileSize - offset);
            offset += bytesRead;
        }

        // clean up
        istream.close();
        connection.disconnect();

        return imageData;
    }

    protected void notifyImageLoaded(String url, Bitmap bitmap) {
        Message message = new Message();
        message.what = RemoteImageLoaderHandler.HANDLER_MESSAGE_ID;
        Bundle data = new Bundle();
        data.putString(RemoteImageLoaderHandler.IMAGE_URL_EXTRA, url);
        Bitmap image = bitmap;
        data.putParcelable(RemoteImageLoaderHandler.BITMAP_EXTRA, image);
        message.setData(data);

        handler.sendMessage(message);
    }
}
