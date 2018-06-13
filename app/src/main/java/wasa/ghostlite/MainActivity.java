package wasa.ghostlite;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.mapboxsdk.style.sources.TileSet;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

public class MainActivity extends Activity {

    private DrawMapView drawMapView;
    private static final String TAG = MainActivity.class.getSimpleName();
    MapServer server = null;
    Speech speech;
    ArduinoCommunication communication;

    private void createServer() {
        if (server == null) {
            server = new MapServer("mnt/external_sd/", 8080);
            new Thread(server).start();
            Log.e("main", "Server Started");
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
        drawMapView.initLogFile();
        drawMapView.onCreate(savedInstanceState);
        drawMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                // 使用例などはこちら
                // https://github.com/mapbox/mapbox-android-demo/tree/master/MapboxAndroidDemo/src/main/java/com/mapbox/mapboxandroiddemo/examples/styles

                drawMapView.setMapboxMap(mapboxMap);

                // 既定のマップは全部消す
                mapboxMap.clear();
                for (Layer layer: mapboxMap.getLayers()) mapboxMap.removeLayer(layer);
                for (Source source: mapboxMap.getSources()) mapboxMap.removeSource(source);

                // マップデータの読み込みソースを設定
                TileSet tileSet = new TileSet("{\"version\": 8}", "http://localhost:8080/osm_tiles/{z}/{x}/{y}.pbf");
                VectorSource mapSource = new VectorSource("mapSource", tileSet);
                mapboxMap.addSource(mapSource);

                // 各マップレイヤの表示
                LineLayer waterLayer = new LineLayer("water", "mapSource").withSourceLayer("water").withProperties(
                        PropertyFactory.lineColor(Color.parseColor("#0761FC")),
                        PropertyFactory.lineWidth(1.0f)
                );
                mapboxMap.addLayer(waterLayer);

                LineLayer aerowayLayer = new LineLayer("aeroway", "mapSource").withSourceLayer("aeroway").withProperties(
                        PropertyFactory.lineColor(Color.parseColor("#FC7907")),
                        PropertyFactory.lineWidth(5.0f)
                );
                mapboxMap.addLayer(aerowayLayer);

                LineLayer boundaryLayer = new LineLayer("boundary", "mapSource").withSourceLayer("boundary").withProperties(
                        PropertyFactory.lineColor(Color.parseColor("#66FF99")),
                        PropertyFactory.lineWidth(1.0f)
                );
                mapboxMap.addLayer(boundaryLayer);

                LineLayer transportationLayer = new LineLayer("transportation", "mapSource").withSourceLayer("transportation").withProperties(
                        PropertyFactory.lineColor(Color.parseColor("#660099")),
                        PropertyFactory.lineWidth(1.0f)
                );
                mapboxMap.addLayer(transportationLayer);

                // 桶川スポットのソース追加
                Point okegawaPoint = Point.fromLngLat(139.523889, 35.975278);
                GeoJsonSource okegawaSource = new GeoJsonSource("okegawaPoint", okegawaPoint);
                mapboxMap.addSource(okegawaSource);

                // 桶川スポットのレイヤ追加
                CircleLayer okegawaLayer = new CircleLayer("okegawaLayer", "okegawaPoint").withProperties(
                        PropertyFactory.circleRadius(10f),
                        PropertyFactory.circleColor(Color.parseColor("#007CBF"))
                );
                okegawaLayer.setMinZoom(3);
                mapboxMap.addLayer(okegawaLayer);

                Bitmap planeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.plane);

                mapboxMap.addImage("planeImage", planeBitmap);

                // 機体の画像の位置情報を初期設定
                Point initPoint = Point.fromLngLat(139.523889, 35.975278);
                GeoJsonSource planeSource = new GeoJsonSource("planeSource", initPoint);
                mapboxMap.addSource(planeSource);

                // 機体の画像情報を表示
                SymbolLayer planeLayer = new SymbolLayer("planeLayer", "planeSource").withProperties(
                        PropertyFactory.iconImage("planeImage"),
                        PropertyFactory.iconSize(1.0f),
                        PropertyFactory.iconOffset(new Float[]{0.0f, 39.0f}),
                        PropertyFactory.iconRotationAlignment("map")
                );
                mapboxMap.addLayer(planeLayer);
            }
        });

        this.communication = new ArduinoCommunication(this, this.drawMapView);

        this.speech = new Speech(MainActivity.this, this.drawMapView);

        // デバッグ用です (画面をタップしたら回転数が増える)
//        this.drawMapView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                drawMapView.cadence++;
//                drawMapView.updateData();
//                return true;
//            }
//        });
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
