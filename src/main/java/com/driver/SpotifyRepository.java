package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        users.add(new User(name, mobile));
        return users.get(users.size()-1);
    }

    public Artist createArtist(String name) {
        artists.add(new Artist(name));
        return artists.get(artists.size()-1);
    }

    public Album createAlbum(String title, String artistName) {
        boolean present = false;
        Album a = new Album(title);
        for(Artist artist : artists){
            if(artist.getName().equals(artistName)){
                albums.add(a); //new Album created.
                present = true;
                break;
            }
        }

        Artist at = new Artist(artistName);
        if(!present){
            artists.add(at);
            albums.add(a);
        }

        //update the artistAlbumHashMap.
        List<Album> albums1 = new ArrayList<Album>();
        if(artistAlbumMap.containsKey(at)) albums1 = artistAlbumMap.get(at);
        albums1.add(a);
        artistAlbumMap.put(at, albums1);

        return a;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        boolean present = false;
        Song s = new Song(title, length);
        for(Album album : albums){
            if(album.getTitle().equals(albumName)){
                songs.add(s);
                present = true;

                //update the albumSongHashMap.
                List<Song> songs1 = new ArrayList<>();
                if(albumSongMap.containsKey(album)) songs1 = albumSongMap.get(album);
                songs1.add(s);
                albumSongMap.put(album, songs1);
                break;
            }
        }

        if(!present) throw new RuntimeException("Album does not exist");

        return s;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        Playlist pl = new Playlist(title);
        playlists.add(pl); //create a playlist.

        List<Song> song1 = new ArrayList<>();
       // if(playlistSongMap.containsKey(pl)) song1 = playlistSongMap.get(pl);
        for(Song song : songs){
            if(song.getLength() == length){
                song1.add(song);
            }
        }

        playlistSongMap.put(pl, song1);

        boolean present = false;
        for(User user1 : users){
            if(user1.getMobile().equals(mobile)){
                creatorPlaylistMap.put(user1, pl);
                List<User> user = new ArrayList<User>();
                user.add(user1);
                playlistListenerMap.put(pl, user);
                present = true;
                break;
            }
        }

        if(!present) throw new RuntimeException("User does not exist");

        return pl;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        Playlist pt = new Playlist(title);
        playlists.add(pt); //create the playList.

        List<Song> song1 = new ArrayList<>();
        for(Song song : songs){
           for(String titles : songTitles){
               if(song.getTitle().equals(titles)) {
                   song1.add(song);
                   break;
               }
           }
        }

        playlistSongMap.put(pt, songs);

        boolean present = false;
        for(User user : users){
            if(user.getMobile().equals(mobile)){
                creatorPlaylistMap.put(user, pt);
                List<User> users1 = new ArrayList<>();
                users1.add(user);
                playlistListenerMap.put(pt, users1);
                present = true;
                break;
            }
        }

        if(!present) throw new RuntimeException("User does not exist");

        return pt;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        boolean userExist = false;
        boolean playlistExist = false;
        Playlist playlistName = null;
        User uName = null;

        for(Playlist playlist : playlists) {
            if(playlist.getTitle().equals(playlistTitle)){
                playlistExist = true;
                playlistName = playlist;
                break;
            }
        }

        if(!playlistExist) throw new RuntimeException("Playlist does not exist");

        for(User u : users){
            if(u.getMobile().equals(mobile)){
                userExist = true;
                uName = u;
                break;
            }
        }

        if(!userExist) throw new RuntimeException("User does not exist");

        boolean isUserCreator = false;
        boolean isUserListener = false;

        if(creatorPlaylistMap.containsKey(uName)) isUserCreator = true;
            for(User u : playlistListenerMap.get(playlistName)){
                if(u == uName) isUserListener = true;
            }

        if(!isUserCreator) creatorPlaylistMap.put(uName, playlistName);

        if(!isUserListener){
            List<User> userList = new ArrayList<>();
            if(playlistListenerMap.containsKey(playlistName)) userList = playlistListenerMap.get(playlistName);
            userList.add(uName);
            playlistListenerMap.put(playlistName, userList);
        }

        return playlistName;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        boolean userPresent = false;
        boolean songPresent = false;
        Song updateSong = null;
        boolean isSongLike = false;
        Song spresent = null;
        User upresent = null;

        for(Song s : songs){
            if(s.getTitle().equals(songTitle)) {
                spresent = s;
                songPresent = true;
                break;
            }
        }

        for(User u : users){
            if(u.getMobile().equals(mobile)){
                upresent = u;
                userPresent = true;
                break;
            }
        }

        for(Song s : songLikeMap.keySet()){
            for(User u : songLikeMap.get(s)){
                if(u.getMobile().equals(mobile)){
                    isSongLike = true;
                    break;
                }
            }
        }

        if(!userPresent) throw new RuntimeException("User does not exist");
        if(!songPresent) throw new RuntimeException("Song does not exist");

        if(!isSongLike){
            spresent.setLikes(spresent.getLikes() + 1);

            List<User> users1 = new ArrayList<>();
            if (songLikeMap.containsKey(spresent)) users1 = songLikeMap.get(spresent);
            users1.add(upresent);
            songLikeMap.put(spresent, users1);
        }

        //artist like the song.
        Album albumName = null;  //find the album in which song is present.
        for(Album album : albumSongMap.keySet()){
            for(Song songs : albumSongMap.get(album)){
                if(songs.getTitle().equals(songTitle)){
                    albumName = album;
                    break;
                }
            }
        }

        //find the artist and incresae the likes.
        for(Artist artist : artistAlbumMap.keySet()){
            for(Album album : artistAlbumMap.get(artist)){
                if(albumName == album){
                    artist.setLikes(artist.getLikes()+1);
                    break;
                }
            }
        }

        return updateSong;
    }

    public String mostPopularArtist() {
        String artistName = null;
        int noOfLikes = 0;
        for(int i=0; i<artists.size(); i++){
            if(noOfLikes <= artists.get(i).getLikes()){
                noOfLikes = artists.get(i).getLikes();
                artistName = artists.get(i).getName();
            }
        }

        return artistName;
    }

    public String mostPopularSong() {
        String songTitle = null;
        int noOfLikes = 0;
        for(int i=0; i<songs.size(); i++){
            if(noOfLikes <= songs.get(i).getLikes()){
                noOfLikes = songs.get(i).getLikes();
                songTitle = songs.get(i).getTitle();
            }
        }
        return songTitle;
    }
}
