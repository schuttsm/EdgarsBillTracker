package com.sschutt.billtracker3;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.birbit.android.jobqueue.JobManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class BillReportListAdapter extends BaseAdapter {
    Context context;
    List<BillReportRow> rows;
    private final String TAG = "BillReportListAdapter";
    private JobManager jobManager;
    private String token;
    public BillReportListAdapter(List<BillReportRow> rows, JobManager jobManager, Context context) {
        this.rows = rows;
        this.context = context;
        this.jobManager = jobManager;
        this.token = token;
    }

    @Override
    public int getCount() {
        if (rows == null)
            return 0;
        else
            return rows.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public BillReportRow getItem(int position) {
        return rows.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final BillReportRow row = this.getItem(position);
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.billreportlist_layout, null);
        }

        final TextView row_amount_currency = (TextView)convertView.findViewById(R.id.row_amount_currency);
        final TextView row_category = (TextView)convertView.findViewById(R.id.row_category);
        final TextView row_date = (TextView)convertView.findViewById(R.id.row_date);
        final ImageButton btn_delete = (ImageButton)convertView.findViewById(R.id.btn_delete);

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HttpDataOptions options = new HttpDataOptions(new HashMap<String, String>(), Request.Method.DELETE, "bill/" + row.id, false, true);
                jobManager.addJobInBackground(new HttpDataJob(options));
            }
        });

        row_category.setText(row.category);
        row_date.setText(row.getFormattedDate());
        row_amount_currency.setText(row.amount + " " + row.currency);

        return convertView;
    }
}
