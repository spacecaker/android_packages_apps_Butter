/**
 * 
 */

package com.spacecaker.butter.tasks;

import java.lang.ref.WeakReference;
import java.util.Iterator;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.spacecaker.butter.lastfm.api.Artist;
import com.spacecaker.butter.lastfm.api.Image;
import com.spacecaker.butter.lastfm.api.ImageSize;
import com.spacecaker.butter.lastfm.api.PaginatedResult;
import com.spacecaker.butter.utils.ApolloUtils;
import com.androidquery.AQuery;

import static com.spacecaker.butter.Constants.ARTIST_IMAGE_ORIGINAL;
import static com.spacecaker.butter.Constants.LASTFM_API_KEY;

/**
 * @author Andrew Neal
 * @Note This is used to display artist images in @TracksBrowser
 */
public class LastfmGetArtistImagesOriginal extends AsyncTask<String, Integer, String> {

    // URL to cache
    private String url = null;

    private final ImageView mImageView;

    private final WeakReference<ImageView> imageviewReference;

    // AQuery
    private final AQuery aq;

    // Context
    private final Context mContext;

    private final WeakReference<Context> contextReference;

    public LastfmGetArtistImagesOriginal(Context context, ImageView iv) {
        contextReference = new WeakReference<Context>(context);
        mContext = contextReference.get();
        imageviewReference = new WeakReference<ImageView>(iv);
        mImageView = imageviewReference.get();

        // Initiate AQuery
        aq = new AQuery(mContext);
    }

    @Override
    protected String doInBackground(String... artistname) {
        if (ApolloUtils.isOnline(mContext)) {
            PaginatedResult<Image> artist = Artist.getImages(artistname[0], 1, 1, LASTFM_API_KEY);
            Iterator<Image> iterator = artist.getPageResults().iterator();
            while (iterator.hasNext()) {
                Image mTemp = iterator.next();
                url = mTemp.getImageURL(ImageSize.ORIGINAL);
            }
            aq.cache(url, 0);
            ApolloUtils.setImageURL(artistname[0], url, ARTIST_IMAGE_ORIGINAL, mContext);
            return url;
        } else {
            url = ApolloUtils.getImageURL(artistname[0], ARTIST_IMAGE_ORIGINAL, mContext);
        }
        return url;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null && mImageView != null) {
            new BitmapFromURL(mImageView).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, result);
        }
        super.onPostExecute(result);
    }
}
