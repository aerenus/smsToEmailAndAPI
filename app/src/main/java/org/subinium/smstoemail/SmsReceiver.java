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
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import co.nedim.maildroidx.MaildroidX;
import co.nedim.maildroidx.MaildroidXType;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class SmsReceiver extends BroadcastReceiver {

    private static final DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    ArrayList<APIModel> apiModels;
    private String BASE_URL = "https://edk.univera.com.tr:8443/mobile/";
    Retrofit retrofit;
    CompositeDisposable compositeDisposable;

    @Override
    public void onReceive(Context ctx, Intent intent) {

        String action = intent.getAction();
        Bundle bundle = intent.getExtras();

        Gson gson = new GsonBuilder().setLenient().create();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

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

        Boolean found;
        String smsText = sender + " [" + body + "] ";
        if(sender.contains("VPN")) {
            APIInterface apiModel = retrofit.create(APIInterface.class);
            Call<List<APIModel>> call = apiModel.getData(smsText);
            call.enqueue(new Callback<List<APIModel>>() {
                @Override
                public void onResponse(Call<List<APIModel>> call, Response<List<APIModel>> response) {
                    Log.d("SmsReceiver", "OnResponse returned.\"");
                }

                @Override
                public void onFailure(Call<List<APIModel>> call, Throwable t) {
                    Log.d("SmsReceiver", "OnFail returned.\"");
                    t.printStackTrace();
                }

            });

        }








    }
}
