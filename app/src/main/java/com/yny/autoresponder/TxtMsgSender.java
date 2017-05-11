package com.yny.autoresponder;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.telephony.gsm.SmsManager;

import com.yny.autoresponder.history.SentSmsLogger;

import java.io.IOException;

public class TxtMsgSender {
    private static Context ctx;
    private static String profile = "Main";

    private AutoResponderDbAdapter dbAdapter;

    private AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;

    private TxtMsgSender(AutoResponderDbAdapter dbAdapter) {
        this.dbAdapter = dbAdapter;
    }

    public void playBackgroundMusic() {
        mAudioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setParameters("incall_music_enabled=true");
        //mAudioManager.setParameters("Set_BGS_UL_Mute=1");
        //mAudioManager.setParameters("Set_SpeechCall_UL_Mute=1");
        mAudioManager.setSpeakerphoneOn(true);
        //mAudioManager.setMode(AudioManager.STREAM_VOICE_CALL);
        //mAudioManager.setMicrophoneMute(false);

        playRing(ctx);

        int result = mAudioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

        if (result == AudioManager.AUDIOFOCUS_GAIN) {
            resume();
        }
    }

    private void playRing(Context ctx) {

        if (mMediaPlayer == null) {
            try {
                mMediaPlayer = new MediaPlayer();
                AssetManager assetManager = ctx.getAssets();
                AssetFileDescriptor fileDescriptor = assetManager.openFd("konghao.wav");
                mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(),
                        fileDescriptor.getStartOffset());
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.prepare();
                mMediaPlayer.setOnCompletionListener(completionListener);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    private void resume() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    private void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    private void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    public void sendTextMessageIfPossible(String telNumber) {
        if (shouldSendMessage(telNumber)) {
            String messageBody = dbAdapter.fetchMessageBody(profile);

            SmsManager smsMgr = SmsManager.getDefault();
            smsMgr.sendTextMessage(telNumber, null, messageBody, null, null);
            saveMessageToHistory(telNumber, messageBody);

            incrementCounter();
        }
    }

    private void incrementCounter() {
        Intent intent = new Intent(NotificationArea.INCREMENT);
        ctx.sendBroadcast(intent);
    }

    private boolean shouldSendMessage(String telNumber) {
        return telNumber != null && telNumber.length() > 0;
    }

    private void saveMessageToHistory(String telNumber, String messageBody) {
        Intent sentSmsLogger = new Intent(TxtMsgSender.ctx, SentSmsLogger.class);
        sentSmsLogger.putExtra("telNumber", telNumber);
        sentSmsLogger.putExtra("messageBody", messageBody);
        TxtMsgSender.ctx.startService(sentSmsLogger);
    }

    public static void setProfile(String profile) {
        TxtMsgSender.profile = profile;
    }

    public static String getProfile() {
        return profile;
    }

    public static TxtMsgSender createAndSetUp(Context ctx) {
        TxtMsgSender.ctx = ctx;

        AutoResponderDbAdapter dbAdapter = AutoResponderDbAdapter.initializeDatabase(ctx);
        TxtMsgSender txtMsgSender = new TxtMsgSender(dbAdapter);
        return txtMsgSender;
    }

    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                pause();
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                resume();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // mAm.unregisterMediaButtonEventReceiver(RemoteControlReceiver);
                mAudioManager.abandonAudioFocus(afChangeListener);
                // Stop playback
                stop();
            }
        }
    };

    MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer player) {
            if (!player.isLooping()) {
                mAudioManager.abandonAudioFocus(afChangeListener);
            }
        }
    };

}
