package org.greenvilleoaks

import com.google.maps.DistanceMatrixApi
import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import com.google.maps.model.DistanceMatrix
import com.google.maps.model.GeocodingResult
import com.google.maps.model.Unit
import groovy.transform.Immutable

@Immutable
class Geodedic {
    private String centralAddress
    private GeoApiContext context
    private List<Member> geodedicMembers


    /**
     * Add geodedic information (latitude, longitude, & Distance from a central point) to the create list.
     * Any geodedic information will be persisted to a file to optimize subsequent uses.
     *
     * @param members The list of members
     */
    public void create(final List<Member> members) {
        // Geocode any addresses that were missing from the Geodedic CSV file
        members.each { Member member ->
            if (!findMemberGeodedic(member, geodedicMembers)) {
                geocode(member)
                addDistance(member)

                geodedicMembers <<  member
            }
        }
    }



    private void geocode(Member member) {
        GeocodingResult[] results = GeocodingApi.geocode(context, member.fullAddress).await()

        if (!results || (results.size() == 0)) {
            throw new RuntimeException("No address was found for '$member.fullAddress'")
        }
        else if (results && results.size() > 1) {
            throw new RuntimeException("${results.size()} addresses were found for '$member.fullAddress'")
        }

        member.latitude         = results[0].geometry.location.lat
        member.longitude        = results[0].geometry.location.lng
        member.formattedAddress = results[0].formattedAddress
    }


    private void addDistance(Member member) {
        DistanceMatrix distanceMatrix = DistanceMatrixApi.getDistanceMatrix(
                context, [member.fullAddress] as String[], [centralAddress] as String[]).
                units(Unit.IMPERIAL).
                await()

        if (!distanceMatrix || !distanceMatrix.rows ||
                (distanceMatrix.rows.size() == 0) ||
                (distanceMatrix.rows[0].elements.size() == 0)) {
            throw new RuntimeException("Can't find distance from '$member.fullAddress' to '$centralAddress'")

        }
        else if (distanceMatrix && distanceMatrix.rows && distanceMatrix.rows.size() > 1) {
            throw new RuntimeException("${distanceMatrix.rows.size()} distance matrix rows were found from '$member.fullAddress' to '$centralAddress'")
        }
        else if (distanceMatrix && distanceMatrix.rows && distanceMatrix.rows[0].elements.size() > 1) {
            throw new RuntimeException("${distanceMatrix.rows[0].elements.size()} distance matrix elements were found from '$member.fullAddress' to '$centralAddress'")
        }

        member.commuteDistance2CentralPointInMeters      = distanceMatrix.rows[0].elements[0].distance.inMeters
        member.commuteDistance2CentralPointHumanReadable = distanceMatrix.rows[0].elements[0].distance.humanReadable

        member.commuteTime2CentralPointInSeconds     = distanceMatrix.rows[0].elements[0].duration.inSeconds
        member.commute = distanceMatrix.rows[0].elements[0].duration.humanReadable
    }


    private static Member findMemberGeodedic(
            final Member member,
            final List<Member> members) {
        return members.find {
            member.address.equals(it.address) &&
            member.city.equals(it.city) &&
            member.zip.equals(it.zip)
        }
    }
}