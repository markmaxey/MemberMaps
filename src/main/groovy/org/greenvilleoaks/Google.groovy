package org.greenvilleoaks

import com.google.maps.DistanceMatrixApi
import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import com.google.maps.model.DistanceMatrix
import com.google.maps.model.GeocodingResult
import com.google.maps.model.Unit

class Google {
    private final GeoApiContext context

    public Google(GeoApiContext context) {
        this.context = context
    }


    public GeocodingResult[] geocode(final String fullAddress) {
         return GeocodingApi.geocode(context, fullAddress).await()
    }


    public DistanceMatrix distanceMatrix(final String origin, final String destination) {
        return distanceMatrix([origin] as String[], [destination] as String[])
    }


    public DistanceMatrix distanceMatrix(final List<String> origins, final List<String> destinations) {
        return distanceMatrix((String[])origins.toArray(), (String[])destinations.toArray())
    }


    public DistanceMatrix distanceMatrix(final String[] origins, final String[] destinations) {
        return DistanceMatrixApi.getDistanceMatrix(context, origins, destinations).
                units(Unit.IMPERIAL).
                await()
    }
}
