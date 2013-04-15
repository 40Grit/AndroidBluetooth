package bluetooth;

import java.util.ArrayList;

import Ultrasonic.Controller.MainActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

public class DeviceDialog extends DialogFragment implements OnClickListener
{
	private Activity activity;
	private ArrayAdapter<String> deviceListAdapter;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		activity = getActivity();
		
		//Setup ArrayAdapter to hold the name and MAC's of each paired device
		//Use android's default list item 
		deviceListAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_selectable_list_item);
		
		ArrayList<String> deviceList = BluetoothConnection.getPairedDeviceList();

		for (String device : deviceList)
		{
			deviceListAdapter.add(device);
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		builder.setTitle("Select A Generator");
		builder.setAdapter(deviceListAdapter, this);
		return builder.create();
	}
	
	
	DeviceDialogListener deviceDialogListener;
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		
		try
		{
			deviceDialogListener = (DeviceDialogListener) activity;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + "Class doesn't implement DeviceDialogListener");
		}
	}
	

	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		String deviceString = deviceListAdapter.getItem(which);
		
		String mac = deviceString.substring(deviceString.length() - 17);
		
		deviceDialogListener.onDeviceSelected(mac);
	}
	
	public interface DeviceDialogListener
	{
		public void onDeviceSelected(String mac);
	}
	
}
