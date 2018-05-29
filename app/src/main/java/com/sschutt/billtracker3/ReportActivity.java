package com.sschutt.billtracker3;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


import com.couchbase.lite.Array;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.Expression;
import com.couchbase.lite.Ordering;
import com.couchbase.lite.PropertyExpression;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ReportActivity extends BaseLoggedInActivity {
    private TextView status_bar;
    private final String TAG = "ReportActivity";
    private PieChart mChart;
    private TableLayout child_layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        status_bar = (TextView) findViewById(R.id.status_bar);
        ListView report_legend = (ListView)findViewById(R.id.report_legend);
        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();

        mChart = findViewById(R.id.chart1);
        PieDataSet dataSet = new PieDataSet(entries, "");
        PieData data = new PieData(dataSet);
        ArrayList<Integer> colors = new ArrayList<Integer>();
        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);
        mChart.setUsePercentValues(true);
        mChart.getDescription().setEnabled(false);
        mChart.setCenterTextRadiusPercent(5f);

        Legend l = mChart.getLegend();
        String selected_currency = "ALL";

        for (int c : ColorTemplate.MATERIAL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        dataSet.setColors(colors);

        try{
            Database db = ((BillTrackerApplication)this.getApplication()).getDatabase();
            Query query = QueryBuilder.select(SelectResult.property("amount"),
                    SelectResult.property("category"))
                    .from(DataSource.database(db))
                    .where(Expression.property("currency").equalTo(Expression.string(selected_currency)));
            ResultSet result = query.execute();
            List<Result> result_rows = result.allResults();
            Float total = 0f;
            HashMap<String, Float> amounts = new HashMap<String, Float>();
            for(int index = 0; index < result_rows.size(); index++) {
                Result obj = result_rows.get(index);
                Float amount = Float.parseFloat(obj.getString("amount"));
                total += amount;
                String category = obj.getString("category");
                Float curr_amount;
                if (amounts.containsKey(category))
                    curr_amount = amounts.get(category) + amount;
                else
                    curr_amount = amount;

                amounts.put(category, curr_amount);
            }
            data.setValueFormatter(new PercentFormatter());
            Iterator it = amounts.entrySet().iterator();
            ArrayList<String> str_amounts = new ArrayList<String>();
            while (it.hasNext()) {
                Map.Entry<String, Float> pair = (Map.Entry)it.next();
                Float category_amount = pair.getValue();
                Float category_percent = category_amount / total;
                entries.add(new PieEntry(category_percent, pair.getKey()));
                str_amounts.add(" - " + category_amount + " " + selected_currency);
            }
            data.setValueTextSize(11f);
            data.setValueTextColor(Color.BLACK);
            mChart.setDrawHoleEnabled(true);
            mChart.setHoleColor(Color.WHITE);

            mChart.setTransparentCircleColor(Color.WHITE);
            mChart.setTransparentCircleAlpha(110);
            mChart.setCenterTextColor(Color.BLACK);
            mChart.setData(data);

            report_legend.setAdapter(new ReportLegendListAdapter(dataSet, l, str_amounts, this));
            mChart.getLegend().setWordWrapEnabled(true);
            mChart.getLegend().setEnabled(false);
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, e.toString());
            status_bar.setText(e.toString());
        }
    }
}
