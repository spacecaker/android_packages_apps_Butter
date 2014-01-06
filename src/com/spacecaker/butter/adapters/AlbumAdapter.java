
package com.spacecaker.butter.adapters;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.spacecaker.butter.R;
import com.spacecaker.butter.grid.fragments.AlbumsFragment;
import com.spacecaker.butter.tasks.LastfmGetAlbumImages;
import com.spacecaker.butter.tasks.ViewHolderTask;
import com.spacecaker.butter.utils.ApolloUtils;
import com.spacecaker.butter.utils.MusicUtils;
import com.spacecaker.butter.views.ViewHolderGrid;
import com.androidquery.AQuery;

import static com.spacecaker.butter.Constants.ALBUM_IMAGE;

/**
 * @author Andrew Neal
 */
public class AlbumAdapter extends SimpleCursorAdapter {

    private AnimationDrawable mPeakOneAnimation, mPeakTwoAnimation;

    private WeakReference<ViewHolderGrid> holderReference;

    public AlbumAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final View view = super.getView(position, convertView, parent);
        // ViewHolderGrid
        final ViewHolderGrid viewholder;

        if (view != null) {

            viewholder = new ViewHolderGrid(view);
            holderReference = new WeakReference<ViewHolderGrid>(viewholder);
            view.setTag(holderReference.get());

        } else {
            viewholder = (ViewHolderGrid)convertView.getTag();
        }

        // AQuery
        final AQuery aq = new AQuery(view);

        // Album name
        String albumName = mCursor.getString(AlbumsFragment.mAlbumNameIndex);
        holderReference.get().mViewHolderLineOne.setText(albumName);

        // Artist name
        String artistName = mCursor.getString(AlbumsFragment.mArtistNameIndex);
        holderReference.get().mViewHolderLineTwo.setText(artistName);

        // Match positions
        holderReference.get().position = position;
        if (aq.shouldDelay(position, view, parent, "")) {
            holderReference.get().mViewHolderImage.setImageDrawable(null);
        } else {
            // Check for missing album images and cache them
            if (ApolloUtils.getImageURL(albumName, ALBUM_IMAGE, mContext) == null) {
                new LastfmGetAlbumImages(mContext, null, 0).executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR, artistName, albumName);
            } else {
                new ViewHolderTask(null, holderReference.get(), position, mContext, 1, 1,
                        holderReference.get().mViewHolderImage).executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR, albumName);
            }
        }
        // Now playing indicator
        long currentalbumid = MusicUtils.getCurrentAlbumId();
        long albumid = mCursor.getLong(AlbumsFragment.mAlbumIdIndex);
        if (currentalbumid == albumid) {
            holderReference.get().mPeakOne.setImageResource(R.anim.peak_meter_1);
            holderReference.get().mPeakTwo.setImageResource(R.anim.peak_meter_2);
            mPeakOneAnimation = (AnimationDrawable)holderReference.get().mPeakOne.getDrawable();
            mPeakTwoAnimation = (AnimationDrawable)holderReference.get().mPeakTwo.getDrawable();
            try {
                if (MusicUtils.mService.isPlaying()) {
                    mPeakOneAnimation.start();
                    mPeakTwoAnimation.start();
                } else {
                    mPeakOneAnimation.stop();
                    mPeakTwoAnimation.stop();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            holderReference.get().mPeakOne.setImageResource(0);
            holderReference.get().mPeakTwo.setImageResource(0);
        }
        return view;
    }
}
