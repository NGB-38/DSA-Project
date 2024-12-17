import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;

public class SongListFrame extends JFrame {
    private DefaultListModel<String> songListModel;
    private JList<String> songListDisplay;
    private ArrayList<Song> songs;

    public SongListFrame(ArrayList<Song> songs) {
        this.songs = songs;
        setTitle("Sorted Songs");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        songListModel = new DefaultListModel<>();
        songListDisplay = new JList<>(songListModel);
        add(new JScrollPane(songListDisplay), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton sortByTitleButton = new JButton("Sort by Title");
        sortByTitleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MusicLibrarySorter.timSort(songs, Comparator.comparing(Song::getSongTitle, String.CASE_INSENSITIVE_ORDER));
                updateSongListDisplay();
            }
        });
        buttonPanel.add(sortByTitleButton);

        JButton sortByArtistButton = new JButton("Sort by Artist");
        sortByArtistButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MusicLibrarySorter.timSort(songs, Comparator.comparing(Song::getSongArtist, String.CASE_INSENSITIVE_ORDER));
                updateSongListDisplay();
            }
        });
        buttonPanel.add(sortByArtistButton);

        JButton sortByLengthButton = new JButton("Sort by Length");
        sortByLengthButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MusicLibrarySorter.timSort(songs, Comparator.comparing(Song::getSongLength));
                updateSongListDisplay();
            }
        });
        buttonPanel.add(sortByLengthButton);

        add(buttonPanel, BorderLayout.SOUTH);

        updateSongListDisplay();
    }

    private void updateSongListDisplay() {
        songListModel.clear();
        for (Song song : songs) {
            songListModel.addElement(song.getSongTitle() + " - " + song.getSongArtist() + " (" + song.getSongLength() + ")");
        }
    }
}