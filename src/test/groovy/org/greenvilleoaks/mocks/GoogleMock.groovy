package org.greenvilleoaks.mocks

import com.google.maps.model.*
import org.greenvilleoaks.Google


/** An enumerated set of faults that clients can inject into GoogleMock */
enum GoogleFaultCode {
    none,
    no_geocode_results,
    multiple_geocode_results,
    null_distance_matrix,
    null_distance_matrix_rows,
    no_distance_matrix_rows,
    null_distance_matrix_row_elements,
    no_distance_matrix_row_elements,
    multiple_distance_matrix_rows,
    multiple_distance_matrix_row_elements,
    distance_matrix_status_not_ok
}


/**
 * A mock of the Google geocode & distance matrix services.
 */
class GoogleMock extends Google {
    /** A set of addresses that were geocoded */
    private final Set<String> addressesGeocoded
    
    /** The member whose identity is the index and whose value is a fault code */
    private final List<GoogleFaultCode> memberNum2FaultCode

    
    public GoogleMock(
            final List<GoogleFaultCode> memberNum2FaultCode,
            final Set<String> addressesGeocoded) {
        super(null) // We don't use the GeoApiContext

        // The number in the memberNum2FaultCode list has to be one more than the number being processed.
        this.memberNum2FaultCode = memberNum2FaultCode
        this.addressesGeocoded   = addressesGeocoded
    }


    public GeocodingResult[] geocode(final String fullAddress) {
        if (memberNum2FaultCode.get(Integer.valueOf(fullAddress)) == GoogleFaultCode.no_geocode_results) {
            return null
        }
        else if (memberNum2FaultCode.get(Integer.valueOf(fullAddress)) == GoogleFaultCode.multiple_geocode_results) {
            return [new GeocodingResult(), new GeocodingResult()]
        }
        else {
            addressesGeocoded.add(fullAddress)
            GeocodingResult[] geocodingResults = [new GeocodingResult()]
            geocodingResults[0].geometry           = new Geometry()
            geocodingResults[0].geometry.location  =
                    new LatLng(Double.valueOf(fullAddress), Double.valueOf(fullAddress))
            return geocodingResults
        }
    }


    public DistanceMatrix distanceMatrix(final String[] origins, final String[] destinations) {
        if (memberNum2FaultCode.get(Integer.valueOf(origins[0])) == GoogleFaultCode.null_distance_matrix) {
            return null
        }
        else if (memberNum2FaultCode.get(Integer.valueOf(origins[0])) == GoogleFaultCode.null_distance_matrix_rows) {
            return new DistanceMatrix(origins, destinations, null)
        }
        else if (memberNum2FaultCode.get(Integer.valueOf(origins[0])) == GoogleFaultCode.no_distance_matrix_rows) {
            return new DistanceMatrix(origins, destinations, new DistanceMatrixRow[0])
        }
        else if (memberNum2FaultCode.get(Integer.valueOf(origins[0])) == GoogleFaultCode.null_distance_matrix_row_elements) {
            return new DistanceMatrix(origins, destinations, new DistanceMatrixRow[1])
        }
        else if (memberNum2FaultCode.get(Integer.valueOf(origins[0])) == GoogleFaultCode.no_distance_matrix_row_elements) {
            DistanceMatrixRow[] rows = new DistanceMatrixRow[1]
            rows[0] = new DistanceMatrixRow()
            rows[0].elements = new DistanceMatrixElement[0]
            return new DistanceMatrix(origins, destinations, rows)
        }
        else if (memberNum2FaultCode.get(Integer.valueOf(origins[0])) == GoogleFaultCode.multiple_distance_matrix_rows) {
            DistanceMatrixRow[] rows = new DistanceMatrixRow[2]
            rows[0] = new DistanceMatrixRow()
            rows[0].elements = new DistanceMatrixElement[0]
            rows[1] = new DistanceMatrixRow()
            rows[1].elements = new DistanceMatrixElement[0]
            return new DistanceMatrix(origins, destinations, rows)
        }
        else if (memberNum2FaultCode.get(Integer.valueOf(origins[0])) == GoogleFaultCode.multiple_distance_matrix_row_elements) {
            DistanceMatrixRow[] rows = new DistanceMatrixRow[1]
            rows[0] = new DistanceMatrixRow()
            rows[0].elements = new DistanceMatrixElement[2]
            return new DistanceMatrix(origins, destinations, rows)
        }
        else if (memberNum2FaultCode.get(Integer.valueOf(origins[0])) == GoogleFaultCode.distance_matrix_status_not_ok) {
            DistanceMatrixRow[] rows = new DistanceMatrixRow[1]
            rows[0] = new DistanceMatrixRow()
            rows[0].elements = new DistanceMatrixElement[1]
            rows[0].elements[0] = new DistanceMatrixElement()
            rows[0].elements[0].status = DistanceMatrixElementStatus.ZERO_RESULTS
            return new DistanceMatrix(origins, destinations, rows)
        }
        else {
            DistanceMatrixRow[] rows = new DistanceMatrixRow[origins.length]
            for(int rowNdx=0; rowNdx < origins.length; rowNdx++) {
                rows[rowNdx] = new DistanceMatrixRow();
                rows[rowNdx].elements = new DistanceMatrixElement[destinations.length];
                for(int elementNdx=0; elementNdx<destinations.length; elementNdx++) {
                    int d = Math.abs(Integer.valueOf(origins[rowNdx]) - Integer.valueOf(destinations[elementNdx]))
                    rows[rowNdx].elements[elementNdx] = new DistanceMatrixElement()
                    rows[rowNdx].elements[elementNdx].distance = new Distance()
                    rows[rowNdx].elements[elementNdx].distance.inMeters = d
                    rows[rowNdx].elements[elementNdx].distance.humanReadable =
                            Long.toString(rows[rowNdx].elements[elementNdx].distance.inMeters)
                    rows[rowNdx].elements[elementNdx].duration = new Duration()
                    rows[rowNdx].elements[elementNdx].duration.inSeconds = d
                    rows[rowNdx].elements[elementNdx].duration.humanReadable =
                            Long.toString(rows[rowNdx].elements[elementNdx].duration.inSeconds)
                    rows[rowNdx].elements[elementNdx].status = DistanceMatrixElementStatus.OK
                }
            }
            return new DistanceMatrix(origins, destinations, rows)
        }
    }
}
