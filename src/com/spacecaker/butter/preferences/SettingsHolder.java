/**
 * 
 */

package com.spacecaker.butter.preferences;

import static com.spacecaker.butter.Constants.ALBUM_IMAGE;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.spacecaker.butter.IApolloService;
import com.spacecaker.butter.R;
import com.spacecaker.butter.activities.AudioPlayerHolder;
import com.spacecaker.butter.service.ApolloService;
import com.spacecaker.butter.service.ServiceToken;
import com.spacecaker.butter.utils.ApolloUtils;
import com.spacecaker.butter.utils.MusicUtils;
/**
 * @author Andrew Neal FIXME - Work on the IllegalStateException thrown when
 *         using PreferenceFragment and theme chooser
 */
@SuppressWarnings("deprecation")
public class SettingsHolder extends PreferenceActivity implements ServiceConnection {

    // Service
    private ServiceToken mToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // This should be called first thing
        super.onCreate(savedInstanceState);

        // Load settings XML
        int preferencesResId = R.xml.settings;
        addPreferencesFromResource(preferencesResId);
        // ActionBar
        initActionBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder obj) {
        MusicUtils.mService = IApolloService.Stub.asInterface(obj);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        MusicUtils.mService = null;
    }

    /**
     * Update the ActionBar as needed
     */
    private final BroadcastReceiver mMediaStatusReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Update the ActionBar
            initActionBar();
        }

    };

    @Override
    protected void onStart() {
        // Bind to Service
        mToken = MusicUtils.bindToService(this, this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ApolloService.META_CHANGED);
        filter.addAction(ApolloService.QUEUE_CHANGED);
        filter.addAction(ApolloService.PLAYSTATE_CHANGED);

        registerReceiver(mMediaStatusReceiver, filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        // Unbind
        if (MusicUtils.mService != null)
            MusicUtils.unbindFromService(mToken);

        unregisterReceiver(mMediaStatusReceiver);
        super.onStop();
    }

    /**
     * Update the ActionBar
     */
    public void initActionBar() {
        // Custom ActionBar layout
        View view = getLayoutInflater().inflate(R.layout.custom_action_bar, null);
        // Show the ActionBar
        getActionBar().setCustomView(view);
        getActionBar().setTitle(R.string.settings);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayShowCustomEnabled(true);

        ImageView mAlbumArt = (ImageView)view.findViewById(R.id.action_bar_album_art);
        TextView mTrackName = (TextView)view.findViewById(R.id.action_bar_track_name);
        TextView mAlbumName = (TextView)view.findViewById(R.id.action_bar_album_name);

        String url = ApolloUtils.getImageURL(MusicUtils.getAlbumName(), ALBUM_IMAGE, this);
        AQuery aq = new AQuery(this);
        mAlbumArt.setImageBitmap(aq.getCachedImage(url));

        mTrackName.setText(MusicUtils.getTrackName());
        mAlbumName.setText(MusicUtils.getAlbumName());

        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                context.startActivity(new Intent(context, AudioPlayerHolder.class));
                finish();
            }
        });
    }
}
