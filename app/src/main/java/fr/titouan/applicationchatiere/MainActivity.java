package fr.titouan.applicationchatiere;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import java.nio.charset.Charset;
import java.util.Set;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    // TAG permettant de savoir dans quelle méthode on se trouve, dans les logs
    private static final String TAG = "MainActivity";

    // Représente l'appareil Bluetooth
    BluetoothAdapter mBluetoothAdapter;

    //
    BluetoothConnectionService mBluetoothConnection;

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Nom du composant Bluetooth Arduino
    private static final String NOM_COMPOSANT_BT_ARDUINO = "DSD TECH HC-05";

    // Code pour verrouiller
    private static final String DEVERROUILLER = "1";

    // Code pour déverrouiller
    private static final String VERROUILLER = "0";

    Button btnVerrouiller;// on définit un bouton verouiller
    Button btnDeverrouiller;// on définit un bouton deverouiller
    String descriptionTemps;// on définit une variable descriptiontemps

// on détruit la connection bluetooth lorsque la connection se termine
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        mBluetoothConnection.stop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        enableBT();
        final ImageView monImage = findViewById(R.id.imageView);
        final ImageView meteoImage = findViewById(R.id.imageView2);
        final TextView monText = findViewById(R.id.textView);
        monImage.setImageResource(R.drawable.chatiere);
        meteoImage.setImageResource(R.drawable.i50d);


        Set<BluetoothDevice> mesDevices = mBluetoothAdapter.getBondedDevices();
        BluetoothDevice monDevice = null;
        for (BluetoothDevice device : mesDevices) {
            Log.d(TAG, device.getAddress());
            Log.d(TAG, device.getName());
            if (device.getName().equals(NOM_COMPOSANT_BT_ARDUINO)) {
                monDevice = device;
            }
        }

        //APPID c9afb749c4ce94e522e9668bae6be96c
        // http://api.openweathermap.org/data/2.5/weather?q=trets&appid=c9afb749c4ce94e522e9668bae6be96c
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://api.openweathermap.org/data/2.5/weather?q=trets&appid=c9afb749c4ce94e522e9668bae6be96c&units=metric", new TextHttpResponseHandler() {

            @Override
            public void onStart() {
            // called before request is started
            }


            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
// called when response HTTP status is "200 OK"
                Gson gson = new GsonBuilder().create();
                Weathermap weathermap = gson.fromJson(response, Weathermap.class);
                Log.d(TAG, "La température à Trets est de : " + weathermap.getMain().getTemp());

                descriptionTemps = weathermap.getWeather().get(0).getDescription();// on définit la variable descriptionTemps

                switch (descriptionTemps) {
                    case "clear sky": meteoImage.setImageResource(R.drawable.i01d);
                        break;
                    case "few clouds": meteoImage.setImageResource(R.drawable.i02d);
                        break;
                    case "scattered clouds": meteoImage.setImageResource(R.drawable.i03d);
                        break;
                    case "broken clouds": meteoImage.setImageResource(R.drawable.i04d);
                        break;
                    case "shower rain": meteoImage.setImageResource(R.drawable.i09d);
                        break;
                    case "rain": meteoImage.setImageResource(R.drawable.i10d);
                        break;
                    case "thunderstorm": meteoImage.setImageResource(R.drawable.i11d);
                        break;
                    case "snow": meteoImage.setImageResource(R.drawable.i13d);
                        break;
                    case "mist": meteoImage.setImageResource(R.drawable.i50d);
                        break;
                } // on a définit tout les cas possibles de météo
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
// called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d(TAG, "BUG");
            }

            @Override
            public void onRetry(int retryNo) {
// called when request is retried
            }
        });

        mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);

        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");
        mBluetoothConnection.startClient(monDevice, MY_UUID_INSECURE);


        btnVerrouiller = findViewById(R.id.btnVerrouiller);
        btnDeverrouiller = findViewById(R.id.btnDeverrouiller);

        btnVerrouiller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] bytes = VERROUILLER.getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(bytes);
                monImage.setImageResource(R.drawable.chatbloque);
                monText.setText(BluetoothConnectionService.lieuChat);
                switch (descriptionTemps) {
                    case "clear sky": meteoImage.setImageResource(R.drawable.i01d);
                        break;
                    case "few clouds": meteoImage.setImageResource(R.drawable.i02d);
                        break;
                    case "scattered clouds": meteoImage.setImageResource(R.drawable.i03d);
                        break;
                    case "broken clouds": meteoImage.setImageResource(R.drawable.i04d);
                        break;
                    case "shower rain": meteoImage.setImageResource(R.drawable.i09d);
                        break;
                    case "rain": meteoImage.setImageResource(R.drawable.i10d);
                        break;
                    case "thunderstorm": meteoImage.setImageResource(R.drawable.i11d);
                        break;
                    case "snow": meteoImage.setImageResource(R.drawable.i13d);
                        break;
                    case "mist": meteoImage.setImageResource(R.drawable.i50d);
                        break;
                }


            }
        });

        btnDeverrouiller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] bytes = DEVERROUILLER.getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(bytes);
                monImage.setImageResource(R.drawable.chatiere);
                monText.setText(BluetoothConnectionService.lieuChat);
                switch (descriptionTemps) {
                    case "clear sky": meteoImage.setImageResource(R.drawable.i01d);
                        break;
                    case "few clouds": meteoImage.setImageResource(R.drawable.i02d);
                        break;
                    case "scattered clouds": meteoImage.setImageResource(R.drawable.i03d);
                        break;
                    case "broken clouds": meteoImage.setImageResource(R.drawable.i04d);
                        break;
                    case "shower rain": meteoImage.setImageResource(R.drawable.i09d);
                        break;
                    case "rain": meteoImage.setImageResource(R.drawable.i10d);
                        break;
                    case "thunderstorm": meteoImage.setImageResource(R.drawable.i11d);
                        break;
                    case "snow": meteoImage.setImageResource(R.drawable.i13d);
                        break;
                    case "mist": meteoImage.setImageResource(R.drawable.i50d);
                        break;
                }
            }
        });


    }
// l'appareil en question n'a pas le bluetooth
    public void enableBT() {
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
            this.finish();
            System.exit(0);
        }
        // l'appareil en question a pas le bluetooth
        if (!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            //IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);

        }
    }
}