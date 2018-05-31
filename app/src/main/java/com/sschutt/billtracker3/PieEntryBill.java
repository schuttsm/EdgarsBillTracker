package com.sschutt.billtracker3;

import com.github.mikephil.charting.data.PieEntry;

public class PieEntryBill extends PieEntry {
    public String Currency;
    public float CategoryAmount;
    public float CategoryPercent;
    public String Category;
    public int Color;
    public PieEntryBill(float CategoryPercent, String Category, float CategoryAmount, String Currency) {
        super(CategoryPercent, Category);
        this.CategoryPercent = CategoryPercent;
        this.Category = Category;
        this.Currency = Currency;
        this.CategoryAmount = CategoryAmount;
    }
}
