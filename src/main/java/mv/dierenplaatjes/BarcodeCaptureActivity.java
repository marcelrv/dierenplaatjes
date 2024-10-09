package mv.dierenplaatjes;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.KeyEvent;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

/**
 * Sample Activity extending from ActionBarActivity to display a Toolbar.
 */
public class BarcodeCaptureActivity extends AppCompatActivity {
    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private String lastText;
    private DierenPlayer dierenPlayer;

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() == null || (result.getText().equals(lastText) && dierenPlayer.isPlaying())) {
                // Prevent duplicate scans
                return;
            }

            lastText = result.getText();
            int cardId = Integer.parseInt(result.getText());
            dierenPlayer.play(cardId, PlayType.ALL);
            //Toast.makeText(barcodeScannerView.getContext(), "Yeay, found the barcode: " + result.getText(), Toast.LENGTH_SHORT).show();

            //Added preview of scanned barcode
            //      ImageView imageView = findViewById(R.id.barcodePreview);
            //   imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW));
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.capture_appcompat);
        Toolbar toolbar = findViewById(R.id.my_awesome_toolbar);
        toolbar.setTitle("Scan Barcode");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dierenPlayer = new DierenPlayer(getApplicationContext());
        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner);
        barcodeScannerView.decodeContinuous(callback);
        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dierenPlayer.stopPlaying();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }


    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}
