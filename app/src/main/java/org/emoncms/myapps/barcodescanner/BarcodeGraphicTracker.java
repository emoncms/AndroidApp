package org.emoncms.myapps.barcodescanner;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

class BarcodeGraphicTracker extends Tracker<Barcode> {
    private Callback mCallback;

    BarcodeGraphicTracker(Callback callback) {
        mCallback = callback;
    }

    public interface Callback {
        void onFound(Barcode barcode);
    }

    @Override
    public void onUpdate(Detector.Detections<Barcode> detectionResults, Barcode item) {
        mCallback.onFound(item);
    }
}