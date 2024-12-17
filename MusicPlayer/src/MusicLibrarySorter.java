import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MusicLibrarySorter {
    private static final int RUN = 32;

    public static void sortPlaylistByTitle(DoublyLinkedList<Song> playlist) {
        if (playlist == null || playlist.size() == 0) return;

        // Convert DoublyLinkedList to ArrayList for sorting
        ArrayList<Song> songList = new ArrayList<>();
        for (int i = 0; i < playlist.size(); i++) {
            songList.add(playlist.get(i));
        }

        // Sort the list using Timsort
        timSort(songList, new SongTitleComparator());

        // Clear the original playlist and add sorted songs back
        playlist.clear();
        for (Song song : songList) {
            playlist.add(song);
        }
    }

    private static void insertionSort(List<Song> songs, int left, int right, Comparator<Song> comparator) {
        for (int i = left + 1; i <= right; i++) {
            Song key = songs.get(i);
            int j = i - 1;
            while (j >= left && comparator.compare(songs.get(j), key) > 0) {
                songs.set(j + 1, songs.get(j));
                j--;
            }
            songs.set(j + 1, key);
        }
    }

    private static void mergeSort(List<Song> songs, int left, int mid, int right, Comparator<Song> comparator) {
        int len1 = mid - left + 1;
        int len2 = right - mid;

        List<Song> leftArray = new ArrayList<>(len1);
        List<Song> rightArray = new ArrayList<>(len2);

        for (int i = 0; i < len1; i++) {
            leftArray.add(songs.get(left + i));
        }
        for (int i = 0; i < len2; i++) {
            rightArray.add(songs.get(mid + 1 + i));
        }

        int i = 0, j = 0, k = left;
        while (i < len1 && j < len2) {
            if (comparator.compare(leftArray.get(i), rightArray.get(j)) <= 0) {
                songs.set(k++, leftArray.get(i++));
            } else {
                songs.set(k++, rightArray.get(j++));
            }
        }

        while (i < len1) {
            songs.set(k++, leftArray.get(i++));
        }

        while (j < len2) {
            songs.set(k++, rightArray.get(j++));
        }
    }

    public static void timSort(List<Song> songs, Comparator<Song> comparator) {
        int n = songs.size();

        for (int i = 0; i < n; i += RUN) {
            insertionSort(songs, i, Math.min((i + RUN - 1), (n - 1)), comparator);
        }

        for (int size = RUN; size < n; size = 2 * size) {
            for (int left = 0; left < n; left += 2 * size) {
                int mid = left + size - 1;
                int right = Math.min((left + 2 * size - 1), (n - 1));

                if (mid < right) {
                    mergeSort(songs, left, mid, right, comparator);
                }
            }
        }
    }

    private static class SongTitleComparator implements Comparator<Song> {
        @Override
        public int compare(Song s1, Song s2) {
            return s1.getSongTitle().compareToIgnoreCase(s2.getSongTitle());
        }
    }
}