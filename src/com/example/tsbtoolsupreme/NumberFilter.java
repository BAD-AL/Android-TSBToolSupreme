package com.example.tsbtoolsupreme;



import android.text.InputFilter;
import android.text.Spanned;

public class NumberFilter implements InputFilter {

    private int min, max;

    public NumberFilter(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public NumberFilter(String min, String max) {
        this.min = Integer.parseInt(min);
        this.max = Integer.parseInt(max);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {   
        try {
            int input = Integer.parseInt(source.toString());
            if (isInRange(min, max, input))
                return null;
        } catch (NumberFormatException nfe) { }     
        return "";
    }

    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}
