package com.scepticallistic.kevin.tipcalc;

import java.util.Comparator;

public class PercentComparator implements Comparator<String> {

    @Override
    public int compare(String s1, String s2) {
        int p1 = Integer.parseInt(s1.substring(0, s1.length() - 1));
        int p2 = Integer.parseInt(s2.substring(0, s2.length() - 1));

        return p1 - p2;
    }

}
