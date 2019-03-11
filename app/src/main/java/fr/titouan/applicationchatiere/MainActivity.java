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

    //UUID Arduino
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // Nom du composant Bluetooth Arduino
    private static final String NOM_COMPOSANT_BT_ARDUINO = "HC-05";
    // Gere les threads de communication BT
    BluetoothConnectionService mBluetoothConnection;

    // Code pour verrouiller
    private static final String DEVERROUILLER = "1";

    // Code pour déverrouiller
    private static final String VERROUILLER = "0";
    // on définit une variable descriptiontemps
    String descriptionTemps;

    // on détruit la connection bluetooth lorsque la connection se termine
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        //mBluetoothConnection.stop();
    }

    // Première méthode appelée lorsque l'application se lance
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // On définit le module BT
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Cette méthode regarde si l'appreil à un module BT
        // Si non l'application s'arrête
        // Si oui, si le BT n'est pas activé, il l'est
        enableBT();


        // Récupération de l'image qui représente la chatière
        final ImageView chatiere = findViewById(R.id.chatiere);

        // Récupération de l'image qui représente la météo
        final ImageView meteoImage = findViewById(R.id.meteo);

        // Récupération du texte qui indique la position du chat
        final TextView temperature = findViewById(R.id.temperature);

        // Récupération du texte qui indique la position du chat
        final TextView positionChat = findViewById(R.id.positionChat);
        positionChat.setText("On ne sait pas où est le chat. On ne connait pas la position de la chatière. Cliquez pour rafraichir SVP !.");

        // on définit un bouton verouiller
        final Button btnVerrouiller = findViewById(R.id.btnVerrouiller);

        // on définit un bouton deverouiller
        final Button btnDeverrouiller = findViewById(R.id.btnDeverrouiller);

        // Récupération de l'image qui représente la chatière
        chatiere.setImageResource(R.drawable.chatiere);

        // Récupération de l'image qui représente la chatière
        meteoImage.setImageResource(R.drawable.i50d);

        // On liste les devices appairés
        Set<BluetoothDevice> mesDevices = mBluetoothAdapter.getBondedDevices();
        BluetoothDevice monDevice = null;
        // Pour chaque Device, on log l'adresse et le nom
        for (BluetoothDevice device : mesDevices) {
            Log.d(TAG, device.getAddress());
            Log.d(TAG, device.getName());
            // S'il s'agit du nom du composant BT arduino, c'est notre Device
            if (device.getName().equals(NOM_COMPOSANT_BT_ARDUINO)) {
                monDevice = device;
            }
        }

        //APPID c9afb749c4ce94e522e9668bae6be96c : Clé permettant d"interroger le site openweathermap
        // La requête doit être comme ça :
        // http://api.openweathermap.org/data/2.5/weather?q=trets&appid=c9afb749c4ce94e522e9668bae6be96c
        // Définition d'un client HTTP asynchrone
        AsyncHttpClient client = new AsyncHttpClient();
        // Récupération de la météo à Trets
        client.get("http://api.openweathermap.org/data/2.5/weather?q=trets&appid=c9afb749c4ce94e522e9668bae6be96c&units=metric", new TextHttpResponseHandler() {

            @Override
            public void onStart() {
                Log.d(TAG, "Appel pour météo Trets");
            }


            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
                // Création d'un outil permettant de lire la réponse Json
                Gson gsonBuilder = new GsonBuilder().create();

                // Toutes les classes "Météo" qui représentent la météo renvoyée en Json par le site ont été générées sur le site : http://www.jsonschema2pojo.org/
                // en passant la réponse suivante :
                /*
                {
                    "coord": {
                    "lon": 5.69,
                            "lat": 43.45
                },
                    "weather": [
                    {
                        "id": 801,
                            "main": "Clouds",
                            "description": "few clouds",
                            "icon": "02d"
                    }
  ],
                    "base": "stations",
                        "main": {
                    "temp": 15,
                            "pressure": 1031,
                            "humidity": 62,
                            "temp_min": 14,
                            "temp_max": 16
                },
                    "visibility": 10000,
                        "wind": {
                    "speed": 3.6,
                            "deg": 240
                },
                    "clouds": {
                    "all": 20
                },
                    "dt": 1551025800,
                        "sys": {
                    "type": 1,
                            "id": 6512,
                            "message": 0.0036,
                            "country": "FR",
                            "sunrise": 1550989281,
                            "sunset": 1551028802
                },
                    "id": 6453664,
                        "name": "Trets",
                        "cod": 200
                }
                */


                // Tout est chargé dans un objet Weathermap
                Weathermap weathermap = gsonBuilder.fromJson(response, Weathermap.class);

                // La temprérature est loguée
                Log.d(TAG, "La température à Trets est de : " + weathermap.getMain().getTemp());
                temperature.setText(weathermap.getMain().getTemp().toString() + "°");

                // On récupère le description du temps qu'il fait
                descriptionTemps = weathermap.getWeather().get(0).getIcon();
                Log.d(TAG, "Temps : " + weathermap.getWeather().get(0).getIcon());

                // Selon le temps, on affiche un image différente
                afficherImageMeteo(meteoImage);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
                // Si la connexion ne fonctionne pas, on log
                Log.d(TAG, "BUG");
            }

            @Override
            public void onRetry(int retryNo) {
                // non défini
            }
        });
        // fin de l'appel au site Météo

        // Création d'une classe gérant la connexion BT
        mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
        Log.d(TAG, "startBTConnection: Initialisation de la connexion.");
        //Démérrage d'un thread client
        mBluetoothConnection.startClient(monDevice, MY_UUID_INSECURE);

        // Définition d'un listener sur le bouton
        btnVerrouiller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // On met 0 dans un tableau de bytes
                byte[] bytes = VERROUILLER.getBytes(Charset.defaultCharset());
                // On envoie le tableau à l'Arduino
                mBluetoothConnection.write(bytes);
                // On affiche l'image de la chatière bloquée
                chatiere.setImageResource(R.drawable.chatbloque);
                // On refresh a position du chat
                positionChat.setText(BluetoothConnectionService.lieuChat);
                // On refresh l'image météo
                afficherImageMeteo(meteoImage);
            }
        });

        // Définition d'un listener sur le bouton
        btnDeverrouiller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // On met 0 dans un tableau de bytes
                byte[] bytes = DEVERROUILLER.getBytes(Charset.defaultCharset());
                // On envoie le tableau à l'Arduino
                mBluetoothConnection.write(bytes);
                // On affiche l'image de la chatière débloquée
                chatiere.setImageResource(R.drawable.chatiere);
                // On refresh la position du chat
                positionChat.setText(BluetoothConnectionService.lieuChat);
                // On refresh l'image météo
                afficherImageMeteo(meteoImage);
            }
        });

        positionChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // On refresh la position du chat
                positionChat.setText(BluetoothConnectionService.lieuChat);
                afficherImageMeteo(meteoImage);
            }
        });

    }

    // Cette méthode regarde si l'appreil à un module BT
    // Si non l'application s'arrête
    // Si oui, si le BT n'est pas activé, il l'est
    public void enableBT() {
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "enableBT: Pas de module BT sur l'appareil");
            this.finish();
            System.exit(0);
        }
        // l'appareil en question a pas le bluetooth
        if (!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "enableBT: Activation BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);
        }
    }

    // En fonction du temps, change l'image de la météo
    public void afficherImageMeteo(ImageView meteoImage) {
        switch (descriptionTemps) {
            case "01n":
                meteoImage.setImageResource(R.drawable.i01d);
                break;
            case "02n":
                meteoImage.setImageResource(R.drawable.i02d);
                break;
            case "03n":
                meteoImage.setImageResource(R.drawable.i03d);
                break;
            case "04n":
                meteoImage.setImageResource(R.drawable.i04d);
                break;
            case "09n":
                meteoImage.setImageResource(R.drawable.i09d);
                break;
            case "10n":
                meteoImage.setImageResource(R.drawable.i10d);
                break;
            case "11n":
                meteoImage.setImageResource(R.drawable.i11d);
                break;
            case "13n":
                meteoImage.setImageResource(R.drawable.i13d);
                break;
            case "50n":
                meteoImage.setImageResource(R.drawable.i50d);
                break;
        } // on a définit tout les cas possibles de météo
    }
}