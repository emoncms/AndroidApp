package org.emoncms.myapps;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class HTTPClient
{
    private static HTTPClient mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    private HTTPClient(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized HTTPClient getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new HTTPClient(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
            mRequestQueue.getCache().clear();
        }
        return mRequestQueue;
    }

    public void cancellAll(String TAG) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(TAG);
        }
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setShouldCache(false);
        req.setRetryPolicy(new DefaultRetryPolicy(2500, 0, 1f));
        getRequestQueue().add(req);
    }
}