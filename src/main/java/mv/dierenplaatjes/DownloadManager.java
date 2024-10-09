package mv.dierenplaatjes;

import android.util.Log;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

public class DownloadManager {
    private static final String TAG = "Downloader";
    private final OkHttpClient client = new OkHttpClient();

    int counter = 0;
    int msgCounter = 0;
    // Callback interface to notify the caller about download completion or failure
    public interface DownloadCallback {
        void onSuccess(File downloadedFile);
        void onFailure(IOException e);
        void onProgress(int progress);
    }

    // Method to start the file download
    public void downloadFile(String fileUrl, File targetFile, DownloadCallback callback) {
        Request request = new Request.Builder().url(fileUrl).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Download failed: " + e.getMessage());
                callback.onFailure(e);  // Notify failure
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onFailure(new IOException("Unexpected code " + response));
                    return;
                }

                // Stream the file content to disk
                try {
                    streamToFile(response.body().byteStream(), targetFile,callback);
                    Log.d(TAG, "File downloaded successfully: " + targetFile.getAbsolutePath());
                    callback.onSuccess(targetFile);  // Notify success
                } catch (IOException e) {
                    Log.e(TAG, "Error streaming file: " + e.getMessage());
                    callback.onFailure(e);
                }
            }
        });
    }

    // Method to stream file from InputStream to File
    private void streamToFile(InputStream inputStream, File targetFile, DownloadCallback callback) throws IOException {
        try (Sink fileSink = Okio.sink(targetFile);
             BufferedSink bufferedSink = Okio.buffer(fileSink)) {

            byte[] buffer = new byte[8192];  // 8KB buffer
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                bufferedSink.write(buffer, 0, bytesRead);
                counter= counter + bytesRead;
                if (counter > msgCounter* 1024*1024) {
                    msgCounter++;
                    Log.d(TAG, "Downloaded " + counter + " bytes");
                    callback.onProgress(counter);
                }
            }
            bufferedSink.flush();
        }
    }

    void unpackTgzFile(File tgzFile, File destDir) {
        try (FileInputStream fis = new FileInputStream(tgzFile);
             GZIPInputStream gzipInputStream = new GZIPInputStream(fis);
             TarArchiveInputStream tarInputStream = new TarArchiveInputStream(gzipInputStream)) {

            TarArchiveEntry entry;
            while ((entry = tarInputStream.getNextTarEntry()) != null) {
                if (entry.isDirectory()) {
                    // Create directories in app-specific storage
                    File dir = new File(destDir, entry.getName());
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                } else {
                    // Extract files in app-specific storage

                    String fileName = entry.getName().split("/")[1];
                    if (Character.isDigit(fileName.charAt(0))) {
                        fileName = "s"+ fileName.substring(2);
                    }
                    Log.d(TAG, "Extracting: " + fileName);

                    File outputFile = new File(destDir,fileName);
                    File parentDir = outputFile.getParentFile();
                    if (!parentDir.exists()) {
                        parentDir.mkdirs();  // Ensure the parent directory exists
                    }

                    FileOutputStream fos = new FileOutputStream(outputFile);
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = tarInputStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
            }
            Log.d(TAG, "Unpacking complete!");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error unpacking .tgz file: " + e.getMessage());
        }
    }
}
