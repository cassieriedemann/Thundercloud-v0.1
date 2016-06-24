package boombotix.com.thundercloud.ui.fragment.wifi;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;

import javax.inject.Inject;

import boombotix.com.thundercloud.R;
import boombotix.com.thundercloud.ThundercloudApplication;
import boombotix.com.thundercloud.bluetooth.connection.BluetoothClassicConnection;
import boombotix.com.thundercloud.bluetooth.wifi.CredentialsFormatter;
import boombotix.com.thundercloud.model.constants.BluetoothConstants;
import boombotix.com.thundercloud.ui.base.BaseFragment;
import butterknife.Bind;
import butterknife.ButterKnife;
import okio.BufferedSink;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment must implement the
 * {@link WifiConnectFragmentCallbacks} interface to handle interaction events. Use the {@link
 * WifiConnectFragment#newInstance} factory method to create an instance of this fragment.
 */
public class WifiConnectFragment extends BaseFragment {

    private static final String ARG_NETWORK_NAME = "networkName";
    private static final String ARG_SPEAKER_NAME = "speakerName";
    private static final String ARG_PASSWORD = "password";

    @Bind(R.id.connecting_message)
    TextView message;

    @Inject
    BluetoothClassicConnection speakerConnection;

    @Inject
    CredentialsFormatter credentialsFormatter;

    private String networkName;

    private String speakerName;

    private String password;

    private WifiConnectFragmentCallbacks listener;

    public WifiConnectFragment() {
        // Required empty public constructor
    }

    /**
     * @param networkName name of network that speaker should connect to
     * @param speakerName name of connected speaker, for display purposes
     * @return A new instance of fragment WifiConnectFragment.
     */
    public static WifiConnectFragment newInstance(String networkName, String speakerName, String password) {
        //todo take instance of WifiNetwork instead of name
        WifiConnectFragment fragment = new WifiConnectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NETWORK_NAME, networkName);
        args.putString(ARG_SPEAKER_NAME, speakerName);
        args.putString(ARG_PASSWORD, password);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            networkName = getArguments().getString(ARG_NETWORK_NAME);
            speakerName = getArguments().getString(ARG_SPEAKER_NAME);
            password = getArguments().getString(ARG_PASSWORD);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wifi_connect, container, false);
        ButterKnife.bind(this, view);

        getSupportActivity().getActivityComponent().inject(this);

        setConnectingMessage();
        setTitle();
        connectSpeakerToNetwork();
        return view;
    }

    private void setTitle() {
        String title = getString(R.string.wifi_connect_title, speakerName);
        getActivity().setTitle(title);
    }

    /**
     * Show the network name in the connecting message text
     */
    private void setConnectingMessage() {
        String text = getString(R.string.wifi_connect_connecting, networkName);
        message.setText(text);
    }

    /**
     * Attempts to connect the current speaker to the specific wifi network, sends message to {@link
     * WifiConnectFragmentCallbacks} after completion.
     */
    private void connectSpeakerToNetwork() {
        byte[] binaryCredentials = credentialsFormatter.prepareCredentialsForSpeaker(networkName, password);

        String macAddress = PreferenceManager
                .getDefaultSharedPreferences(ThundercloudApplication.instance())
                .getString(BluetoothConstants.BOOMBOT_SHAREDPREF_KEY, "");

        try {
            BufferedSink bufferedSink = speakerConnection.outputBuffer(macAddress);
            bufferedSink.write(binaryCredentials);
            bufferedSink.flush();
            bufferedSink.close();
            listener.onWifiConnectSuccess();
        } catch (IOException e) {
            e.printStackTrace();
            listener.onWifiConnectFailure();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof WifiConnectFragmentCallbacks) {
            listener = (WifiConnectFragmentCallbacks) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement WifiConnectFragmentCallbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    public interface WifiConnectFragmentCallbacks {

        void onWifiConnectSuccess();

        void onWifiConnectFailure();
    }
}
