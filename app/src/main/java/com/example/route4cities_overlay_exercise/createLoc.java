package com.example.route4cities_overlay_exercise;

import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;
import java.util.Vector;

public class createLoc
 {
    public static int orientation(LatLng point1, LatLng point2, LatLng point3)
    {
        double calculatedExp = (point2.longitude - point1.longitude) * (point3.latitude - point2.latitude) -
                (point2.latitude - point1.latitude) * (point3.longitude - point2.longitude);
        if (calculatedExp == 0) return 0;
        return (calculatedExp > 0)? 1: 2;
    }
    public static Vector<LatLng> convexHull(LatLng markers[], int n)
    {
        Vector<LatLng> hull = new Vector<LatLng>();
        if (n < 3)
        {
            hull.addAll(Arrays.asList(markers));
            return hull;
        }
        int varLat = 0;
        for (int i = 1; i < n; i++)
            if (markers[i].latitude < markers[varLat].latitude)
                varLat = i;
        int value1 = varLat, value2;
        do
        {
            hull.add(markers[value1]);
            value2 = (value1 + 1) % n;

            for (int i = 0; i < n; i++)
            {
                if (orientation(markers[value1], markers[i], markers[value2])
                        == 2)
                    value2 = i;
            }
            value1 = value2;
        }
        while (value1 != varLat);
        return hull;
    }
}
