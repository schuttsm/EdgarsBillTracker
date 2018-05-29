package com.sschutt.billtracker3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Request;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieDataSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReportLegendListAdapter extends BaseAdapter {
    private PieDataSet dataSet;
    private Context context;
    private Legend legend;
    private ArrayList<String> amounts;
    ReportLegendListAdapter(PieDataSet dataSet, Legend legend, ArrayList<String> amounts, Context context) {
        this.dataSet = dataSet;
        this.legend = legend;
        this.amounts = amounts;
        this.context = context;
    }

    @Override
    public int getCount() {
        if (dataSet == null)
            return 0;
        else
            return dataSet.getEntryCount();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public ReportLegendRow getItem(int position) {
        ReportLegendRow row = new ReportLegendRow();
        row.Color = this.legend.getColors()[position];
        row.Category = dataSet.getEntryForIndex(position).getLabel();
        return row;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ReportLegendRow row = this.getItem(position);
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.legend_row, null);
        }


        LinearLayout tv_color = (LinearLayout)convertView.findViewById(R.id.tv_color);
        TextView tv_label = (TextView)convertView.findViewById(R.id.tv_label);
        tv_color.setBackgroundColor(row.Color);
        tv_label.setText(row.Category + this.amounts.get(position));

        return convertView;
    }
}
