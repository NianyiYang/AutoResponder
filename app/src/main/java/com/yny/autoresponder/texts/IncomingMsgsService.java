package com.yny.autoresponder.texts;

import android.content.Context;
import android.content.IntentFilter;

import com.yny.autoresponder.TxtMsgSender;

public class IncomingMsgsService {
	private Context mCtx;
	
	private TxtMsgReceiver txtReceiver;

	public static boolean isActive = false;

	public IncomingMsgsService(Context mCtx) {
		this.mCtx = mCtx;
	}

	public void register() {
		TxtMsgSender msgSender = TxtMsgSender.createAndSetUp(mCtx);
		txtReceiver = new TxtMsgReceiver(msgSender);
		IntentFilter incomingTxtFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		mCtx.registerReceiver(txtReceiver, incomingTxtFilter);
		isActive = true;
	}
	
	public void unregister() {
		try {
			mCtx.unregisterReceiver(txtReceiver);
		} catch (IllegalArgumentException e) { 
			// Do nothing
		} finally { 
			isActive = false;
		}
	}
}
