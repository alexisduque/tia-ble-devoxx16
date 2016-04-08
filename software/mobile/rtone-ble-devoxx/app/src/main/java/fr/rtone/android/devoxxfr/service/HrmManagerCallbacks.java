/**  * Created by Alexis DUQUE - alexisd61@gmail.com.  * Date : 05/04/16.  */

package fr.rtone.android.devoxxfr.service;


import fr.rtone.android.devoxxfr.profile.BleManagerCallbacks;

public interface HrmManagerCallbacks extends BleManagerCallbacks {

	/**
	 * Called when a button was pressed or released on device
	 * @param state true if the button was pressed, false if released
	 */
	void onDataReceived(final boolean state);

	/**
	 * Called when the data has been sent to the connected device.
	 * @param state true when LED was enabled, false when disabled
	 */
	void onDataSent(final boolean state);

	/**
	 * Called when hrm value has been received from the device.
	 *
	 * @param value
	 *            the hrm value in percent
	 */
	public void onHrmValueReceived(final int value);
}
