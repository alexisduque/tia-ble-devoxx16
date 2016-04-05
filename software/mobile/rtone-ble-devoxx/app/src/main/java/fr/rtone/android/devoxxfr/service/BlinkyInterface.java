/**  * Created by Alexis DUQUE - alexisd61@gmail.com.  * Date : 05/04/16.  */

package fr.rtone.android.devoxxfr.service;

/**
 * The Blinky device interface. The blinky device will also send notifications whenever a button was pressed.
 */
public interface BlinkyInterface {
	/**
	 * Sends the LED state to the target device.
	 * @param onOff the new state. True to enable LED, false to disable it.
	 */
	void send(final boolean onOff);
}
