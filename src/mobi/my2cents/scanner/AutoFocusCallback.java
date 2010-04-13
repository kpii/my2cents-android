/*
 * Copyright (C) 2010 ZXing authors
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

import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;

final class AutoFocusCallback implements Camera.AutoFocusCallback {

  private static final long AUTOFOCUS_INTERVAL_MS = 1500L;

  private final CameraConfigurationManager configManager;
  private boolean reinitCamera;
  private Handler autoFocusHandler;
  private int autoFocusMessage;

  AutoFocusCallback(CameraConfigurationManager configManager) {
    this.configManager = configManager;
  }

  void setHandler(Handler autoFocusHandler, int autoFocusMessage) {
    this.autoFocusHandler = autoFocusHandler;
    this.autoFocusMessage = autoFocusMessage;
  }

  public void onAutoFocus(boolean success, Camera camera) {
    if (autoFocusHandler != null) {
      Message message = autoFocusHandler.obtainMessage(autoFocusMessage, success);
      // Simulate continuous autofocus by sending a focus request every
      // AUTOFOCUS_INTERVAL_MS milliseconds.
      autoFocusHandler.sendMessageDelayed(message, AUTOFOCUS_INTERVAL_MS);
      autoFocusHandler = null;
      if (!reinitCamera) {
        reinitCamera = true;
        configManager.setDesiredCameraParameters(camera);
      }
    }
  }

}
