package com.RWTech.Freedom;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.RWTech.Freedom.service.TorService;
import com.RWTech.Freedom.service.TorServiceConstants;
import com.RWTech.Freedom.service.util.Prefs;
import com.RWTech.Freedom.ui.VPNEnableActivity;

public class OnBootReceiver extends BroadcastReceiver {

	private static boolean sReceivedBoot = false;

	@Override
	public void onReceive(Context context, Intent intent) {

		if (Prefs.startOnBoot() && (!sReceivedBoot))
		{
			if (Prefs.useVpn())
				startVpnService(context); //VPN will start Tor once it is done
			else
				startService(TorServiceConstants.ACTION_START, context);

			sReceivedBoot = true;
		}
	}
	
	public void startVpnService (final Context context)
    	{
		   Intent intent = new Intent(context,VPNEnableActivity.class);
           intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
           context.startActivity(intent);

    	}

	private void startService (String action, Context context)
	{
		
		Intent torService = new Intent(context, TorService.class);
		torService.setAction(action);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			context.startForegroundService(torService);
		}
		else
		{
			context.startService(torService);
		}

	}
	
	
}

