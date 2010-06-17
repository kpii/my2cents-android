/***
	Copyright (c) 2009 CommonsWare, LLC
	
	Licensed under the Apache License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may obtain
	a copy of the License at
		http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package mobi.my2cents;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

abstract public class WakefulIntentService extends IntentService {
	abstract protected void doWakefulWork(Intent intent);
	
	public static final String LOCK_NAME_STATIC="com.commonsware.cwac.wakeful.WakefulIntentService";
	private static PowerManager.WakeLock lockStatic=null;
	
	public static void acquireStaticLock(Context context) {
		getLock(context).acquire();
	}
	
	synchronized private static PowerManager.WakeLock getLock(Context context) {
		if (lockStatic==null) {
			PowerManager mgr=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
			
			lockStatic=mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
														LOCK_NAME_STATIC);
			lockStatic.setReferenceCounted(true);
		}
		
		return(lockStatic);
	}
	
	public WakefulIntentService(String name) {
		super(name);
	}
	
	@Override
  public void onStart(Intent intent, int startId) {
		if (!getLock(this).isHeld()) {	// fail-safe for crash restart
			getLock(this).acquire();
		}

		super.onStart(intent, startId);
	}
	
	@Override
	final protected void onHandleIntent(Intent intent) {
		try {
			doWakefulWork(intent);
		}
		finally {
			getLock(this).release();
		}
	}
}
