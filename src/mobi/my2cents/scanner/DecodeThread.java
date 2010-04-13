/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mobi.my2cents.scanner;

import java.util.Hashtable;
import java.util.Vector;

import mobi.my2cents.ScanActivity;
import android.os.Handler;
import android.os.Looper;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ResultPointCallback;

/**
 * This thread does all the heavy lifting of decoding the images.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
final class DecodeThread extends Thread {

  public static final String BARCODE_BITMAP = "barcode_bitmap";

  private final ScanActivity activity;
  private final Hashtable<DecodeHintType, Object> hints;
  private Handler handler;

  DecodeThread(ScanActivity activity,
               Vector<BarcodeFormat> decodeFormats,
               ResultPointCallback resultPointCallback) {

    this.activity = activity;

    hints = new Hashtable<DecodeHintType, Object>(3);
    if (decodeFormats != null) {
    	hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
    }    
    hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, resultPointCallback);
  }

  Handler getHandler() {
    return handler;
  }

  @Override
  public void run() {
    Looper.prepare();
    handler = new DecodeHandler(activity, hints);    
    Looper.loop();
  }

}
