package com.microntek.android.gps.util;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;

public class RetryPolicyCustomize extends DefaultRetryPolicy {

    protected long mInterval = 3000;

    // デフォルトタイムアウト時間
    private static final int CONNECTION_TIMEOUT = 20000;
    private static final int CONNECTION_RETRY_COUNT = 3;

    public RetryPolicyCustomize(int initialTimeoutMs, int maxNumRetries, float backoffMultiplier) {
        super(initialTimeoutMs, maxNumRetries, backoffMultiplier);
    }

    public RetryPolicyCustomize() {
        super(CONNECTION_TIMEOUT, CONNECTION_RETRY_COUNT, 1f);
    }

    public RetryPolicyCustomize(long interval) {
        super(CONNECTION_TIMEOUT, CONNECTION_RETRY_COUNT, 1f);
        mInterval = interval;
    }

    @Override
    public void retry(VolleyError error) throws VolleyError {
        NetworkResponse response = error.networkResponse;
        if (response != null && response.statusCode >= 500 && response.statusCode < 600) {
            // サーバーエラー時はリトライしない
            throw error;
        }
        if (mInterval > 0) {
            try {
                Thread.sleep(mInterval);
            } catch (InterruptedException e) {
            }
        }
        VolleyLog.d("Network Retry count : %d", getCurrentRetryCount());
        super.retry(error);
    }
}