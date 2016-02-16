package boombotix.com.thundercloud.bluetooth.playback;

import boombotix.com.thundercloud.model.playback.MusicPlaybackAction;
import boombotix.com.thundercloud.model.playback.MusicPlaybackState;
import rx.Observable;

/**
 * Implementation of {@link MusicPlaybackBluetoothEndpoint} that sends and receives playback info over bluetooth
 */
public class MusicPlaybackBoombotBluetoothEndpoint implements MusicPlaybackBluetoothEndpoint{

    @Override
    public Observable<Boolean> controlPlayback(MusicPlaybackAction action) {
        return null;
    }

    @Override
    public Observable<MusicPlaybackState> getPlaybackState() {
        return null;
    }

    @Override
    public Observable<Boolean> setVolume(int volume) {
        return null;
    }

    @Override
    public Observable<Boolean> updateQueue() {
        return null;
    }
}
