package boombotix.com.thundercloud.dependencyinjection.component;

import android.content.SharedPreferences;

import com.google.gson.Gson;

import javax.inject.Singleton;

import boombotix.com.thundercloud.ThundercloudApplication;
import boombotix.com.thundercloud.api.SpotifyAuthenticationEndpoint;
import boombotix.com.thundercloud.api.SpotifyTrackEndpoint;
import boombotix.com.thundercloud.authentication.AuthManager;
import boombotix.com.thundercloud.bluetooth.BluetoothMessageWrapper;
import boombotix.com.thundercloud.bluetooth.authentication.BoombotAuthenticationBluetoothEndpoint;
import boombotix.com.thundercloud.bluetooth.authentication.MockAuthenticationEndpoint;
import boombotix.com.thundercloud.bluetooth.playback.BoombotMusicPlaybackBluetoothEndpoint;
import boombotix.com.thundercloud.bluetooth.wifi.BoombotWifiBluetoothEndpoint;
import boombotix.com.thundercloud.dependencyinjection.module.ApiModule;
import boombotix.com.thundercloud.dependencyinjection.module.ApplicationModule;
import boombotix.com.thundercloud.dependencyinjection.module.BluetoothModule;
import boombotix.com.thundercloud.dependencyinjection.module.RepositoryModule;
import boombotix.com.thundercloud.houndify.request.HoundifyRequestTransformer;
import boombotix.com.thundercloud.houndify.response.HoundifyDeserializer;
import boombotix.com.thundercloud.houndify.response.HoundifyResponseParser;
import boombotix.com.thundercloud.houndify.response.HoundifyModelExtractor;
import boombotix.com.thundercloud.playback.MusicControls;
import boombotix.com.thundercloud.playback.PlaybackQueue;
import boombotix.com.thundercloud.playback.SpotifyPlayer;
import boombotix.com.thundercloud.wifi.WifiScanResultsBroadcastReciever;
import boombotix.com.thundercloud.wifi.WifiScanResultsObservableContract;
import dagger.Component;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

/**
 * Application level injection class (Component). The lifetime of this component is the lifetime of
 * the application, and any global modules should be defined as part of this module.
 *
 * For Modules/injections that only need to live through a given activity lifecycle (which should in
 * theory be anything which is not strictly global), see {@link ActivityComponent}
 *
 * Created by kenton on 1/24/16.
 */
@Singleton
@Component(modules =
        {
                ApplicationModule.class,
                RepositoryModule.class,
                ApiModule.class,
                BluetoothModule.class
        }
)
public interface ApplicationComponent {


    final class Initializer {

        public static ApplicationComponent init(ThundercloudApplication application) {
            return DaggerApplicationComponent.builder()
                    .applicationModule(new ApplicationModule(application))
                    .apiModule(new ApiModule())
                    .repositoryModule(new RepositoryModule(application))
                    .bluetoothModule(new BluetoothModule())
                    .build();
        }

        private Initializer() {
            // Block instantiation
        }
    }

    void inject(WifiScanResultsBroadcastReciever reciever);

    Gson gson();

    SpotifyApi spotifyApi();

    SharedPreferences sharedPreferences();

    AuthManager authManager();

    SpotifyService spotifyService();

    BoombotAuthenticationBluetoothEndpoint authenticationBoombotBluetoothEndpoint();

    BoombotMusicPlaybackBluetoothEndpoint musicPlaybackBoombotBluetoothEndpoint();

    BoombotWifiBluetoothEndpoint wifiBoombotBluetoothEndpoint();

    BluetoothMessageWrapper bluetoothMessageWrapper();

    MockAuthenticationEndpoint mockAuthenticationEndpoint();

    HoundifyResponseParser houndifyHelper();

    HoundifyModelExtractor houndifyModelExtractor();

    HoundifyDeserializer houndifyDeserializer();

    HoundifyRequestTransformer houndifyRequestAdapter();

    PlaybackQueue playbackQueue();

    MusicControls musicControls();

    SpotifyPlayer spotifyPlayer();

    SpotifyTrackEndpoint trackEndpoint();

    SpotifyAuthenticationEndpoint authenticationEndpoint();

    WifiScanResultsObservableContract wifiScanResultObservable();
}
