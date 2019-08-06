package de.wpsverlinden.c4cduplicateanalyzer;

import de.wpsverlinden.c4cduplicateanalyzer.model.Account;
import org.springframework.stereotype.Component;

@Component
public class LevenshteinCalculator {

    public float getSimilarity(Account a, Account b, float threshold) {
        String aData = a.getSerialData();
        String bData = b.getSerialData();
        int length_difference = Math.abs(aData.length() - bData.length());
        int max_length = (Math.max(aData.length(), bData.length()));
        // DROP IF: distance > -threshold*max_length + max_length
        // AND distance > length_difference
        // SO --> DROP IF: length_difference > -threshold*max_length + max_length
        if (length_difference > -threshold * max_length + max_length) {
            return Float.MIN_VALUE;
        }

        int distance = getLevenshteinDistance(aData, bData);
        float ratio = ((float) distance) / max_length;
        return 1 - ratio;
    }

    // Implementation from https://web.archive.org/web/20120526085419/http://www.merriampark.com/ldjava.htm
    private int getLevenshteinDistance(String s, String t) {

        int n = s.length(); // length of s
        int m = t.length(); // length of t

        if (n == 0) {
            return m;
        } else if (m == 0) {
            return n;
        }

        int p[] = new int[n + 1]; //'previous' cost array, horizontally
        int d[] = new int[n + 1]; // cost array, horizontally
        int _d[]; //placeholder to assist in swapping p and d

        // indexes into strings s and t
        int i; // iterates through s
        int j; // iterates through t

        char t_j; // jth character of t

        int cost; // cost

        for (i = 0; i <= n; i++) {
            p[i] = i;
        }

        for (j = 1; j <= m; j++) {
            t_j = t.charAt(j - 1);
            d[0] = j;

            for (i = 1; i <= n; i++) {
                cost = s.charAt(i - 1) == t_j ? 0 : 1;
                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost				
                d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
            }

            // copy current distance counts to 'previous row' distance counts
            _d = p;
            p = d;
            d = _d;
        }

        // our last action in the above loop was to switch d and p, so p now 
        // actually has the most recent cost counts
        return p[n];
    }
}