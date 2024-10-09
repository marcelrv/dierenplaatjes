package mv.dierenplaatjes;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.mikepenz.aboutlibraries.LibsBuilder;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

       public static final String DOWNLOAD_URL = "https://up.shiz.me/f/ah-dierkaartjes.tar.gz";

    public static String PACKAGE_NAME;
    private static final String TAG = "MainActivity";

    public PlayType playType = PlayType.ALL;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() == null) {
                    Intent originalIntent = result.getOriginalIntent();
                    if (originalIntent == null) {
                        Log.d("MainActivity", "Cancelled scan");
                        Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_LONG).show();
                    } else if (originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                        Log.d("MainActivity", "Cancelled scan due to missing camera permission");
                        Toast.makeText(MainActivity.this, "Cancelled due to missing camera permission", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d("MainActivity", "Scanned");
                    Toast.makeText(MainActivity.this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PACKAGE_NAME = getApplicationContext().getPackageName();
        ProgressBar progressBar = findViewById(R.id.progressBar1);
        progressBar.setVisibility(View.VISIBLE);

        RadioGroup radioGroup = findViewById(R.id.soundTypeRadio);
        radioGroup.setVisibility(View.INVISIBLE);

        File appSpecificDir = getExternalFilesDir(null); // App-specific directory
        if (appSpecificDir != null) {
            File tgzFile = new File(appSpecificDir, "downloaded_file.tgz");
            if (!tgzFile.exists()) {
                downloadInProgress(true);
                Log.d(TAG, "Downloading sound files..");


                Toast.makeText(MainActivity.this, "Downloading sound files..", Toast.LENGTH_SHORT).show();

                DownloadManager downloader = new DownloadManager();
                downloader.downloadFile(DOWNLOAD_URL, tgzFile, new DownloadManager.DownloadCallback() {
                    @Override
                    public void onSuccess(File downloadedFile) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(() -> {
                            ((Button) findViewById(R.id.btn_scan)).setText(getString(R.string.extracting));  //do UI stuff
                            TextView progressText = findViewById(R.id.progressText);
                            progressText.setText("Extracting sound files..");
                        });
                        // Once download completes, unpack the file
                        Log.d(TAG, "Extracting sound files..");
                        downloader.unpackTgzFile(downloadedFile, appSpecificDir);
                        handler.post(() -> downloadInProgress(false));
                    }

                    @Override
                    public void onProgress(int progress) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(() -> {
                            TextView textView = findViewById(R.id.progressText);
                            textView.setText(String.format("Downloaded %d KB", progress / 1024));
                        });
                    }

                    @Override
                    public void onFailure(IOException e) {
                        Log.e(TAG, "Download failed: " + e.getMessage());
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(() -> {
                            TextView textView = findViewById(R.id.progressText);
                            textView.setText("Download failed " + e.getMessage());
                        });
                    }
                });
            } else {
                progressBar.setVisibility(View.INVISIBLE);

                Toast.makeText(MainActivity.this, "SoundFile existing " + tgzFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "No app specific directory", Toast.LENGTH_SHORT).show();
        }

    }

    private void downloadInProgress(boolean downloading) {
        Button btnScan = findViewById(R.id.btn_scan);
        ProgressBar progressBar = findViewById(R.id.progressBar1);
        TextView progressText = findViewById(R.id.progressText);
        if (downloading) {
            progressBar.setVisibility(View.VISIBLE);
            progressText.setVisibility(View.VISIBLE);
            btnScan.setEnabled(false);
            btnScan.setText(getString(R.string.downloading));
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            progressText.setVisibility(View.INVISIBLE);
            btnScan.setText(getString(R.string.btn_scan));
            btnScan.setEnabled(true);
        }
    }

    private int stateT;

    public void toggleTest(View view) {
        //  Button btnScan = findViewById(R.id.btn_scan);

        if (stateT == 0) {
            stateT = 1;
            downloadInProgress(true);
        } else {
            stateT = 0;
            downloadInProgress(false);

            //btnScan.setBackgroundColor(Color.GRAY);
        }
    }

    public void radio_clicked(View view) {
        RadioGroup radioGroup = findViewById(R.id.soundTypeRadio);
        int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();

        switch (checkedRadioButtonId) {
            case R.id.sound_questions:
                playType = PlayType.ALL;
                break;
            case R.id.soundonly:
                playType = PlayType.ANIMAL_ONLY;
                break;
            case R.id.question_only:
                playType = PlayType.QUESTION_ONLY;
                break;
            default:
                playType = PlayType.ALL;
                break;
        }
    }

    public void scanContinuous(View view) {
        Intent intent = new Intent(this, ContinuousCaptureActivity.class);
        startActivity(intent);
    }

    public void scanToolbar(View view) {
        ScanOptions options = new ScanOptions().setCaptureActivity(BarcodeCaptureActivity.class);
        /*
          ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.ONE_D_CODE_TYPES);
        options.setPrompt("Scan something");
        options.setOrientationLocked(false);
        options.setBeepEnabled(true);
        barcodeLauncher.launch(options);
         */
        barcodeLauncher.launch(options);
    }


    public void deleteMyFile(View view) {
        deleteDLFile("downloaded_file.tgz");
    }

    private void deleteDLFile(String fileName) {
        Log.d(TAG, "Deleting file: " + fileName);
        File appSpecificDir = getExternalFilesDir(null); // App-specific directory
        if (appSpecificDir != null) {
            File file = new File(appSpecificDir, fileName);
            if (file.exists()) {
                file.delete();
            }
            Toast.makeText(MainActivity.this, "File deleted: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        }
    }

    public void aboutLibs(View view) {
        deleteDLFile("downloaded_file.tgz");
        new LibsBuilder().start(this);
    }


    // Method to show the About dialog
    public void showAboutDialog(View view) {
        // Inflate the custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View aboutView = inflater.inflate(R.layout.about_dialog, null);

        // Retrieve the version number dynamically
        TextView versionTextView = aboutView.findViewById(R.id.about_version);

        try{
            String versionName =  getApplicationContext().getPackageManager()
                    .getPackageInfo( getApplicationContext().getPackageName(), 0).versionName;
            String version = "Version: " + versionName;
            versionTextView.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        // Create and show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(aboutView);
        builder.setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss());

        builder.create().show();
    }

    // Method to open GitHub link when the user clicks on the GitHub link
    public void openGitHubLink(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/marcelrv/dierenapp"));
        startActivity(browserIntent);
    }

    /**
     * Sample of scanning from a Fragment
     */
    /*
    public static class ScanFragment extends Fragment {
        private final ActivityResultLauncher<ScanOptions> fragmentLauncher = registerForActivityResult(new ScanContract(),
                result -> {
                    if(result.getContents() == null) {
                        Toast.makeText(getContext(), "Cancelled from fragment", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Scanned from fragment: " + result.getContents(), Toast.LENGTH_LONG).show();
                    }
                });

        public ScanFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_scan, container, false);
            Button scan = view.findViewById(R.id.scan_from_fragment);
            scan.setOnClickListener(v -> scanFromFragment());
           return view;
        }

        public void scanFromFragment() {
            fragmentLauncher.launch(new ScanOptions());
        }
    }
*/


}
