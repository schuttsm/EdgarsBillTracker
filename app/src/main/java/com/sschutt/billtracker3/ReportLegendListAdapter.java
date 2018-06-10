package com.sschutt.billtracker3;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
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
    private Float total;
    ReportLegendListAdapter(PieDataSet dataSet, Legend legend, Float total, Context context) {
        this.dataSet = dataSet;
        this.legend = legend;
        this.context = context;
        this.total = total;
    }

    @Override
    public int getCount() {
        if (dataSet == null)
            return 0;
        else
            return dataSet.getEntryCount() + 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public PieEntryBill getItem(int position) {
        PieEntryBill row;
        if (position == dataSet.getEntryCount()) {
            row = new PieEntryBill(100, "Total", this.total, "ALL");
            // row.Color = Color.WHITE;
        }
        else {
            row = ((PieEntryBill) dataSet.getEntryForIndex(position));
            int num_colors = this.legend.getColors().length;
            row.Color = this.legend.getColors()[position % (num_colors - 1)];
        }
        return row;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final PieEntryBill row = this.getItem(position);
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.legend_row, null);
        }

        LinearLayout tv_color = (LinearLayout)convertView.findViewById(R.id.tv_color);
        TextView tv_label = (TextView)convertView.findViewById(R.id.tv_label);
        TextView tv_amt = (TextView)convertView.findViewById(R.id.tv_amt);
        tv_color.setBackgroundColor(row.Color);
        tv_label.setText(row.Category);
        tv_amt.setText(row.CategoryAmount + " " + row.Currency);
        // tv_label.setText(row.Category + this.amounts.get(position));

        return convertView;
    }
}
