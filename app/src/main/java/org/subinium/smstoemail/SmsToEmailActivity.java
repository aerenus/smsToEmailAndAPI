package org.subinium.smstoemail;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SmsToEmailActivity extends AppCompatActivity {

    private static final String SMS_PERMISSION_NAME = "android.permission.RECEIVE_SMS";
    private static final int SMS_PERMISSION_REQ_CODE = 1;

    ArrayList<APIModel> apiModels;
    private String BASE_URL = "https://edk.univera.com.tr:8443/mobile/";
    Retrofit retrofit;
    CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms2email);

        processLayout();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            Log.d("S2E", "İzin gerekli");
            if (checkCallingOrSelfPermission(SMS_PERMISSION_NAME) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{SMS_PERMISSION_NAME}, SMS_PERMISSION_REQ_CODE);
            }
        }


        Gson gson = new GsonBuilder().setLenient().create();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Note: From https://developer.android.com/reference/android/app/Activity
        // It is possible that the permissions request interaction with the user is interrupted.
        // In this case you will receive empty permissions and results arrays which should be treated as a cancellation.

        boolean granted = false;

        if (permissions != null && permissions.length > 0 && permissions[0] != null && permissions[0].equals(SMS_PERMISSION_NAME)) {

            granted = grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }

        if (granted) {

            Log.d("S2E", "İzin verildi");
            Toast.makeText(this, "SMS okuma izni verildi", Toast.LENGTH_LONG).show();
        } else {

            Log.d("S2E", "İzin reddedildi");
            Toast.makeText(this, "SMS okuma reddedildi", Toast.LENGTH_LONG).show();
        }
    }

    private void processLayout() {

        final EditText etServer = findViewById(R.id.et_server);
        final EditText etUsername = findViewById(R.id.et_username);
        final EditText etPassword = findViewById(R.id.et_password);
        final EditText etPort = findViewById(R.id.et_port);
        final EditText etAPI = findViewById(R.id.et_api);

        PrefManager pm = PrefManager.getInstance(this);
        etServer.setText(pm.getServer());
        etUsername.setText(pm.getUsername());
        etPassword.setText(pm.getPassword());
        etPort.setText(pm.getPort());
        etAPI.setText(pm.getAPI());

        final Button b = findViewById(R.id.b_save);
        b.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
        b.setText("KAYDEDİLİYOR...");
                PrefManager pm = PrefManager.getInstance(getApplicationContext());
                pm.setServer(etServer.getText().toString());
                pm.setUsername(etUsername.getText().toString());
                pm.setPassword(etPassword.getText().toString());
                pm.setPort(etPort.getText().toString());
                pm.setAPI(etAPI.getText().toString());
                b.setText("KAYDEDİLDİ");
            }
        });
    }
}
