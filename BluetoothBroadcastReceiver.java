package bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class BluetoothBroadcastReceiver extends BroadcastReceiver
{
	public static final String TAG = "BluetoothBroadcastReceiver";
	public static final boolean DEBUG = true;
	
	private final BluetoothBroadcastReceiverListener listener;
	private final IntentFilter filter;
	private final Context context;
	
	public BluetoothBroadcastReceiver(Context context, BluetoothBroadcastReceiverListener listener)
	{
		super();
		this.listener = listener;
		filter 	= new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		//filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		this.context = context;  
	}
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		if(DEBUG) Log.d(TAG, "onReceive()");
		
		int state = -1;
		
		String action = intent.getAction();
		
		final BluetoothAdapter	btAdapter = BluetoothAdapter.getDefaultAdapter();
		if(action == BluetoothAdapter.ACTION_STATE_CHANGED)
			state = btAdapter.getState();
		else if(action == BluetoothDevice.ACTION_ACL_CONNECTED)
			state = BluetoothAdapter.STATE_CONNECTED;
		else if(action == BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)
			state = BluetoothAdapter.STATE_DISCONNECTING;
		else if(action == BluetoothDevice.ACTION_ACL_DISCONNECTED)
			state = BluetoothAdapter.STATE_DISCONNECTED;
		
		if(DEBUG) Log.d(TAG, "onReceive(): " + state);
		
		listener.bluetoothBroadcastReceived(state);
	}
	
	public void register()
	{
		context.registerReceiver(this, filter);
	}
	
	public void unregister()
	{
		context.unregisterReceiver(this);
	}

	
	public interface BluetoothBroadcastReceiverListener
	{
		void bluetoothBroadcastReceived(int state);
	}
}
