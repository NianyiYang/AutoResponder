package com.yny.autoresponder.texts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;

import com.yny.autoresponder.TxtMsgSender;

import java.util.ArrayList;

public class TxtMsgReceiver extends BroadcastReceiver {

	private TxtMsgSender msgSender;

	public TxtMsgReceiver(TxtMsgSender msgSender) {
		this.msgSender = msgSender;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
			answerToIncomingMessages(intent);
		}
	}

	private void answerToIncomingMessages(Intent intent) {
		SmsMessage[] msgs = getSmsMessagesFromIntent(intent);
		String[] numbers = getAuthorsNumber(msgs);
		sendMessagesToAuthors(numbers);
	}

	private SmsMessage[] getSmsMessagesFromIntent(Intent intent) {
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		
		if (bundle != null) {
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];
			for(int i = 0; i < msgs.length; i++) {
				byte[] pdu = (byte[]) pdus[i];
				SmsMessage msg = SmsMessage.createFromPdu(pdu);
				msgs[i] = msg;
			}
		}
		
		return msgs;
	}

	private String[] getAuthorsNumber(SmsMessage[] msgs) {
		ArrayList<String> phoneNumbers = new ArrayList<String>();
		for (SmsMessage message : msgs) {
			if(!message.isEmail()) {
				String number = message.getDisplayOriginatingAddress();
				phoneNumbers.add(number);
			}
		}
		return phoneNumbers.toArray(new String[] {});
	}
	
	private void sendMessagesToAuthors(String[] numbers) {
		for (String number : numbers) {
			msgSender.sendTextMessageIfPossible(number);
		}
	}
}
