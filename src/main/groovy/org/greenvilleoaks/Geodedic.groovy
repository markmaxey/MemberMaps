package org.greenvilleoaks

import com.google.maps.model.DistanceMatrix
import com.google.maps.model.DistanceMatrixElement
import com.google.maps.model.DistanceMatrixElementStatus
import com.google.maps.model.GeocodingResult
import groovy.util.logging.Log4j
import org.greenvilleoaks.view.View

@Log4j
final class Geodedic {
    private final String centralAddress
    private final List<Member> geodedicMembers
    private final Google google
    private final View roleView

    public Geodedic(
            final String centralAddress,
            final List<Member> geodedicMembers,
            final Google google,
            final View roleView) {
        this.centralAddress  = centralAddress
        this.geodedicMembers = geodedicMembers
        this.google          = google
        this.roleView        = roleView
    }


    /**
     * Add geodedic information (latitude, longitude, & Distance from a central point) to the create list.
     * Any geodedic information will be persisted to a file to optimize subsequent uses.
     *
     * @param members The list of members
     */
    public void create(final List<Member> members) {
        log.info("Geocoding members addresses ...")

        // Geocode any addresses that were missing from the Geodedic CSV file
        members.each { Member member ->
            Member geodedicInfo = findGeodedicInfo4Address(member, geodedicMembers)
            if (!geodedicInfo) {
                // Obtain the geodedic data about the member
                geocode(member)
                addDistance(member, centralAddress)

                // Add the address' geodedic information so that subsequent
                // members at the same address or subsequent runs of the
                // program won't have to use the Google APIs that are both
                // slow and have limits on their usage.
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

                // TODO: Add memberRoleCommute
            }
        }

        log.info("Finished geocoding members addresses")
    }



    private void geocode(Member member) {
        GeocodingResult[] results = google.geocode(member.fullAddress)

        if (!results || (results.size() == 0)) {
            log.error("No address was found for '$member.fullAddress'")
        }
        else if (results && results.size() > 1) {
            log.error("${results.size()} addresses were found for '$member.fullAddress'")
        }
        else {
            member.latitude         = results[0].geometry.location.lat
            member.longitude        = results[0].geometry.location.lng
            member.formattedAddress = results[0].formattedAddress
        }
    }


    private void addDistance(Member member, String destinationAddress) {
        DistanceMatrixElement distanceMatrixElement = findDistance(member, destinationAddress)

        if (distanceMatrixElement) {
            member.commuteDistance2CentralPointInMeters      = distanceMatrixElement.distance.inMeters
            member.commuteDistance2CentralPointHumanReadable = distanceMatrixElement.distance.humanReadable

            member.commuteTime2CentralPointInSeconds         = distanceMatrixElement.duration.inSeconds
            member.commuteTime2CentralPointHumanReadable     = distanceMatrixElement.duration.humanReadable
        }
    }


    private DistanceMatrixElement findDistance(Member member, String destinationAddress) {
        DistanceMatrix distanceMatrix = google.distanceMatrix(member.fullAddress, destinationAddress)

        if (!distanceMatrix || !distanceMatrix.rows ||
                (distanceMatrix.rows.size() == 0) ||
                (distanceMatrix.rows[0].elements.size() == 0)) {
            log.error("Can't find distance from '$member.fullAddress' to '$destinationAddress'")
        }
        else if (distanceMatrix && distanceMatrix.rows && distanceMatrix.rows.size() > 1) {
            log.error("${distanceMatrix.rows.size()} distance matrix rows were found from '$member.fullAddress' to '$destinationAddress'")
        }
        else if (distanceMatrix && distanceMatrix.rows && distanceMatrix.rows[0].elements.size() > 1) {
            log.error("${distanceMatrix.rows[0].elements.size()} distance matrix elements were found from '$member.fullAddress' to '$destinationAddress'")
        }
        else if (distanceMatrix && distanceMatrix.rows && distanceMatrix.rows[0].elements[0].status != DistanceMatrixElementStatus.OK ) {
            log.error("${distanceMatrix.rows[0].elements.size()} distance matrix elements were found from '$member.fullAddress' to '$destinationAddress'")
        }
        else {
            return distanceMatrix.rows[0].elements[0]
        }

        return null
    }




    private static Member findGeodedicInfo4Address(
            final Member memberToBeFound,
            final List<Member> members) {
        return members.find {
            memberToBeFound.address.equals(it.address) &&
            memberToBeFound.city.equals(it.city) &&
            memberToBeFound.zip.equals(it.zip)
        }
    }
}