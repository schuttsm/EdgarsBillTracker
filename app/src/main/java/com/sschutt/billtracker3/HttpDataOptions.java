package com.sschutt.billtracker3;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import com.android.volley.Request;

/**
 * Created by Stephen on 5/18/2018.
 */

public class HttpDataOptions implements Serializable {
    public Map<String, String> params;
    public int method;
    public String url;
    public Boolean save_output;
    public Boolean refresh_bills;

    public HttpDataOptions(Map<String, String> params, int method, String url) {
        this(params, method, url, false, false);
    }

    public HttpDataOptions(Map<String, String> params, int method, String url, Boolean save_output, Boolean refresh_bills) {
        this.params = params;
        this.method = method;
        this.url = url;
        this.save_output = save_output;
        this.refresh_bills = refresh_bills;
    }
}
