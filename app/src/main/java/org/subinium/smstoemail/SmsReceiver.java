package org.subinium.smstoemail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import co.nedim.maildroidx.MaildroidX;
import co.nedim.maildroidx.MaildroidXType;

public class SmsReceiver extends BroadcastReceiver {

    private static final DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    @Override
    public void onReceive(Context ctx, Intent intent) {

        String action = intent.getAction();
        Bundle bundle = intent.getExtras();

        if (bundle == null || action == null) {

            return;
        }

        if (action.equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {

            Log.d("S2E", "SMS Alındı");

            // Original processing of SMS.
            Object[] allPdus = (Object[]) bundle.get("pdus");
            String format = bundle.getString("format");

            if (allPdus != null) {

                Set<String> senderSet = new HashSet<String>();
                StringBuilder bodyBuilder = new StringBuilder();
                long minTime = Long.MAX_VALUE;
                for (int i = 0; i < allPdus.length; i++) {

                    SmsMessage smsMessage = createMessage(allPdus[i], format);
                    if (smsMessage == null) {

                        continue;
                    }

                    String sender = smsMessage.getOriginatingAddress();
                    if (sender != null) {

                        senderSet.add(smsMessage.getOriginatingAddress());
                    }

                    String body = smsMessage.getMessageBody();
                    bodyBuilder.append(body);

                    long thisTime = smsMessage.getTimestampMillis();
                    if (thisTime < minTime) {

                        minTime = thisTime;
                    }
                }
                Log.d("1A", "RETROSTART");
                sendEmail(ctx, TextUtils.join(",", senderSet), bodyBuilder.toString(), getFormattedTime(minTime));
            }
        }
    }

    private SmsMessage createMessage(Object pdu, String format) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            return SmsMessage.createFromPdu((byte[]) pdu, format);
        } else {

            return SmsMessage.createFromPdu((byte[]) pdu);
        }
    }

    private String getFormattedTime(long timestamp) {

        return DEFAULT_DATE_FORMAT.format(new Date(timestamp));
    }

    private void sendEmail(Context ctx, String sender, String body, String time) {

        Log.d("S2E", "From: " + sender);

        PrefManager pm = PrefManager.getInstance(ctx);
        String email = pm.getUsername();

        MaildroidX.Builder b = new MaildroidX.Builder()
                .smtp(pm.getServer())
                .smtpUsername(email)
                .smtpPassword(pm.getPassword())
                .smtpAuthentication(true)
                .port(pm.getPort())
                .type(MaildroidXType.PLAIN)
                .to(email)
                .from(email)
                .subject("VPN Pass: " + sender + " [" + body + "] ")
                .body("\n" + body + "\n\nSifre " + sender + " tarafindan " + time + " tarihinde iletildi. \nBu mesaj nobet telefonu tarafindan otomatik olarak yonlendirildi." )
                .onCompleteCallback(new MaildroidX.onCompleteCallback() {
                    @Override
                    public void onSuccess() {

                        Log.d("S2E", "Email başarılı");
                    }

                    @Override
                    public void onFail(String s) {

                        Log.d("S2E", "Email hatası");
                    }

                    @Override
                    public long getTimeout() {

                        return 6000; // 1 minute
                    }
                });
        b.mail();
    }
}
