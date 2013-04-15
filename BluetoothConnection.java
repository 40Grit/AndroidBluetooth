package bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import bluetooth.BluetoothBroadcastReceiver.BluetoothBroadcastReceiverListener;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * BluetoothConnection opens RFCOMM sockets.
 * Sockets can be opened as client or host.
 * <P>
 * Low level ACL broadcasts, and adapter power state broadcasts 
 * are received and passed through the given handler.
 * <P>
 * unregister and register the receiver with the contect activities lifecycle accordingly
 * enableBroadcast(), disableBroadcast()
 * <P>
 * Ensure bluetooth adapter exists and is enabled before instantiating
 * isAdapter() can be used to confirm adapters existence
 * DeviceDialog in the Bluetooth package can be used to enable Bluetooth
 *
 * @author      Grit 
 * @version     1.0
 * @since       2012-10-01
 */
public final class BluetoothConnection implements BluetoothBroadcastReceiverListener
{
	public static final String TAG = "BluetoothConnector";
	public static final boolean DEBUG = true;
	
	public static final int STATE_DISCONNECTING = BluetoothAdapter.STATE_DISCONNECTING;
	public static final int STATE_DISCONNECTED = BluetoothAdapter.STATE_DISCONNECTED;	
	public static final int STATE_CONNECTING = BluetoothAdapter.STATE_CONNECTING;
	public static final int STATE_CONNECTED = BluetoothAdapter.STATE_CONNECTED;
	public static final int STATE_TURNING_ON = BluetoothAdapter.STATE_TURNING_ON;
	public static final int STATE_TURNING_OFF = BluetoothAdapter.STATE_TURNING_OFF;
	public static final int STATE_ON = BluetoothAdapter.STATE_ON;
	public static final int STATE_OFF= BluetoothAdapter.STATE_OFF;
	
	
	public static final	UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	private final BluetoothAdapter 	btAdapter = BluetoothAdapter.getDefaultAdapter();
	private final Handler btHandler;
	private final BluetoothBroadcastReceiver broadcastReceiver;
	
	private final BluetoothSocket 	btSocket;
	private final ConnectThread connectThread;
	
	/**
	 * Create new BluetoothConnection as host
	 *
	 * @param handler a handler to send connection states through
	 * @param context give the context of the instantiating activity? don't understand context well enough to give good description.  
	 * @param the amount of time to listen before connectThread.run() returns. <<wordsmith this
	 * @return Constructor
	 */
	public BluetoothConnection(Handler handler, Context context, final int timeout) throws IOException
	{
		Log.d(TAG,"constructor");
			
		btHandler 		  = handler;
		broadcastReceiver = new BluetoothBroadcastReceiver(context, this);
		
		connectThread = null;
		
		final BluetoothServerSocket btServerSocket = 
					btAdapter.listenUsingInsecureRfcommWithServiceRecord(btAdapter.getName() ,uuid);
		
		class ListenThread extends Thread
		{	
			BluetoothSocket tempSocket;
			public void run()
			{
				Log.d(TAG, "ListenThread.run()");
				try
				{
					tempSocket = 	btServerSocket.accept(timeout);
				} 
				catch (IOException e)
				{
					Log.e(TAG, "Listen Timed out: " + Integer.toString(timeout));
					e.printStackTrace();
				}
			}
		}
		
		ListenThread listenThread = new ListenThread();
		btSocket = listenThread.tempSocket;
	}
	
	
	/**
	 * Create new BluetoothConnection as client
	 *
	 * @param handler a handler to send connection states through
	 * @param context give the context of the instantiating activity? don't understand context well enough to give good description.
	 * @param macAddress the mac address of the SPP device to connect to  
	 * @return Constructor
	 */
	public BluetoothConnection(Handler handler, Context context, String macAddress) throws IOException 
	{
		Log.d(TAG,"constructor");
		btHandler 		= handler;
		broadcastReceiver = new BluetoothBroadcastReceiver(context, this);
		broadcastReceiver.register();
		
		final BluetoothDevice btDevice = btAdapter.getRemoteDevice(macAddress);
		btSocket = btDevice.createInsecureRfcommSocketToServiceRecord(uuid);
		connectThread = new ConnectThread(macAddress);
		connectThread.start();
	}
	
	/**
	 * Check for default adapters existence
	 *   
	 * @return boolean
	 */
	public static boolean isAdapter()
	{
		return (BluetoothAdapter.getDefaultAdapter() != null) ? (true):(false);
	}


	/**
	 * Check that bluetooth adapter is enabled
	 *   
	 * @return boolean
	 */
	public static boolean isBluetoothEnabled()
	{
		return BluetoothAdapter.getDefaultAdapter().isEnabled();
	}
	

	/**
	 * send a connection state to the handler
	 * @param state the state to send  
	 * @return void
	 */
	private synchronized void sendState(int state)
	{
		Message msg = btHandler.obtainMessage(state);
		btHandler.sendMessage(msg);
	}
	

	/**
	 * callback for BluetoothBroadcastReceiver
	 *   
	 * @return void
	 */
	@Override
	public synchronized void bluetoothBroadcastReceived(int state)
	{
		if(DEBUG) Log.d(TAG, "Broadcast Received: " + Integer.toString(state));
		sendState(state);
	}
	
	

	/**
	 * Get the InputStream for the (hopefully) connected btSocket
	 *   
	 * @return InputStream
	 */
	public synchronized InputStream getInputStream()
	{	
		Log.d(TAG,"getting input stream for: " +btSocket.getRemoteDevice().getName());
		try
		{
			return btSocket.getInputStream();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	

	/**
	 * Get the OutputStream for the (hopefully) connected btSocket
	 *   
	 * @return OutputStream
	 */
	public synchronized OutputStream getOutputStream()
	{	
		Log.d(TAG,"getting output stream for: " +btSocket.getRemoteDevice().getName());
		try
		{
			return btSocket.getOutputStream();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
		

	/**
	 * Open btSocket using provided mac address
	 *   
	 * @return InputStream
	 */
	private class ConnectThread extends Thread
	{		
		ConnectThread(String mac)
		{
			Log.d(TAG,"Connect Thread constructor");
		}
		
		public void run()
		{		
			Log.d(TAG,"Attempting to connect");
			//get bluetooth device from provided macaddress get a bluetooth rfcomm socket from device
			
			try
			{
				btSocket.connect();
				sendState(STATE_CONNECTED);
			} 
			catch(IOException e) 
			{
				Log.e(TAG,"Could Not Connect");
				e.printStackTrace();
			}
		}
	}	
	
	

	/**
	 * Get an arrayList of the devices currently paired to the default adapter
	 *   
	 * @return ArrayList<String> of paired devices
	 */
	public static ArrayList<String> getPairedDeviceList()
	{
		//TODO: Ensure when DefaultAdapter isn't found that exception is thrown
		Set <BluetoothDevice> pairedDevices = BluetoothAdapter
												.getDefaultAdapter()
													.getBondedDevices();
		
		//FIXME: Maybe change order of operation
		ArrayList <String> deviceStrings = new ArrayList<String>();
		if(pairedDevices.size()>0)
        {
        	for(BluetoothDevice device:pairedDevices)
        	{
        		deviceStrings.add(device.getName() + "\n" + device.getAddress());
        	}
        }
		return deviceStrings;
	}
	

	/**
	 * close the (hopefully) connected btSocket
	 * Always call for old connection before getting a new BluetoothConnection  
	 * @return void
	 */
	public synchronized void disconnect()
	{
		try
		{
			btSocket.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}


	/**
	 * enable the BluetoothBroadcastReceiver
	 * Use in onRestart() and other relevant locations of your activity  
	 * @return void
	 */
	public void enableBroadcast()
	{
		broadcastReceiver.register();
	}
	

	/**
	 * Disable the BluetoothBroadcastReceiver
	 * Use in onPause() and other relevant locations of your activity  
	 * @return InputStream
	 */
	public void disableBroadcast()
	{
		broadcastReceiver.unregister();	
	}

}
