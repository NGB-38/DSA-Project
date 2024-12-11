import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class MusicPlayer extends PlaybackListener {
    private Song currentSong;

    private AdvancedPlayer advancePlayer;

    private boolean isPaused;
    private int currentFrame;

    public MusicPlayer(){

    }

    public void loadSong(Song song){
        currentSong = song;

        if(currentSong != null){
            playCurrentSong();
        }
    }

    public void pauseSong(){
        if(advancePlayer != null){
            isPaused = true;

            stopSong();
        }
    }

    public void stopSong(){
        if(advancePlayer != null){
            advancePlayer.stop();
            advancePlayer.close();
            advancePlayer = null;
        }
    }

    public void playCurrentSong() {
        if(currentSong == null) return;
        try {
            FileInputStream fileInputStream = new FileInputStream(currentSong.getFilePath());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            advancePlayer = new AdvancedPlayer(bufferedInputStream);
            advancePlayer.setPlayBackListener(this);

            startMusicThread();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void startMusicThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(isPaused){
                        advancePlayer.play(currentFrame, Integer.MAX_VALUE);
                    }else{
                        advancePlayer.play();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void playbackStarted(PlaybackEvent evt) {
        System.out.println("Playback Started");
    }

    @Override
    public void playbackFinished(PlaybackEvent evt) {
        System.out.println("Playback Finished");

        if(isPaused){
            currentFrame += (int) ((double) evt.getFrame() *currentSong.getFrameRatePerMilliseconds());
//            System.out.println("Stopped @" + currentFrame);
        }
    }
}
