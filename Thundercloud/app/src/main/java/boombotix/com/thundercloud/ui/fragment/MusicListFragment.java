package boombotix.com.thundercloud.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import javax.inject.Inject;

import boombotix.com.thundercloud.R;
import boombotix.com.thundercloud.authentication.AuthManager;
import boombotix.com.thundercloud.model.AuthRefreshResponse;
import boombotix.com.thundercloud.ui.activity.LoginActivity;
import boombotix.com.thundercloud.ui.adapter.YourMusicAdapter;
import boombotix.com.thundercloud.ui.base.BaseFragment;
import boombotix.com.thundercloud.ui.fragment.yourmusic.subscriber.AlbumsSubscriber;
import boombotix.com.thundercloud.ui.fragment.yourmusic.subscriber.ArtistsSubscriber;
import boombotix.com.thundercloud.ui.fragment.yourmusic.subscriber.PlaylistsSubscriber;
import boombotix.com.thundercloud.ui.fragment.yourmusic.subscriber.SongsSubscriber;
import butterknife.Bind;
import butterknife.ButterKnife;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsCursorPager;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.SavedAlbum;
import kaaes.spotify.webapi.android.models.SavedTrack;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by jsaucedo on 2/1/16.
 */
public class MusicListFragment extends BaseFragment implements AuthManager.AuthRefreshRespCallback,
        PlaylistsSubscriber.PlaylistsSubscriberCallback, SongsSubscriber.SongsSubscriberCallback,
        AlbumsSubscriber.AlbumsSubscriberCallback, ArtistsSubscriber.ArtistsSubscriberCallback {

    private final String TAG = "MusicListFragment";
    private static final String ARG_SECTION = "section";
    public static final int PLAYLIST_SECTION = 0;
    public static final int SONGS_SECTION = 1;
    public static final int ALBUMS_SECTION = 2;
    public static final int ARTISTS_SECTION = 3;
    @Bind(R.id.recycler)
    RecyclerView recyclerView;
    @Inject
    AuthManager authManager;
    @Inject
    SpotifyService spotifyService;
    @Inject
    SpotifyApi spotifyApi;

    public MusicListFragment() {
    }


    public static MusicListFragment newInstance(int sectionNumber) {
        MusicListFragment fragment = new MusicListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getSupportActivity().getActivityComponent().inject(this);
        if(authManager.getUserId() != null) {
            initView();
        }
        else{
            startActivity(new Intent(getActivity(), LoginActivity.class));
        }
    }

    private void initView() {
        spotifyApi.setAccessToken(authManager.getAccessToken());
        recyclerView.setAdapter(new YourMusicAdapter(getActivity(), new ArrayList<>()));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        switch(getArguments().getInt(ARG_SECTION)){
            case PLAYLIST_SECTION:
                displayPlaylistContent();
                break;
            case SONGS_SECTION:
                displaySongsContent();
                break;
            case ALBUMS_SECTION:
                displayAlbumsContent();
                break;
            case ARTISTS_SECTION:
                displayArtistsContent();
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_music_pager_list, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    private void displayArtistsContent() {
        Observable.defer(() -> authManager.getValidAccessToken()
                .flatMap(authRefreshResponse -> Observable.just(spotifyService.getFollowedArtists())))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ArtistsSubscriber(this));
    }

    private void displayAlbumsContent() {
        Observable.defer(() -> authManager.getValidAccessToken()
                .flatMap(authRefreshResponse -> Observable.just(spotifyService.getMySavedAlbums())))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new AlbumsSubscriber(this));
    }

    private void displaySongsContent() {
        Observable.defer(() -> authManager.getValidAccessToken()
                .flatMap(authRefreshResponse -> Observable.just(spotifyService.getMySavedTracks())))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SongsSubscriber(this));
    }

    /**
     * Formats duration of song time hh:mm:ss
     * prettytime plugin cannot be used here because it only offers relative time formatting
     * @param l duration in seconds
     * @return string formatted hh:mm:ss
     */
    private String prettyTime(long l) {
        String time = "";
        long seconds = l % 60;
        long minutes = l/60 % 60;
        long hours = l/3600 % 60;

        if(hours > 0){
            time += ((hours < 10) ? "0" : "") + hours + ":";
        }
        time += ((minutes < 10 && hours > 0) ? "0" : "") + minutes + ":";
        time += ((seconds < 10) ? "0" : "") + seconds;
        return time;
    }

    private void displayPlaylistContent() {
        Observable.defer(() -> authManager.getValidAccessToken()
                .flatMap(authRefreshResponse -> Observable.just(spotifyService.getPlaylists(authManager.getUserId()))))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new PlaylistsSubscriber(this));
    }

    @Override
    public void onSuccess(AuthRefreshResponse authRefreshResponse) {
        initView();
    }

    @Override
    public void onError(Throwable error) {
        Log.e(TAG, error.getMessage());
        error.printStackTrace();
    }

    @Override
    public void playlistsResponse(Pager<PlaylistSimple> playlistSimplePager) {
        ArrayList < Pair < String, String >> items = new ArrayList<>();
        for (PlaylistSimple playlistSimple : playlistSimplePager.items) {
            items.add(new Pair<>(playlistSimple.name,
                    getResources().getQuantityString(R.plurals.songs,
                            playlistSimple.tracks.total, playlistSimple.tracks.total)));
        }

        recyclerView.setAdapter(new YourMusicAdapter(getActivity(), items));
    }

    @Override
    public void songsResponse(Pager<SavedTrack> savedTrackPager) {
        ArrayList<Pair<String, String>> items = new ArrayList<>();
        for (SavedTrack savedTrack : savedTrackPager.items) {
            items.add(new Pair<>(savedTrack.track.name,
                    savedTrack.track.artists.get(0).name));

        }

        recyclerView.setAdapter(new YourMusicAdapter(getActivity(), items));
    }

    @Override
    public void albumsResponse(Pager<SavedAlbum> savedAlbumPager) {
        ArrayList<Pair<String, String>> items = new ArrayList<>();
        for (SavedAlbum savedAlbum : savedAlbumPager.items) {
            items.add(new Pair<>(savedAlbum.album.name, savedAlbum.album.release_date));
        }

        recyclerView.setAdapter(new YourMusicAdapter(getActivity(), items));
    }

    @Override
    public void artistsResponse(ArtistsCursorPager artistsCursorPager) {
        ArrayList<Pair<String, String>> items = new ArrayList<>();
        for (Artist artist : artistsCursorPager.artists.items) {
            items.add(new Pair<>(artist.name, TextUtils.join(", ", artist.genres)));
        }
        recyclerView.setAdapter(new YourMusicAdapter(getActivity(), items));
    }
}
