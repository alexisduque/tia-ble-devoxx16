/**  * Created by Alexis DUQUE - alexisd61@gmail.com.  * Date : 05/04/16.  */

package fr.rtone.android.devoxxfr.service;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import fr.rtone.android.devoxxfr.profile.BleManager;
import fr.rtone.android.devoxxfr.profile.BleProfileService;

public class HrmService extends BleProfileService implements HrmManagerCallbacks {
	private static final String TAG = "HrmService";

	public static final String BROADCAST_SENSOR_STATE_CHANGED = "fr.rtone.android.devoxxfr.BROADCAST_SENSOR_STATE_CHANGED";
	public static final String BROADCAST_BATTERY_LEVEL_CHANGED = "fr.rtone.android.devoxxfr.BROADCAST_BATTERY_LEVEL_CHANGED";
	public static final String BROADCAST_HRM_VALUE_CHANGED = "fr.rtone.android.devoxxfr.BROADCAST_HRM_VALUE_CHANGED";
	public static final String EXTRA_HRM_VALUE = "fr.rtone.android.devoxxfr.EXTRA_HRM_VALUE";
	public static final String EXTRA_DATA = "fr.rtone.android.devoxxfr.EXTRA_DATA";

	private HrmManager mManager;

	private final HrmBinder mBinder = new HrmBinder();

	public class HrmBinder extends BleProfileService.LocalBinder implements HrmInterface {
		private boolean mSensorState;
		private boolean mButtonState;

		@Override
		public void send(final boolean onOff) {
			mManager.send(mSensorState = onOff);
			onDataSent(onOff);
		}

		public boolean isOn() {
			return mSensorState;
		}

		public boolean isButtonPressed() {
			return mButtonState;
		}
	}

	@Override
	protected LocalBinder getBinder() {
		return mBinder;
	}

	@Override
	protected BleManager<HrmManagerCallbacks> initializeManager() {
		return mManager = new HrmManager(this);
	}

	@Override
	public void onDataReceived(final boolean state) {
		mBinder.mButtonState = state;
		final Intent broadcast = new Intent(BROADCAST_BATTERY_LEVEL_CHANGED);
		broadcast.putExtra(EXTRA_BATTERY_LEVEL, state);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	/**
	 * Called when hrm value has been received from the device.
	 *
	 * @param value
	 *            the hrm value in percent
	 */
	public void onHrmValueReceived(final int value) {
		// mBinder.mSensorState = true;
		final Intent broadcast = new Intent(BROADCAST_HRM_VALUE_CHANGED);
		broadcast.putExtra(EXTRA_HRM_VALUE, value);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onDataSent(boolean state) {
		mBinder.mSensorState = state;
		final Intent broadcast = new Intent(BROADCAST_SENSOR_STATE_CHANGED);
		broadcast.putExtra(EXTRA_DATA, state);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}
}
