package com.RWTech.Freedom.mini.vpn;

import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import com.RWTech.Freedom.service.TorService;
import com.RWTech.Freedom.service.TorServiceConstants;
import com.RWTech.Freedom.service.util.Prefs;
import com.RWTech.Freedom.service.vpn.TorVpnService;

/**
 * To combat background service being stopped/swiped
 */
public class VPNEnableActivity extends AppCompatActivity {
	
	private final static int REQUEST_VPN = 7777;
	private	Intent intent = null;
	private boolean checkVpn = true;
	private Handler h = new Handler();
	
	@Override
	public void onCreate(Bundle icicle ) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate( icicle );

		Log.d("VPNEnableActivity","prompting user to start Freedom VPN");
	}
	
	public void onResume() {
		super.onResume();
		
		if (checkVpn)
		{
			intent = VpnService.prepare(this);
			
			if (intent != null)
				promptStartVpnService();
			else
				startVpnService ();
			
			checkVpn = false;
		}
	}
	
	public void promptStartVpnService () {
		// todo no actual prompting happens here and this should be refactored
		startVpnService();
    }
	 
	private void startVpnService ()
	{
   		if (intent == null)
   		{
			Prefs.putUseVpn(true);

			Log.d("VPNEnableActivity","VPN enabled, starting Tor...");
			TorVpnService.start(this);
            
            Handler h = new Handler();
            h.postDelayed(new Runnable () {
            	
            	public void run ()
            	{
            		sendIntentToService(TorServiceConstants.ACTION_START);		
            		finish();
            	}
            }, 100);


   		}
   		else
   		{
   			Log.w("VPNEnableActivity","prompt for VPN");
            startActivityForResult(intent,REQUEST_VPN);

   		}

	}

	public static final int ACTIVITY_RESULT_VPN_DENIED = 63;

	@Override
	protected void onActivityResult(int request, int response, Intent data) {
		super.onActivityResult(request, response, data);
	        
		if (request == REQUEST_VPN && response == RESULT_OK) {
			TorVpnService.start(this);

			h.postDelayed(new Runnable () {
	            	@Override
	            	public void run () {
	            		sendIntentToService(TorServiceConstants.ACTION_START);
	            		finish();
	            	}
	            }, 1000);
		}
		else if (request == REQUEST_VPN && response == RESULT_CANCELED) {
			setResult(ACTIVITY_RESULT_VPN_DENIED);
			finish();
		}
	  }
	  

		private void sendIntentToService(String action) {
			Intent torService = new Intent(this, TorService.class);    
			torService.setAction(action);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				startForegroundService(torService);
			}
			else
			{
				startService(torService);
			}
		}
}
