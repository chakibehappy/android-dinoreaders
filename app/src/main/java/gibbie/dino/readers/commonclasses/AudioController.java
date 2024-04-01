package gibbie.dino.readers.commonclasses;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.IOException;

import gibbie.dino.readers.interfaces.AudioControllerCallback;

public class AudioController {

    MediaPlayer player = null;

    public void playSFX(Context context, String filename){
        MediaPlayer player = new MediaPlayer();
        String path = "android.resource://" + context.getPackageName() + "/raw/" + filename;
        try {
            player.setDataSource(context, Uri.parse(path));
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playAudioFromURL(String url, AudioControllerCallback callback)
    {
        stopAudio();
        player = new MediaPlayer();
        try {
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    callback.OnAudioEnd();
                }
            });

            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(url);
            player.prepare();
            player.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopAudio(){
        if(player != null){
            player.stop();
            player.release();
        }
    }

    public void pauseAudio(){
        if(player != null && player.isPlaying())
            player.pause();
    }

}
