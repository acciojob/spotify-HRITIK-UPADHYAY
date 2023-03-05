package com.driver;

import java.time.LocalDate;
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
        for(Artist artist : artists){
            if(artist.getName().equals(artistName)){
                albums.add(new Album(title)); //new Album created.
                present = true;

                //update the artistAlbumHashMap.
                List<Album> albums = artistAlbumMap.get(artist);
                albums.add(new Album(title));
                artistAlbumMap.put(artist, albums);

                break;
            }
        }

        if(!present){
            artists.add(new Artist(artistName));
            albums.add(new Album(title));
        }

        return albums.get(artists.size()-1);
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        boolean present = false;
        for(Album album : albums){
            if(album.getTitle().equals(albumName)){
                songs.add(new Song(title, length));
                present = true;

                //update the albumSongHashMap.
                List<Song> songs = albumSongMap.get(album);
                songs.add(new Song(title, length));
                albumSongMap.put(album, songs);
                break;
            }
        }

        if(!present) throw new RuntimeException("Album does not exist");

        return songs.get(songs.size()-1);
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        playlists.add(new Playlist(title)); //create a playlist.

        List<Song> song1 = new ArrayList<>();
        if(playlistSongMap.containsKey(new Playlist(title))) song1 = playlistSongMap.get(new Playlist(title));
        for(Song song : songs){
            if(song.getLength() == length){
                song1.add(song);
            }
        }

        playlistSongMap.put(new Playlist(title), song1);

        boolean present = false;
        for(User user : users){
            if(user.getMobile().equals(mobile)){
                creatorPlaylistMap.put(user, new Playlist(title));
                List<User> users1 = playlistListenerMap.get(new Playlist(title));
                users1.add(user);
                playlistListenerMap.put(new Playlist(title), users1);
                present = true;
                break;
            }
        }

        if(!present) throw new RuntimeException("User does not exist");

        return playlists.get(playlists.size()-1);
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        playlists.add(new Playlist(title)); //create the playList.

        List<Song> songs = new ArrayList<>();
        if(playlistSongMap.containsKey(new Playlist(title))) songs = playlistSongMap.get(new Playlist(title));
        for(Song song : songs){
            if(song.getTitle().equals(title)){
                songs.add(song);
            }
        }

        playlistSongMap.put(new Playlist(title), songs);

        boolean present = false;
        for(User user : users){
            if(user.getMobile().equals(mobile)){
                creatorPlaylistMap.put(user, new Playlist(title));
                List<User> users1 = playlistListenerMap.get(new Playlist(title));
                users1.add(user);
                playlistListenerMap.put(new Playlist(title), users1);
                present = true;
                break;
            }
        }

        if(!present) throw new RuntimeException("User does not exist");

        return playlists.get(playlists.size()-1);
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        boolean userExist = false;
        boolean playlistExist = false;
        Playlist playlistName = null;
        for(Playlist playlist : playlists){
            if(playlist.getTitle().equals(playlistTitle)){
                playlistExist = true;
                for(User user : users){
                    if(user.getMobile().equals(mobile)){
                        userExist = true;
                        if(!creatorPlaylistMap.containsKey(user)){
                            boolean userListener = false;
                            for(User user1 : playlistListenerMap.get(playlist)){
                                if(user == user1){
                                    userListener = true;
                                    break;
                                }
                            }

                            if(userListener) break;
                            else {
                                List<User> user2 = playlistListenerMap.get(playlist);
                                user2.add(user);
                                playlistListenerMap.put(playlist, user2);
                            }
                        }
                    }
                }
                if(!userExist) throw new RuntimeException("User does not exist");
                playlistName = playlist;
            }
        }
        if(!playlistExist) throw new RuntimeException("Playlist does not exist");

        return playlistName;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        boolean userPresent = false;
        boolean songPresent = false;
        Song updateSong = null;
        boolean isSongLike = false;

        for(Song song : songs){
            if(song.getTitle().equals(songTitle)){
                songPresent = true;
                for(User user : songLikeMap.get(song)){
                    if(user.getMobile().equals(mobile)){
                        userPresent = true;
                        updateSong = song;
                        isSongLike = true;
                        break;
                    }
                }
            }
        }

        if(!isSongLike){
            for(Song song : songs){
                if(song.getTitle().equals(songTitle)){
                    songPresent = true;
                    for(User user : users){
                        if(user.getMobile().equals(mobile)){
                            userPresent = true;
                            song.setLikes(song.getLikes()+1);

                            List<User> users = songLikeMap.get(song);
                            users.add(user);
                            songLikeMap.put(song, users);
                            break;
                        }
                    }
                }
            }
        }

        if(!userPresent) throw new RuntimeException("User does not exist");
        if(!songPresent) throw new RuntimeException("Song does not exist");

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
        for(Artist artist : artists){
            if(noOfLikes < artist.getLikes()){
                noOfLikes = artist.getLikes();
                artistName = artist.getName();
            }
        }

        return artistName;
    }

    public String mostPopularSong() {
        String songTitle = null;
        int noOfLikes = 0;
        for(Song song : songs){
            if(noOfLikes < song.getLikes()){
                noOfLikes = song.getLikes();
                songTitle = song.getTitle();
            }
        }

        return songTitle;
    }
}
