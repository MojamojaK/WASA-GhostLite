package wasa.ghostlite;

import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class Speech implements Runnable {

    boolean enabled = false;
    DrawMapView drawMapView;
    private TextToSpeech tts;

    Speech(MainActivity mainActivity, DrawMapView drawMapView) {
        Speech t = this;
        this.drawMapView = drawMapView;
        this.tts = new TextToSpeech(mainActivity, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.JAPAN);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("error", "This language is not supported.");
                    } else {
                        enabled = true;
                        new Thread(t).start();
                        Log.d("success", "TTS Enabled.");
                    }
                } else {
                    Log.e("error", "Initialization Failed.");
                }
            }
        });
    }

    public void run() {
        while (enabled) {
            try {
                Thread.sleep(2000);
                String text = Integer.toString(this.drawMapView.cadence);
                if (text == null || "".equals(text)) {
                    text = "Error";
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                } else {
                    if (this.drawMapView.cadence > 70) { // 70未満はうざいというフィードバックをもらった
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            } catch (InterruptedException e) {}
        }
    }

}
