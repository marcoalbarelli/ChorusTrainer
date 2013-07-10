/*
 * Copyright 2013 Marco Albarelli
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package eu.marcoalbarelli.android.chorustrainer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;
import com.leff.midi.event.meta.TrackName;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import eu.marcoalbarelli.android.chorustrainer.midi.MidiManager;
import eu.marcoalbarelli.android.chorustrainer.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    private final static String TAG  = FullscreenActivity.class.getSimpleName();

    private static MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void isClick(View v){
        switch(v.getId()){
            case R.id.main_choose_file:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*");
                startActivityForResult(intent, Constants.MAIN_FINDFILE_REQUEST_CODE);
                break;
            case R.id.dummy_button:
                if(mp!= null){
                    if(mp.isPlaying()){
                        mp.pause();

                    } else {
//                        mp.reset();
                        mp.start();
                    }
                }
                break;
            default:
                Log.d(TAG, "unhandled click event");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case Constants.MAIN_FINDFILE_REQUEST_CODE:

                String path =data.getData().getEncodedPath();
                if(path!=null){
                    File file = new File(path);

                    TextView t = (TextView) findViewById(R.id.main_filename_display);
                    MidiFile midi = null;
                    try {

                        LinearLayout parent = (LinearLayout) findViewById(R.id.tracks_buttons);
                        parent.removeAllViews();

                        midi = new MidiFile(file);
                        long length = midi.getLengthInTicks();
                        ArrayList<MidiTrack> tracks = midi.getTracks();
                        Iterator<MidiTrack> it = tracks.iterator();

                        while(it.hasNext()){

                            MidiTrack mt = it.next();
                            TreeSet<MidiEvent> events = mt.getEvents();
                            String trackName = "Track";
                            for (MidiEvent ev : events){
                                if(!ev.getClass().equals(NoteOn.class) && !ev.getClass().equals(NoteOff.class)){
                                    Log.d(TAG,ev.getClass().getSimpleName());
                                }
                                if(ev.getClass().equals(TrackName.class)){
                                    trackName = ((TrackName) ev).getTrackName();
                                    Button trackToggleButton = new Button(this);
                                    trackToggleButton.setText(trackName);
                                    trackToggleButton.setTag(trackName);
                                    trackToggleButton.setClickable(true);

                                    parent.addView(trackToggleButton);
                                }

                            }




                        }

                        int resolution = midi.getResolution();
                        t.setText(data.getData().getEncodedPath()+" lunghezza: "+midi.getLengthInTicks()+" resultion "+resolution);

                    } catch(IOException e) {
                        Log.e(TAG, e.getStackTrace().toString());

                    }


                    if(mp!=null){
                        mp.stop();
                        mp.release();
                        mp = null;
                    }
                    MidiManager mm = new MidiManager(this,midi);
                    mm.isolateTrack(1);
                    mm.isolateTrack(2);
                    mm.isolateTrack(3);
                    mm.reduceTempoInFileByFactor(2f);


                    mp = MediaPlayer.create(this,Uri.fromFile(mm.getModifiedFile()));
                }
                break;
        }
    }
}
