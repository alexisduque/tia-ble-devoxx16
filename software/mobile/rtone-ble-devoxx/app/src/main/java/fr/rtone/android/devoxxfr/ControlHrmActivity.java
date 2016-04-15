/**
 * Created by Alexis DUQUE - alexisd61@gmail.com.
 * Date : 05/04/16.
 */

package fr.rtone.android.devoxxfr;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import fr.rtone.android.devoxxfr.service.HrmService;
import fr.rtone.android.devoxxfr.profile.BleProfileService;

public class ControlHrmActivity extends AppCompatActivity {
	private HrmService.HrmBinder mHrmDevice;
	private Button mActionOnOff, mActionConnect;
	private ImageView mImageHeart;
	private TextView mHrmText;
	private View mParentView;
	private View mBackgroundView;
	private Toast mToast;

	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mHrmDevice = (HrmService.HrmBinder) service;

			if (mHrmDevice.isConnected()) {
				mActionConnect.setText(getString(R.string.action_disconnect));
				mActionConnect.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGreen ));


				if (mHrmDevice.isOn()) {
					mImageHeart.setImageDrawable(ContextCompat.getDrawable(ControlHrmActivity.this, R.drawable.heart_rate_red));
					mActionOnOff.setText(getString(R.string.turn_off));
				} else {
					mImageHeart.setImageDrawable(ContextCompat.getDrawable(ControlHrmActivity.this, R.drawable.heart_rate_red_off));
					mActionOnOff.setText(getString(R.string.turn_on));
					mHrmText.setText("");
				}

				if (mHrmDevice.isButtonPressed()) {
					mBackgroundView.setVisibility(View.VISIBLE);
				} else {
					mBackgroundView.setVisibility(View.INVISIBLE);
				}
			} else {
				mActionConnect.setText(getString(R.string.action_connect));
				mActionConnect.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.common_signin_btn_default_background));
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mHrmDevice = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_control_device);

		Intent i = getIntent();
		final String deviceName = i.getStringExtra(HrmService.EXTRA_DEVICE_NAME);
		final String deviceAddress = i.getStringExtra(HrmService.EXTRA_DEVICE_ADDRESS);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle(deviceName);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mActionOnOff = (Button) findViewById(R.id.button_hrm);
		mActionConnect = (Button) findViewById(R.id.action_connect);
		mImageHeart = (ImageView) findViewById(R.id.img_hrm);
		mBackgroundView = findViewById(R.id.background_view);
		mParentView = findViewById(R.id.relative_layout_control);
		mHrmText = (TextView) findViewById(R.id.hrmText);

		mActionOnOff.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				/*** TIA  STEP 5 Enable Notification */
				if (mHrmDevice != null && mHrmDevice.isConnected()) {
					if (mActionOnOff.getText().equals(getString(R.string.turn_on))) {
						mHrmDevice.send(true);
					} else {
						mHrmDevice.send(false);
					}
				} else {
					showError(getString(R.string.please_connect));
				}
			}
		});

		LocalBroadcastManager.getInstance(this).registerReceiver(mHrmUpdateReceiver, makeGattUpdateIntentFilter());

		final Intent intent = new Intent(this, HrmService.class);
		intent.putExtra(HrmService.EXTRA_DEVICE_ADDRESS, deviceAddress);
		startService(intent);
		bindService(intent, mServiceConnection, 0);

		mActionConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mHrmDevice != null && mHrmDevice.isConnected()) {
					mHrmDevice.disconnect();
				} else {
					startService(intent);
					bindService(intent, mServiceConnection, 0);
				}
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (mHrmDevice != null && mHrmDevice.isConnected())
			mHrmDevice.disconnect();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mServiceConnection);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mHrmUpdateReceiver);

		mServiceConnection = null;
		mHrmDevice = null;
	}

	private BroadcastReceiver mHrmUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			switch (action) {
				case HrmService.BROADCAST_SENSOR_STATE_CHANGED: {
					final boolean flag = intent.getBooleanExtra(HrmService.EXTRA_DATA, false);
					if (flag) {
						mImageHeart.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.heart_rate_red));
						mActionOnOff.setText(getString(R.string.turn_off));
					} else {
						mImageHeart.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.heart_rate_red_off));
						mActionOnOff.setText(getString(R.string.turn_on));
						mHrmText.setText("");
					}
					break;
				}
				case HrmService.BROADCAST_CONNECTION_STATE: {
					final int value = intent.getIntExtra(HrmService.EXTRA_CONNECTION_STATE, HrmService.STATE_DISCONNECTED);
					switch (value) {
						case BleProfileService.STATE_CONNECTED:
							mActionConnect.setText(getString(R.string.action_disconnect));
							mActionConnect.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGreen));
							break;
						case BleProfileService.STATE_DISCONNECTED:
							mActionConnect.setText(getString(R.string.action_connect));
							mActionConnect.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.common_signin_btn_default_background));
							mActionOnOff.setText(getString(R.string.turn_on));
							mHrmText.setText("");
							mImageHeart.setImageDrawable(ContextCompat.getDrawable(ControlHrmActivity.this, R.drawable.heart_rate_red_off));
							break;
					}
					break;
				}
				case HrmService.BROADCAST_ERROR: {
					final String message = intent.getStringExtra(HrmService.EXTRA_ERROR_MESSAGE);
					final int code = intent.getIntExtra(HrmService.EXTRA_ERROR_CODE, 0);
					showError(getString(R.string.error_msg, message, code));
					break;
				}
				case HrmService.BROADCAST_BATTERY_LEVEL: {
					final int message = intent.getIntExtra(HrmService.EXTRA_BATTERY_LEVEL, 0);
					showToast("Battery Level : " + message + " %");
					break;
				}
				case HrmService.BROADCAST_HRM_VALUE_CHANGED: {
					final int message = intent.getIntExtra(HrmService.EXTRA_HRM_VALUE, 0);
					mHrmText.setText(message + " bpm");
					break;
				}

			}
		}
	};

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(HrmService.BROADCAST_SENSOR_STATE_CHANGED);
		intentFilter.addAction(HrmService.BROADCAST_HRM_VALUE_CHANGED);
		intentFilter.addAction(HrmService.BROADCAST_CONNECTION_STATE);
		intentFilter.addAction(HrmService.BROADCAST_BATTERY_LEVEL);
		intentFilter.addAction(HrmService.BROADCAST_ERROR);
		return intentFilter;
	}

	private void showError(final String error) {
		Snackbar.make(mParentView, error, Snackbar.LENGTH_LONG).show();
	}

	private void showToast(final String message) {
		if (mToast == null) {
			mToast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
		} else {
			mToast.setText(message);
		}
		mToast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 195);
		mToast.show();
	}
}
