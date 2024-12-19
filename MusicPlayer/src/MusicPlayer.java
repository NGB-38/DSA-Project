import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.*;
import java.util.*;

public class MusicPlayer extends PlaybackListener {
    private static final Object playSignal = new Object();
    private MusicPlayerGUI musicPlayerGUI;
    private Trie songTrie;
    private CircularBuffer<Song> recentlyPlayedSongs;

    private Song currentSong;
    public Song getCurrentSong() {
        return currentSong;
    }

    private DoublyLinkedList<Song> playlist;

    private AdvancedPlayer advancePlayer;

    private boolean isPaused;

    private boolean songFinished;

    private boolean pressedNext, pressedPrev;

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
        songTrie = new Trie();
        recentlyPlayedSongs = new CircularBuffer<>(5, Song.class);
    }

    public void loadSong(Song song){
        currentSong = song;
        playlist = null;

        if(!songFinished)
            stopSong();

        if(currentSong != null){
            currentFrame = 0;
            currentTimeInMilli = 0;
            musicPlayerGUI.setPlaybackSliderValue(0);

            playCurrentSong();
        }
        recentlyPlayedSongs.add(song);
    }

    public List<String> searchSong(String prefix) {
        Song song = new Song("src/Songs/Wind Riders - Asher Fulero.mp3");
        songTrie.insert(song.getSongTitle().toLowerCase(), song.getSongTitle() +"-"+ song.getSongArtist());
        Song song2 = new Song("src/Songs/Auld Lang Syne (Instrumental) - Jingle Punks.mp3");
        songTrie.insert(song2.getSongTitle().toLowerCase(), song2.getSongTitle() +"-"+ song2.getSongArtist());
        Song song3 = new Song("src/Songs/Tropic Fuse - French Fuse.mp3");
        songTrie.insert(song3.getSongTitle().toLowerCase(), song3.getSongTitle() +"-"+ song3.getSongArtist());
        return songTrie.searchByPrefix(prefix);
    }

    public void loadPlaylist(File playlistFile){
        playlist = new DoublyLinkedList<>();

        // store the paths from the text file into the playlist DLL
        try {
            FileReader fileReader = new FileReader(playlistFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // reach each line from the text file and store the text into the songPath variable
            String songPath;
            while((songPath = bufferedReader.readLine()) != null) {
                Song song = new Song(songPath);
                playlist.add(song); // add operation is O(1) DLL
            }
        } catch(Exception e){
            e.printStackTrace();
        }

        if(playlist.size() > 0){
            // reset playback slider
            musicPlayerGUI.setPlaybackSliderValue(0);
            currentTimeInMilli = 0;

            // update current song to the first song in the playlist
            playlist.resetCurrent();
            currentSong = playlist.getCurrent();

            // start from the beginning frame
            currentFrame = 0;

            // update gui
//            musicPlayerGUI.enablePauseButtonDisablePlayButton();
            musicPlayerGUI.enablePlayButtonDisablePauseButton(); // prevent the song from playing automatically
            musicPlayerGUI.updateSongTitleAndArtist(currentSong);
            musicPlayerGUI.updatePlaybackSlider(currentSong);

            // start song
//            playCurrentSong();
        }

        ArrayList<Song> songs = getPlaylist();
        for (Song song : songs) {
            recentlyPlayedSongs.add(song);
        }
    }

    public ArrayList<Song> getPlaylist() {
        ArrayList<Song> songList = new ArrayList<>();
        for (int i = 0; i < playlist.size(); i++) {
            songList.add(playlist.get(i));
        }
        return songList;
    }

    public CircularBuffer<Song> getRecentlyPlayedSongs() {
        return recentlyPlayedSongs;
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

    public void nextSong(){
        // no need to go to the next song if there is no playlist
        if (playlist == null) return;

        // check to see if we have reached the end of the playlist, if so then don't do anything
        if(!playlist.hasNext()) return;

        pressedNext = true;

        // stop the song if possible
        if(!songFinished)
            stopSong();

        // move to the next song
        playlist.moveToNext();

        // update current song
        currentSong = playlist.getCurrent();

        // reset frame
        currentFrame = 0;

        // reset current time in milli
        currentTimeInMilli = 0;

        // update gui
        musicPlayerGUI.enablePauseButtonDisablePlayButton();
        musicPlayerGUI.updateSongTitleAndArtist(currentSong);
        musicPlayerGUI.updatePlaybackSlider(currentSong);

        // play the song
        playCurrentSong();
    }

    public void prevSong(){
        // no need to go to the previous song if there is no playlist
        if (playlist == null) return;

        // check to see if can go to the previous song
        if(!playlist.hasPrev()) return;

        pressedPrev = true;

        // stop the song if possible
        if(!songFinished)
            stopSong();

        // move to the previous song
        playlist.moveToPrev();

        // update current song
        currentSong = playlist.getCurrent();

        // reset frame
        currentFrame = 0;

        // reset current time in milli
        currentTimeInMilli = 0;

        // update gui
        musicPlayerGUI.enablePauseButtonDisablePlayButton();
        musicPlayerGUI.updateSongTitleAndArtist(currentSong);
        musicPlayerGUI.updatePlaybackSlider(currentSong);

        // play the song
        playCurrentSong();
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
                while(!isPaused && !songFinished && !pressedNext && !pressedPrev){
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
        songFinished = false;
        pressedNext = false;
        pressedPrev = false;
    }

    @Override
    public void playbackFinished(PlaybackEvent evt) {
        System.out.println("Playback Finished");
//        System.out.println("Actual Stop: " + evt.getFrame());
        if (isPaused){
            currentFrame += (int) ((double) evt.getFrame() *currentSong.getFrameRatePerMilliseconds());
//            System.out.println("Stopped @" + currentFrame);
        } else {
            // if pressed next or prev button, then don't need to execute the code below
            if(pressedNext || pressedPrev) return;

            //when the song ends
            songFinished = true;

            if(playlist == null) {
                //update gui
                musicPlayerGUI.enablePlayButtonDisablePauseButton();
            } else {
                // last song in the playlist
                if(!playlist.hasNext()){
                    //update gui
                    musicPlayerGUI.enablePlayButtonDisablePauseButton();
                } else {
                    // play the next song
                    nextSong();
                }
            }

        }
    }
}
