package com.prize.factorytest.OTG;

import com.prize.factorytest.R;
import com.prize.factorytest.PrizeFactoryTestListActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.hardware.usb.UsbManager;
import com.mediatek.storage.StorageManagerEx;
import android.os.storage.StorageVolume;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.os.Environment;


import android.os.storage.StorageEventListener;
import android.os.storage.VolumeInfo;
import android.os.storage.VolumeRecord;
import android.os.storage.DiskInfo;
import android.hardware.usb.UsbDevice;
import java.util.HashMap;
import android.hardware.usb.UsbDeviceConnection;
import java.util.Iterator;
import android.hardware.input.InputManager;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbConstants;
import android.view.InputDevice;
public class OTG extends Activity {
	
	private TextView mTextView;
	private StorageManager mStorageManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.otg);
		mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
		//mStorageManager.registerListener(mListener);
		mTextView = (TextView) findViewById(R.id.otg_hint);
		confirmButton(getString(R.string.otg_insert), false);
		//showOtgStorage();
		UDRegisterReceiver();//add by zhangrui
	}

	
	private final StorageEventListener mListener = new StorageEventListener() {
        @Override
        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            onVolumeStateChangedInternal(vol);
        }
		
		@Override
        public void onVolumeRecordChanged(VolumeRecord rec) {
  
        }

        @Override
        public void onVolumeForgotten(String fsUuid) {

        }

        @Override
        public void onDiskScanned(DiskInfo disk, int volumeCount) {

        }

        @Override
        public void onDiskDestroyed(DiskInfo disk) {

        }
    };
	
	private void onVolumeStateChangedInternal(VolumeInfo vol) {
		if(vol.getType() == VolumeInfo.TYPE_PUBLIC){
			if(vol.getState() == VolumeInfo.STATE_CHECKING){
				confirmButton(getString(R.string.otg_checking), false);
			}
			if((vol.getState() == VolumeInfo.STATE_MOUNTED) || (vol.getState() == VolumeInfo.STATE_MOUNTED_READ_ONLY)){
				showOtgStorage(vol);
			}
		}
    }
	
	private void showOtgStorage(VolumeInfo vol) {
        final VolumeRecord rec = mStorageManager.findRecordByUuid(vol.getFsUuid());
        final DiskInfo disk = vol.getDisk();

        if (rec.isSnoozed() && disk.isAdoptable()) {
            return;
        }
        if (disk.isAdoptable() && !rec.isInited()) {

        } else {
			final CharSequence title = disk.getDescription();
			confirmButton(title.toString(), true);
        }
    }
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	public void confirmButton(String title, boolean enable) {
		mTextView.setText(title);
		mTextView.setTextSize(20);
		final Button buttonPass = (Button) findViewById(R.id.passButton);
		buttonPass.setEnabled(enable);
		final Button buttonFail = (Button) findViewById(R.id.failButton);
		buttonPass.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
					PrizeFactoryTestListActivity.itempos++;
				}
				setResult(RESULT_OK);
				finish();
			}
		});
		buttonFail.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
					PrizeFactoryTestListActivity.itempos++;
				}
				setResult(RESULT_CANCELED);
				finish();

			}

		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(usbStateReceiver);
	}
	
	//add by zhangrui start
	 public void UDRegisterReceiver() {
	   IntentFilter filter = new IntentFilter();
       filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
       filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
       filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
       filter.addAction("android.hardware.usb.action.USB_STATE");
       registerReceiver(usbStateReceiver, filter);
	 }
	
	private final BroadcastReceiver usbStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED) || action.equals(UsbManager.ACTION_USB_ACCESSORY_ATTACHED) || action.equals("android.hardware.usb.action.USB_STATE")) {
				String USBNamer = isConnectUSBDevice();
				if(USBNamer != null){
                 confirmButton(USBNamer, true);
				} 
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                confirmButton(getString(R.string.otg_insert), false);
            }
        }
    };
	
	
	
	public String isConnectUSBDevice() {
        String deviceNamer = null;
		UsbManager mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        if (deviceList.size() == 0) {
            return deviceNamer;
        }
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        if (deviceIterator.hasNext()) {
            UsbDevice device = (UsbDevice) deviceIterator.next();
            deviceNamer = device.getManufacturerName()+" "+device.getProductName();
            /*Log.d("zhangrui", "Name: " + device.getDeviceName()+"\n"
                     + "VID: " + device.getVendorId()
                                + "       PID: " + device.getProductId()+"getProductName="+device.getProductName()+"getManufacturerName="+device.getManufacturerName());*/
        }
        return deviceNamer;
    }  
	//add by zhangrui end
	
}
