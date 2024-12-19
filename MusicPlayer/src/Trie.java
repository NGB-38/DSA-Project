import java.util.*;

public class Trie {
    private class TrieNode {
        private TrieNode[] children;
        private boolean isEndOfWord;
        private String songInfo;

        public TrieNode() {
            children = new TrieNode[26];
            isEndOfWord = false;
            songInfo = null;
        }

        public TrieNode[] getChildren() {
            return children;
        }

        public boolean isEndOfWord() {
            return isEndOfWord;
        }

        public void setEndOfWord(boolean endOfWord) {
            isEndOfWord = endOfWord;
        }

        public String getSongInfo() {
            return songInfo;
        }

        public void setSongInfo(String songInfo) {
            this.songInfo = songInfo;
        }

    }

    private TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    public void insert(String key, String songInfo) {
        TrieNode node = root;
        for (char c : key.toLowerCase().toCharArray()) {
            if (c < 'a' || c > 'z') {
                continue;
            }
            int index = c - 'a';
            if (node.getChildren()[index] == null) {
                node.getChildren()[index] = new TrieNode();
            }
            node = node.getChildren()[index];
        }
        node.setEndOfWord(true);
        node.setSongInfo(songInfo);

    }

    public List<String> searchByPrefix(String prefix) {
        TrieNode node = root;
        for (char c : prefix.toLowerCase().toCharArray()) {
            if (c < 'a' || c > 'z') {
                continue;
            }
            int index = c - 'a';
            if (node.getChildren()[index] == null) {
                return new ArrayList<>();
            }
            node = node.getChildren()[index];
        }
        return collectAllSongs(node);
    }

    private List<String> collectAllSongs(TrieNode node) {
        List<String> songs = new ArrayList<>();
        if (node.isEndOfWord()) {
            songs.add(node.getSongInfo());
        }
        for (TrieNode child : node.getChildren()) {
            if (child != null) {
                songs.addAll(collectAllSongs(child));
            }
        }
        return songs;
    }
}

