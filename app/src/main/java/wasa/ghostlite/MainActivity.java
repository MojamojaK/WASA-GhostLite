package wasa.ghostlite;

import android.app.Activity;
import android.os.Bundle;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

public class MainActivity extends Activity {

    private DrawMapView drawMapView;
    private static final String TAG = MainActivity.class.getSimpleName();
    MapServer server = null;
    ArduinoCommunication communication;

    private void createServer() {
        if (server == null) {
            server = new MapServer("mnt/external_sd/", 8080);
            new Thread(server).start();
        }
    }

    private void closeServer() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createServer();

        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);
        drawMapView = (DrawMapView) findViewById(R.id.drawMapView);
        drawMapView.onCreate(savedInstanceState);
        drawMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                drawMapView.setStyleUrl("asset://mapStyle.json");
            }
        });

        this.communication = new ArduinoCommunication(this, this.drawMapView);
    }

    @Override
    public void onStart() {
        super.onStart();
        drawMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        createServer();
        this.communication.resume();
        drawMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        closeServer();
        this.communication.close();
        drawMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        closeServer();
        drawMapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
        drawMapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.communication.destroy();
        closeServer();
        drawMapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        drawMapView.onSaveInstanceState(outState);
    }
}
