package bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public class EnableBluetoothDialog extends DialogFragment implements OnClickListener
{
	private Activity activity;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		activity = getActivity();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setPositiveButton("Yes", this);
		builder.setNegativeButton("No", this);		
		builder.setTitle("Enable Bluetooth?");

		return builder.create();
	}
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		if(which == Dialog.BUTTON_POSITIVE)
			BluetoothAdapter.getDefaultAdapter().enable();
	}
	
	
	
}
