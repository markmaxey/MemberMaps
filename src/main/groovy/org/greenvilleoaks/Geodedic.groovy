package org.greenvilleoaks

import com.google.maps.model.DistanceMatrix
import com.google.maps.model.GeocodingResult
import groovy.transform.Immutable

final class Geodedic {
    private final String centralAddress
    private final List<Member> geodedicMembers
    private final Google google

    public Geodedic(final String centralAddress, List<Member> geodedicMembers, Google google) {
        this.centralAddress  = centralAddress
        this.geodedicMembers = geodedicMembers
        this.google          = google
    }


    /**
     * Add geodedic information (latitude, longitude, & Distance from a central point) to the create list.
     * Any geodedic information will be persisted to a file to optimize subsequent uses.
     *
     * @param members The list of members
     */
    public void create(final List<Member> members) {
        // Geocode any addresses that were missing from the Geodedic CSV file
        members.each { Member member ->
            Member geodedicInfo = findMemberGeodedic(member, geodedicMembers)
            if (!geodedicInfo) {
                geocode(member)
                addDistance(member)

                geodedicMembers <<  member
            }
            else {
                // Having loaded the persistent information from a file instead of asking Google for it,
                // save this information in the member
                member.latitude         = geodedicInfo.latitude
                member.longitude        = geodedicInfo.longitude
                member.formattedAddress = geodedicInfo.formattedAddress

                member.commuteDistance2CentralPointInMeters      = geodedicInfo.commuteDistance2CentralPointInMeters
                member.commuteDistance2CentralPointHumanReadable = geodedicInfo.commuteDistance2CentralPointHumanReadable
                member.commuteTime2CentralPointInSeconds         = geodedicInfo.commuteTime2CentralPointInSeconds
                member.commuteTime2CentralPointHumanReadable     = geodedicInfo.commuteTime2CentralPointHumanReadable
            }
        }
    }



    private void geocode(Member member) {
        GeocodingResult[] results = google.geocode(member.fullAddress)

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
        DistanceMatrix distanceMatrix = google.distanceMatrix(member.fullAddress, centralAddress)

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

        member.commuteTime2CentralPointInSeconds         = distanceMatrix.rows[0].elements[0].duration.inSeconds
        member.commuteTime2CentralPointHumanReadable     = distanceMatrix.rows[0].elements[0].duration.humanReadable
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