package org.greenvilleoaks

import com.google.maps.DistanceMatrixApi
import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import com.google.maps.model.DistanceMatrix
import com.google.maps.model.GeocodingResult
import com.google.maps.model.Unit

final class Google {
    private GeoApiContext context

    public Google(GeoApiContext context) {
        this.context = context
    }


    public GeocodingResult[] geocode(final String fullAddress) {
         return GeocodingApi.geocode(context, fullAddress).await()
    }

    public DistanceMatrix distanceMatrix(final String fullAddress, final String centralAddress) {
        return DistanceMatrixApi.getDistanceMatrix(
                context, [fullAddress] as String[], [centralAddress] as String[]).
                units(Unit.IMPERIAL).
                await()
    }
}
