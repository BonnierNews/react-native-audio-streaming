package com.audioStreaming;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.MetadataRenderer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.io.IOException;

public class Signal extends Service implements ExoPlayer.EventListener, MetadataRenderer.Output, ExtractorMediaSource.EventListener {
    private static final String TAG = "ReactNative";

    // Notification
    private Class<?> clsActivity;

    // Player
    private SimpleExoPlayer player = null;

    public static final String BROADCAST_PLAYBACK_STOP = "stop",
            BROADCAST_PLAYBACK_PLAY = "pause",
            BROADCAST_EXIT = "exit";

    private final IBinder binder = new RadioBinder();
    private final SignalReceiver receiver = new SignalReceiver(this);
    private Context context;
    private String streamingURL;
    private EventsReceiver eventsReceiver;
    private ReactNativeAudioStreamingModule module;

    private TelephonyManager phoneManager;
    private PhoneListener phoneStateListener;

    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    public void onCreate() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_PLAYBACK_STOP);
        intentFilter.addAction(BROADCAST_PLAYBACK_PLAY);
        intentFilter.addAction(BROADCAST_EXIT);
        registerReceiver(this.receiver, intentFilter);
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }
    
    public void setData(Context context, ReactNativeAudioStreamingModule module) {
        this.context = context;
        this.clsActivity = module.getClassActivity();
        this.module = module;
        
        this.eventsReceiver = new EventsReceiver(this.module);
        
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.CREATED));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.IDLE));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.DESTROYED));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.STARTED));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.CONNECTING));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.PLAYING));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.READY));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.STOPPED));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.PAUSED));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.COMPLETED));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.ERROR));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.BUFFERING_START));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.BUFFERING_END));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.METADATA_UPDATED));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.ALBUM_UPDATED));
        registerReceiver(this.eventsReceiver, new IntentFilter(Mode.STREAMING));

        this.phoneStateListener = new PhoneListener(this.module);
        this.phoneManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (this.phoneManager != null) {
            this.phoneManager.listen(this.phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        Log.d("onPlayerStateChanged", ""+playbackState);

        addProgressListener();

        switch (playbackState) {
            case ExoPlayer.STATE_IDLE:
                sendBroadcast(new Intent(Mode.IDLE));
                break;
            case ExoPlayer.STATE_BUFFERING:

                sendBroadcast(new Intent(Mode.BUFFERING_START));
                break;
            case ExoPlayer.STATE_READY:
                if (this.player != null && this.player.getPlayWhenReady()) {
                    sendBroadcast(new Intent(Mode.PLAYING));
                } else {
                    sendBroadcast(new Intent(Mode.READY));
                }
                break;
            case ExoPlayer.STATE_ENDED:
                sendBroadcast(new Intent(Mode.STOPPED));
                break;
        }

    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {}
    
    
    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Log.d(TAG, error.getMessage());
        sendBroadcast(new Intent(Mode.ERROR));
    }

    @Override
    public void onPositionDiscontinuity() {

    }

    private static String getDefaultUserAgent() {
        StringBuilder result = new StringBuilder(64);
        result.append("Dalvik/");
        result.append(System.getProperty("java.vm.version")); // such as 1.1.0
        result.append(" (Linux; U; Android ");

        String version = Build.VERSION.RELEASE; // "1.0" or "3.4b5"
        result.append(version.length() > 0 ? version : "1.0");

        // add the model for the release build
        if ("REL".equals(Build.VERSION.CODENAME)) {
            String model = Build.MODEL;
            if (model.length() > 0) {
                result.append("; ");
                result.append(model);
            }
        }
        String id = Build.ID; // "MASTER" or "M4-rc20"
        if (id.length() > 0) {
            result.append(" Build/");
            result.append(id);
        }
        result.append(")");
        return result.toString();
    }

    private void addProgressListener() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (player != null && player.getPlayWhenReady()) {
                    double position = (getCurrentPosition() / 1000);
                    double duration = (getDuration() / 1000);
                    Intent StreamingIntent = new Intent(Mode.STREAMING);
                    StreamingIntent.putExtra("progress", position);
                    StreamingIntent.putExtra("duration", duration);
                    sendBroadcast(StreamingIntent);
                    handler.postDelayed(runnable, 1000);

                    Log.d(TAG, "run: " + player.getPlayWhenReady());
                }
            }
        };
        handler.postDelayed(runnable, 0);
    }

    private void removeProgressListener() {
        Log.i(TAG, "removeProgressListner");
        handler.removeCallbacks(runnable);
        runnable = null;
    }
    
    /**
     *  Player controls
     */
    
    public void play(String url) {
        if (player != null ) {
            player.setPlayWhenReady(false);
            player.stop();
            player.seekTo(0);
        }
        
        boolean playWhenReady = true; // TODO Allow user to customize this
        this.streamingURL = url;
        
        // Create player
        Handler mainHandler = new Handler();
        TrackSelector trackSelector = new DefaultTrackSelector();
        this.player = ExoPlayerFactory.newSimpleInstance(this.getApplicationContext(), trackSelector);
        
        // Create source
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this.getApplication(), getDefaultUserAgent(), bandwidthMeter);
        MediaSource audioSource = new ExtractorMediaSource(Uri.parse(this.streamingURL), dataSourceFactory, extractorsFactory, mainHandler, this);
        
        // Start preparing audio
        player.prepare(audioSource);
        player.addListener(this);
        player.setPlayWhenReady(playWhenReady);
    }
    
    public void start() {
        player.setPlayWhenReady(true);
    }
    
    public void pause() {
        player.setPlayWhenReady(false);
        sendBroadcast(new Intent(Mode.STOPPED));
    }
    
    public void resume() {
        player.setPlayWhenReady(true);
    }
    
    public void stop() {
        player.setPlayWhenReady(false);
        sendBroadcast(new Intent(Mode.STOPPED));
    }
    
    public boolean isPlaying() {
        return player != null && player.getPlayWhenReady() && player.getPlaybackState() != ExoPlayer.STATE_ENDED;
    }
    
    public long getDuration() {
        return player != null ? player.getDuration() : new Long(0);
    }
    
    public long getCurrentPosition() {
        return player != null ? player.getCurrentPosition() : new Long(0);
    }
    
    public int getBufferPercentage() {
        return player.getBufferedPercentage();
    }
    
    public void seekTo(long timeMillis) {
        player.seekTo(timeMillis);
    }
    
    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void setPlaybackRate(float speed) {
        PlaybackParameters pp = new PlaybackParameters(speed, 1);
        player.setPlaybackParameters(pp);
    }
    
    /**
     *  Meta data information
     */
    
    @Override
    public void onMetadata(Metadata metadata) {

    }
    
    /**
     *  Notification control
     */
    
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }
    
    // Notification
    private PendingIntent makePendingIntent(String broadcast) {
        Intent intent = new Intent(broadcast);
        return PendingIntent.getBroadcast(this.context, 0, intent, 0);
    }

    public class RadioBinder extends Binder {
        public Signal getService() {
            return Signal.this;
        }
    }

    public String getStreamingURL() {
        return this.streamingURL;
    }

    @Override
    public void onLoadError(IOException error) {
        Log.e(TAG, error.getMessage());
    }

}
