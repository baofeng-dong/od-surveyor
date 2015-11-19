package com.meyersj.mobilesurveyor.app.util;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;


public class DataLoader {


    public static final String TAG = "DataLoader";

    private static List<String[]> openCSV(Context context) {
        List<String[]> rows = null;
        try {
            InputStreamReader csv = new InputStreamReader(context.getAssets().open(Cons.ROUTE_DIRECTIONS_CSV));
            CSVReader reader = new CSVReader(csv);
            rows = reader.readAll();
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }
        if(rows != null) return rows.subList(1, rows.size());
        return rows;
    }

    public static ArrayList<String> getRoutes(Context context) {
        List<String[]> rows = DataLoader.openCSV(context);
        ArrayList<String> routes = new ArrayList<String>();
        if(rows != null) {
            for(String[] row: rows) {
                if(row[2].equals("0")) routes.add(row[1]);
            }
        }
        return routes;
    }

    public static HashMap<String, String> getRoutesLookup(Context context) {
        List<String[]> rows = DataLoader.openCSV(context);
        HashMap<String, String> lookup = new HashMap<String, String>();
        if(rows != null) {
            for(String[] row: rows) {
                if(row[2].equals("0")) {
                    lookup.put(row[1], row[0]);
                    lookup.put(row[0], row[1]);
                }
            }
        }
        return lookup;
    }

    public static HashMap<String, String[]> getDirLookup(Context context) {
        List<String[]> rows = DataLoader.openCSV(context);
        HashMap<String, String[]> lookup = new HashMap<String, String[]>();
        if(rows != null) {
            for(String[] row: rows) {
                if(!lookup.containsKey(row[0])) {
                    String[] directions = new String[2];
                    if (Utils.hasSingleDirection(row[0])) {
                        directions = new String[1];
                    }
                    lookup.put(row[0], directions);
                }
                lookup.get(row[0])[Integer.valueOf(row[2])] = row[3];
            }
        }
        return lookup;
    }


}
