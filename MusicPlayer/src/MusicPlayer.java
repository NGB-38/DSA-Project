import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class MusicPlayer extends PlaybackListener {
    private static final Object playSignal = new Object();
    private MusicPlayerGUI musicPlayerGUI;

    private Song currentSong;
    public Song getCurrentSong() {
        return currentSong;
    }

    private AdvancedPlayer advancePlayer;

    private boolean isPaused;
    private int currentFrame;
    public void setCurrentFrame(int frame){
        currentFrame = frame;
    }

    private int currentTimeInMilli;
    public void setCurrentTimeInMilli(int timeInMilli){
        currentTimeInMilli = timeInMilli;
    }

    public MusicPlayer(MusicPlayerGUI musicPlayerGUI){
        this.musicPlayerGUI = musicPlayerGUI;
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

            startPlaybackSliderThread();

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
                        synchronized (playSignal){
                            isPaused = false;

                            playSignal.notify();
                        }
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

    private void startPlaybackSliderThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(isPaused){
                    try {
                        synchronized (playSignal){
                            playSignal.wait();
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }

//                System.out.println("isPaused: " + isPaused);
                while(!isPaused){
                    try {
                        currentTimeInMilli++;

//                        System.out.println(currentTimeInMilli * 1.67);

                        int calculatedFrame = (int)((double) currentTimeInMilli * 1.67 * currentSong.getFrameRatePerMilliseconds());

                        musicPlayerGUI.setPlaybackSliderValue(calculatedFrame);

                        Thread.sleep(1);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

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
//        System.out.println("Actual Stop: " + evt.getFrame());
        if(isPaused){
            currentFrame += (int) ((double) evt.getFrame() *currentSong.getFrameRatePerMilliseconds());
//            System.out.println("Stopped @" + currentFrame);
        }
    }
}
