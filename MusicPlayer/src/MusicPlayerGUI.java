import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

public class MusicPlayerGUI extends  JFrame {

    public static final Color FRAME_COLOR = Color.BLACK;
    public static final Color TEXT_COLOR = Color.WHITE;

    private MusicPlayer musicPlayer;

    private JFileChooser jFileChooser;

    private JLabel songTitle, songArtist;
    private JPanel playbackBtns;
    private JSlider playbackSlider;

    public MusicPlayerGUI() {
        super("Music Player");

        setSize(400, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);

        getContentPane().setBackground(FRAME_COLOR);

        musicPlayer = new MusicPlayer(this);
        jFileChooser = new JFileChooser();

        jFileChooser.setCurrentDirectory(new File("src/Songs"));
        jFileChooser.setFileFilter(new FileNameExtensionFilter("MP3","mp3"));

        addGuiComponent();
    }

    private void addGuiComponent() {
        addToolbar();

        JLabel songImage = new JLabel(loadImage("src/Buttons/record.png"));
        songImage.setBounds(0, 50, getWidth() - 20, 225);
        add(songImage);

        songTitle = new JLabel("Song Title");
        songTitle.setBounds(0, 285, getWidth()-10, 30);
        songTitle.setFont(new Font("Dialog", Font.BOLD, 24));
        songTitle.setForeground(TEXT_COLOR);
        songTitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(songTitle);

        songArtist = new JLabel("Artist");
        songArtist.setBounds(0,315, getWidth()-10,30);
        songArtist.setFont(new Font("Dialog", Font.PLAIN, 24));
        songArtist.setForeground(TEXT_COLOR);
        songArtist.setHorizontalAlignment(SwingConstants.CENTER);
        add(songArtist);

        playbackSlider = new JSlider(JSlider.HORIZONTAL,0,100,0);
        playbackSlider.setBounds(getWidth()/2 - 300/2, 365, 300, 40);
        playbackSlider.setBackground(null);
        playbackSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                musicPlayer.pauseSong();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                JSlider source = (JSlider) e.getSource();

                int frame = source.getValue();

                musicPlayer.setCurrentFrame(frame);

                musicPlayer.setCurrentTimeInMilli((int) (frame / (1.67 * musicPlayer.getCurrentSong().getFrameRatePerMilliseconds())));

                musicPlayer.playCurrentSong();

                enablePauseButtonDisablePlayButton();
            }
        });
        add(playbackSlider);

        addPlaybackBtns();
    }


    private void addToolbar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setBounds(0, 0, getWidth(), 20);
        toolBar.setFloatable(false);

        JMenuBar menuBar = new JMenuBar();
        toolBar.add(menuBar);

        // Song menu
        JMenu songMenu = new JMenu("Song");
        menuBar.add(songMenu);

        JMenuItem loadSong = new JMenuItem("Load Song");
        loadSong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                if(result == JFileChooser.APPROVE_OPTION && selectedFile != null){
                    Song song = new Song(selectedFile.getPath());
                    musicPlayer.loadSong(song);

                    updateSongTitleAndArtist(song);

                    updatePlaybackSlider(song);

                    enablePauseButtonDisablePlayButton();
                }
            }
        });
        songMenu.add(loadSong);

        JMenuItem findSong = new JMenuItem("Find Song");
        findSong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String prefix = JOptionPane.showInputDialog("Enter the first letter of the song:");
                if (prefix != null && !prefix.isEmpty()) {
                    prefix = prefix.toLowerCase();
                    if (!musicPlayer.searchSong(prefix).isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Song found with prefix: " + musicPlayer.searchSong(prefix));
                    } else {
                        JOptionPane.showMessageDialog(null, "No song found with prefix: " + prefix);
                    }
                }
            }
        });
        songMenu.add(findSong);

        JMenuItem sortSongs = new JMenuItem("Sort Songs");
        sortSongs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser playlistChooser = new JFileChooser("src/Playlist");
                playlistChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
                int result = playlistChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = playlistChooser.getSelectedFile();

                if (result == JFileChooser.APPROVE_OPTION && selectedFile != null) {
                    musicPlayer.loadPlaylist(selectedFile);
                    ArrayList<Song> songs = musicPlayer.getPlaylist();
                    new SongListFrame(songs).setVisible(true);
                }
            }
        });
        songMenu.add(sortSongs);

        JMenuItem recentlyPlayed = new JMenuItem("Recently Played");
        recentlyPlayed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRecentlyPlayedSongs();
            }
        });
        songMenu.add(recentlyPlayed);

        // Playlist menu
        JMenu playlistMenu = new JMenu("Playlist");
        menuBar.add(playlistMenu);

        JMenuItem createPlaylist = new JMenuItem("Create Playlist");
        createPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // load music playlist dialog
                new MusicPlaylistDialog(MusicPlayerGUI.this).setVisible(true);
            }
        });
        playlistMenu.add(createPlaylist);

        JMenuItem loadPLaylist = new JMenuItem("Load PLaylist");
        loadPLaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileFilter(new FileNameExtensionFilter("Playlist", "txt"));
                jFileChooser.setCurrentDirectory(new File("src/Playlist"));

                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                if(result == JFileChooser.APPROVE_OPTION && selectedFile != null){
                    // stop the music
                    musicPlayer.stopSong();

                    // load playlist
                    musicPlayer.loadPlaylist(selectedFile);
                }
            }
        });
        playlistMenu.add(loadPLaylist);

        add(toolBar);
    }

    private void addPlaybackBtns() {
        playbackBtns = new JPanel();
        playbackBtns.setBounds(0, 435, getWidth() - 10, 80);
        playbackBtns.setBackground(null);

        // previous button
        JButton prevButton = new JButton(loadImage("src/Buttons/previous.png"));
        prevButton.setBorderPainted(false);
        prevButton.setBackground(null);
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // go to the previous song
                musicPlayer.prevSong();
            }
        });
        playbackBtns.add(prevButton);

        // play button
        JButton playButton = new JButton(loadImage("src/Buttons/play.png"));
        playButton.setBorderPainted(false);
        playButton.setBackground(null);
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enablePauseButtonDisablePlayButton();

                musicPlayer.playCurrentSong();
            }
        });
        playbackBtns.add(playButton);

        // pause button
        JButton pauseButton = new JButton(loadImage("src/Buttons/pause.png"));
        pauseButton.setBorderPainted(false);
        pauseButton.setBackground(null);
        pauseButton.setVisible(false);
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enablePlayButtonDisablePauseButton();

                musicPlayer.pauseSong();
            }
        });
        playbackBtns.add(pauseButton);

        //next button
        JButton nextButton = new JButton(loadImage("src/Buttons/next.png"));
        nextButton.setBorderPainted(false);
        nextButton.setBackground(null);
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // go to the next song
                musicPlayer.nextSong();
            }
        });
        playbackBtns.add(nextButton);

        add(playbackBtns);
    }

    public void setPlaybackSliderValue(int frame){
        playbackSlider.setValue(frame);
    }

    public void updateSongTitleAndArtist(Song song){
        songTitle.setText(song.getSongTitle());
        songArtist.setText(song.getSongArtist());
    }

    public void updatePlaybackSlider(Song song){
        playbackSlider.setMaximum(song.getMp3File().getFrameCount());

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();

        JLabel labelBeginning = new JLabel("00:00");
        labelBeginning.setFont(new Font("Dialog", Font.BOLD, 18));
        labelBeginning.setForeground(TEXT_COLOR);

        JLabel labelEnd = new JLabel(song.getSongLength());
        labelEnd.setFont(new Font("Dialog", Font.BOLD, 18));
        labelEnd.setForeground(TEXT_COLOR);

        labelTable.put(0, labelBeginning);
        labelTable.put(song.getMp3File().getFrameCount(), labelEnd);

        playbackSlider.setLabelTable(labelTable);
        playbackSlider.setPaintLabels(true);
    }

    public void enablePauseButtonDisablePlayButton() {
        JButton playButton = (JButton) playbackBtns.getComponent(1);
        JButton pauseButton = (JButton) playbackBtns.getComponent(2);

        playButton.setVisible(false);
        playButton.setEnabled(false);

        pauseButton.setVisible(true);
        pauseButton.setEnabled(true);
    }

    public void enablePlayButtonDisablePauseButton() {
        JButton playButton = (JButton) playbackBtns.getComponent(1);
        JButton pauseButton = (JButton) playbackBtns.getComponent(2);

        playButton.setVisible(true);
        playButton.setEnabled(true);

        pauseButton.setVisible(false);
        pauseButton.setEnabled(false);
    }

    private void showRecentlyPlayedSongs() {
        CircularBuffer<Song> recentlyPlayedSongs = musicPlayer.getRecentlyPlayedSongs();
        Song[] songs = recentlyPlayedSongs.getAll();

        JFrame frame = new JFrame("Recently Played Songs");
        frame.setSize(300, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(this);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (Song song : songs) {
            listModel.addElement(song.getSongTitle() + " - " + song.getSongArtist());
        }

        JList<String> songList = new JList<>(listModel);
        frame.add(new JScrollPane(songList));

        frame.setVisible(true);
    }

    private ImageIcon loadImage (String imagePath){
        try{
            BufferedImage image = ImageIO.read(new File(imagePath));
            return new ImageIcon(image);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}

