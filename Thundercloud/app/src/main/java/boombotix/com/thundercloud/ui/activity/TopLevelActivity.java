package boombotix.com.thundercloud.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.canelmas.let.DeniedPermission;
import com.canelmas.let.RuntimePermissionListener;
import com.canelmas.let.RuntimePermissionRequest;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import boombotix.com.thundercloud.R;
import boombotix.com.thundercloud.ui.base.BaseActivity;
import boombotix.com.thundercloud.ui.controller.VoiceSearchController;
import boombotix.com.thundercloud.ui.filter.Captureable;
import boombotix.com.thundercloud.ui.filter.ScreenBlurUiFilter;
import boombotix.com.thundercloud.ui.fragment.MusicListFragment;
import boombotix.com.thundercloud.ui.fragment.MusicPagerFragment;
import boombotix.com.thundercloud.ui.fragment.NowPlayingFragment;
import boombotix.com.thundercloud.ui.fragment.PlayerFragment;
import boombotix.com.thundercloud.ui.fragment.QueueFragment;
import boombotix.com.thundercloud.ui.fragment.VoiceSearchResultFragment;
import boombotix.com.thundercloud.ui.view.CropImageView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hugo.weaving.DebugLog;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;
import timber.log.Timber;

public class TopLevelActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        VoiceSearchController, RuntimePermissionListener {

    private FragmentManager fm;

    @Bind(R.id.searchText)
    EditText searchText;

    @Bind(R.id.tabs)
    TabLayout yourMusicTabs;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.toolbar_background)
    CropImageView toolbarBackground;

    @Bind(R.id.main_fragment)
    FrameLayout blur;
    @Bind(R.id.app_bar)
    AppBarLayout appBarLayout;

    @Inject
    ScreenBlurUiFilter screenBlurUiFilter;

    private Subject<Boolean, Boolean> screenCreatedObservable;

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_level);
        ButterKnife.bind(this);
        getActivityComponent().inject(this);

        screenCreatedObservable = PublishSubject.create();
        setupDelayedToolbarBlurEffect();
        setSupportActionBar(toolbar);

        fm = getSupportFragmentManager();
        Fragment mainFragment = fm.findFragmentById(R.id.main_fragment);
        if (mainFragment == null) {

            // TODO actually have  a main fragment
            mainFragment = NowPlayingFragment.newInstance();
            fm.beginTransaction()
                    .add(R.id.main_fragment, mainFragment)
                    .commit();
        }

        Fragment playerFragment = fm.findFragmentById(R.id.player_fragment);
        if (playerFragment == null) {
            playerFragment = new PlayerFragment();
            fm.beginTransaction()
                    .add(R.id.player_fragment, playerFragment)
                    .commit();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setQueueFragment();

    }

    private void attachPlayerFragment() {
        Fragment playerFragment = fm.findFragmentById(R.id.player_fragment);
        if (playerFragment == null) {
            playerFragment = new PlayerFragment();
            fm.beginTransaction()
                    .add(R.id.player_fragment, playerFragment, PlayerFragment.TAG)
                    .commit();
        }
    }

    public Observable<Boolean> getMainViewCreatedObservable() {
        return this.screenCreatedObservable;
    }

    public void alertMainViewCreated() {
        this.screenCreatedObservable.onNext(true);
    }

    private void attachMainFragment() {
        fm = getSupportFragmentManager();
        Fragment mainFragment = fm.findFragmentById(R.id.main_fragment);
        if (mainFragment == null) {
            mainFragment = NowPlayingFragment.newInstance();
            fm.beginTransaction()
                    .add(R.id.main_fragment, mainFragment, NowPlayingFragment.TAG)
                    .commit();
        }
    }

    private void setupDelayedToolbarBlurEffect() {
        getMainViewCreatedObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> blurToolbar());
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.nav_nowplaying) {
            fm.beginTransaction()
                    .replace(R.id.main_fragment, NowPlayingFragment.newInstance())
                    .commit();
        } else if (id == R.id.nav_playlists) {
            changeMusicPagerPage(MusicListFragment.PLAYLIST_SECTION);
        } else if (id == R.id.nav_songs) {
            changeMusicPagerPage(MusicListFragment.SONGS_SECTION);
        } else if (id == R.id.nav_albums) {
            changeMusicPagerPage(MusicListFragment.ALBUMS_SECTION);
        } else if (id == R.id.nav_artists) {
            changeMusicPagerPage(MusicListFragment.ARTISTS_SECTION);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Applies blur effect to toolbar background.
     */
    private void blurToolbar() {
        View toBlur = getCaptureableView();
        if (toBlur != null) {
            this.toolbarBackground.setImageBitmap(this.screenBlurUiFilter.blurView(getCaptureableView()));
            this.toolbarBackground.setOffset(0, 0);
        }
    }

    private void changeMusicPagerPage(int page){
        Fragment musicPagerFragment =  MusicPagerFragment.newInstance(page);
        fm.beginTransaction()
                .replace(R.id.main_fragment, musicPagerFragment)
                .commit();
    }

    private void setQueueFragment(){
        Fragment queueFragment = QueueFragment.newInstance();
        fm.beginTransaction()
                .replace(R.id.queue_container, queueFragment)
                .commit();
    }

    /**
     * Adds fragment overlay for te voice search results, if it already exists it  will remove it
     * and recreate it.
     */
    public void addVoiceSearchResultFragment() {
        removeFragmentByTag(VoiceSearchResultFragment.TAG);

        // add overlay
        fm.beginTransaction()
                .add(R.id.overlay_fragment,
                        VoiceSearchResultFragment.newInstance(),
                        VoiceSearchResultFragment.TAG)
                .commit();
        fm.executePendingTransactions();
    }

    private void findAndRefreshFragment(String tag) {
        Fragment voiceSearchResultFragment = fm.findFragmentByTag(tag);
        if (voiceSearchResultFragment != null) {
            fm.beginTransaction().remove(voiceSearchResultFragment).commit();
        }
    }

    /**
     * Updates text on the voice search fragment overlay
     */
    public void updateVoiceSearchResultFragmentText(String s) {
        setAndGetVoiceSearchResultFragment().updateText(s);
    }

    /**
     * sets voice search fragment text to the query and allows editing via input box
     */
    public void setVoiceSearchResultFragmentQuery(String s) {
            setAndGetVoiceSearchResultFragment().setQuery(s);
    }

    /**
     * Gets viuce search result fragment, if it doesn't exist it will create it first
     *
     * @return the voice search result fragment
     */
    public VoiceSearchResultFragment setAndGetVoiceSearchResultFragment() {
        Fragment fragment = fm.findFragmentByTag(VoiceSearchResultFragment.TAG);
        if (fragment == null) {
            addVoiceSearchResultFragment();
            fragment = fm.findFragmentByTag(VoiceSearchResultFragment.TAG);
        }
        return (VoiceSearchResultFragment) fragment;
    }

    /**
     * Searches fragment by tag and removes it
     */
    public void removeFragmentByTag(String tag) {
        Fragment fragment = fm.findFragmentByTag(tag);
        if (fragment != null) {
            fm.beginTransaction().remove(fragment).commit();
        }
    }

    /**
     * Retrieves player fragment and stops any in process searches
     */
    public void stopPlayerSearch() {
        Fragment fragment = fm.findFragmentByTag(PlayerFragment.TAG);
        if (fragment != null) {
            ((PlayerFragment) fragment).stopSearch();
        }
    }

    /**
     * Hides search input from toolbar
     *
     */
    public void hideSearch(){
        searchText.setVisibility(View.GONE);
        this.toolbarBackground.getLayoutParams().height = Math.round(50 * getResources()
                .getDisplayMetrics().density);
    }

    public void showSearch(){
        searchText.setVisibility(View.VISIBLE);
        this.toolbarBackground.getLayoutParams().height = Math.round(70 * getResources()
                .getDisplayMetrics().density);
    }

    public TabLayout getTabs() {
        return this.yourMusicTabs;
    }

    public void showTabs() {
        this.yourMusicTabs.setVisibility(View.VISIBLE);
    }

    public void hideTabs() {
        this.yourMusicTabs.setVisibility(View.GONE);
    }

    @OnClick(R.id.searchText)
    protected void onClickSearch() {
        blurToolbar();
    }

    public void setToolbarTitle(String title) {
        toolbar.setTitle(title);
    }

    /**
     * Callthrough to return the captureable view of the main content fragment,
     * if it implements {@link Captureable}
     *
     * May return null.
     *
     * @return
     *          Captureable view of content fragment, OR null.
     */
    public @Nullable View getCaptureableView() {
        Fragment contentFragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment);
        if (contentFragment != null && contentFragment instanceof Captureable) {
            return ((Captureable) contentFragment).captureView();
        }
        return null;
    }


    @Override
    public void onShowPermissionRationale(List<String> permissionList, RuntimePermissionRequest permissionRequest) {
        Timber.d("permissions accepted for " + Arrays.toString(permissionList.toArray()));
    }

    @Override
    public void onPermissionDenied(List<DeniedPermission> deniedPermissionList) {
        Timber.d("Permissions denied for " + Arrays.toString(deniedPermissionList.toArray()));
    }
}
