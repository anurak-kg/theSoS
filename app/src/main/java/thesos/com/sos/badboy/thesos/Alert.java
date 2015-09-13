package thesos.com.sos.badboy.thesos;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;


/**
 * Created by Anurak on 28/12/57.
 */
public class Alert {
    Activity activity;
    Context context;
    public MediaPlayer mediaPlayer;

    Alert(Context context){
    this.context = context;
    }
    public void playAlarmSound(){
        this.playSound();
    }
    public void playSound(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.alarmsound);
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mediaPlayer.release();
                        }
                    });
                    mediaPlayer.start();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.run();
    }
    public void stopAlarmSound(){
        mediaPlayer.stop();

    }
}
