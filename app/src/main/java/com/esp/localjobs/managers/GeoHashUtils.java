package com.esp.localjobs.managers;


import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for encoding and decoding geohashes. Based on
 * <a href="http://en.wikipedia.org/wiki/Geohash">http://en.wikipedia.org/wiki/Geohash.
 */
public class GeoHashUtils {

    private static final char[] BASE_32 = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n',
            'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    private final static Map<Character,Integer> DECODE_MAP = new HashMap();

    private static final int PRECISION = 12;
    private static final int[] BITS = {16, 8, 4, 2, 1};

    static {
        for (int i = 0; i < BASE_32.length; i++) {
            DECODE_MAP.put(Character.valueOf(BASE_32[i]), Integer.valueOf(i));
        }
    }

    private GeoHashUtils() {
    }

    /**
     * Encodes the given latitude and longitude into a geohash
     *
     * @param latitude Latitude to encode
     * @param longitude Longitude to encode
     * @return Geohash encoding of the longitude and latitude
     */
    public static String encode(double latitude, double longitude) {
        double[] latInterval = {-90.0, 90.0};
        double[] lngInterval = {-180.0, 180.0};

        final StringBuilder geohash = new StringBuilder();
        boolean isEven = true;

        int bit = 0;
        int ch = 0;

        while (geohash.length() < PRECISION) {
            double mid = 0.0;
            if (isEven) {
                mid = (lngInterval[0] + lngInterval[1]) / 2D;
                if (longitude > mid) {
                    ch |= BITS[bit];
                    lngInterval[0] = mid;
                } else {
                    lngInterval[1] = mid;
                }
            } else {
                mid = (latInterval[0] + latInterval[1]) / 2D;
                if (latitude > mid) {
                    ch |= BITS[bit];
                    latInterval[0] = mid;
                } else {
                    latInterval[1] = mid;
                }
            }

            isEven = !isEven;

            if (bit < 4) {
                bit++;
            } else {
                geohash.append(BASE_32[ch]);
                bit = 0;
                ch = 0;
            }
        }

        return geohash.toString();
    }

    /**
     * Decodes the given geohash into a latitude and longitude
     *
     * @param geohash Geohash to deocde
     * @return Array with the latitude at index 0, and longitude at index 1
     */
    public static double[] decode(String geohash) {
        final double[] latInterval = {-90.0, 90.0};
        final double[] lngInterval = {-180.0, 180.0};

        boolean isEven = true;

        double latitude;
        double longitude;
        for (int i = 0; i < geohash.length(); i++) {
            final int cd = DECODE_MAP.get(Character.valueOf(
                    geohash.charAt(i))).intValue();

            for (int mask : BITS) {
                if (isEven) {
                    if ((cd & mask) != 0) {
                        lngInterval[0] = (lngInterval[0] + lngInterval[1]) / 2D;
                    } else {
                        lngInterval[1] = (lngInterval[0] + lngInterval[1]) / 2D;
                    }
                } else {
                    if ((cd & mask) != 0) {
                        latInterval[0] = (latInterval[0] + latInterval[1]) / 2D;
                    } else {
                        latInterval[1] = (latInterval[0] + latInterval[1]) / 2D;
                    }
                }
                isEven = !isEven;
            }

        }
        latitude = (latInterval[0] + latInterval[1]) / 2D;
        longitude = (lngInterval[0] + lngInterval[1]) / 2D;

        return new double[] {latitude, longitude};
    }
}