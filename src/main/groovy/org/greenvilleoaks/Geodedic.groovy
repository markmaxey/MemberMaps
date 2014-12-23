package org.greenvilleoaks

import com.google.maps.model.DistanceMatrix
import com.google.maps.model.DistanceMatrixElement
import com.google.maps.model.DistanceMatrixElementStatus
import com.google.maps.model.GeocodingResult
import groovy.util.logging.Log4j
import org.greenvilleoaks.view.View

@Log4j
final class Geodedic {
    /** The address of a central location we want to find the distance to for each member */
    private final String centralAddress

    /** A wrapper around the Google APIs */
    private final Google google


    /**
     * Constructor
     * @param centralAddress       The address of a central location we want to find the distance to for each member
     * @param geodedicAddresses    Cached geodedic information for addresses (not fully populated with member info)
     * @param google               A wrapper around the Google APIs
     */
    public Geodedic(
            final String centralAddress,
            final Google google) {
        this.centralAddress    = centralAddress
        this.google            = google
    }



    /**
     * Add geodedic information (latitude, longitude, & Distance from a central point) to geodedicAddresses
     *
     * @param members              The list of members
     * @param roleView             A perspective of all members where each role has a list of members
     * @param memberRoleCommute    The shortest commute will be found for each member to another member in each of these roles.
     * @param geodedicAddresses    In/Out: Geodedic information for addresses (not fully populated with member info).
     *                             This list can be prepopulated with cached results.  It will be updated with all
     *                             the geodedic information for all addresses of all members.
     */
    public void create(
            final List<Member> members,
            final View roleView,
            final List<String> memberRoleCommute,
            final List<Member> geodedicAddresses) {
        log.info("Geocoding members addresses ...")

        // Geocode any addresses that were missing from the Geodedic CSV file
        members.each { Member member ->
            create(member, roleView, memberRoleCommute, geodedicAddresses)
        }

        log.info("Finished geocoding members addresses")
    }



    /**
     * Add geodedic information (latitude, longitude, & Distance from a central point) to geodedicAddresses
     *
     * @param members              A member
     * @param roleView             A perspective of all members where each role has a list of members
     * @param memberRoleCommute    The shortest commute will be found for each member to another member in each of these roles.
     * @param geodedicAddresses    In/Out: Geodedic information for addresses (not fully populated with member info).
     *                             This list can be prepopulated with cached results.  It will be updated with all
     *                             the geodedic information for all addresses of all members.
     */
    public void create(
            final Member member,
            final View roleView,
            final List<String> memberRoleCommute,
            final List<Member> geodedicAddresses) {
        Member geodedicInfo
        synchronized (geodedicAddresses) {
            geodedicInfo = findGeodedicInfo4Address(member, geodedicAddresses)
        }
        if (!geodedicInfo) {
            createGeodedicInfo4AMember(member, roleView, memberRoleCommute)

            // Add the address' geodedic information so that subsequent
            // members at the same address or subsequent runs of the
            // program won't have to use the Google APIs that are both
            // slow and have limits on their usage.
            synchronized (geodedicAddresses) {
                geodedicAddresses << member
            }
        }
        else {
            addCachedGeodedicInfo2Member(memberRoleCommute, geodedicInfo, member)
        }
    }



   /**
     * Create the geodedic information for the address of a member.
     *
     * @param members              A member whose geodedic information will be updated by this method
     * @param roleView             A perspective of all members where each role has a list of members
     * @param memberRoleCommute    The shortest commute will be found for each member to another member in each of these roles.
     */
    private void createGeodedicInfo4AMember(
            final Member member,
            final View roleView,
            final List<String> memberRoleCommute) {
        log.info("Creating geocoding information for " + member)
        geocode(member)
        addDistance(member, centralAddress)
        addDistancesFromMembers2Roles(member, roleView, memberRoleCommute)
    }



    /**
     * Having loaded the persistent information from a file instead of asking Google for it,
     * save this information in the member.
     *
     * @param memberRoleCommute    The shortest commute will be found for each member to another member in each of these roles.
     * @param geodedicInfo         Cached geodedic information about an address of a member
     * @param member               The member to update with the geodedic information
     */
    private void addCachedGeodedicInfo2Member(
            final List<String> memberRoleCommute,
            final Member geodedicInfo,
            final Member member) {
        member.latitude         = geodedicInfo.latitude
        member.longitude        = geodedicInfo.longitude
        member.formattedAddress = geodedicInfo.formattedAddress

        member.commuteDistance2CentralPointInMeters      = geodedicInfo.commuteDistance2CentralPointInMeters
        member.commuteDistance2CentralPointHumanReadable = geodedicInfo.commuteDistance2CentralPointHumanReadable
        member.commuteTime2CentralPointInSeconds         = geodedicInfo.commuteTime2CentralPointInSeconds
        member.commuteTime2CentralPointHumanReadable     = geodedicInfo.commuteTime2CentralPointHumanReadable

        memberRoleCommute.each { String role ->
            member.setProperty("Minimum Commute Distance In Meters to " + role, geodedicInfo.getProperty("Minimum Commute Distance In Meters to " + role))
            member.setProperty("Minimum Commute Distance to " + role,           geodedicInfo.getProperty("Minimum Commute Distance to " + role))
            member.setProperty("Minimum Commute Time In Seconds to " + role,    geodedicInfo.getProperty("Minimum Commute Time In Seconds to " + role))
            member.setProperty("Minimum Commute Time to " + role,               geodedicInfo.getProperty("Minimum Commute Time to " + role))
            member.setProperty("Minimum Commute to " + role,                    geodedicInfo.getProperty("Minimum Commute to " + role))
        }
    }



    /**
     * Some members fill a certain role (e.g, small group leader, staff, etc.).  This method will add distance
     * information to the given member for the shortest commute from the given member to any member in a given role.
     * Only the roles specified in memberRoleCommute will be used.
     *
     * @param members              A member
     * @param roleView             A perspective of all members where each role has a list of members
     * @param memberRoleCommute    The shortest commute will be found for each member to another member in each of these roles.
     */
    private void addDistancesFromMembers2Roles(
            final Member member,
            final View roleView,
            final List<String> memberRoleCommute) {
        // For each role that we care about
        memberRoleCommute.each { String role ->
            Member minMember = null
            DistanceMatrixElement minDistance  = null

            // Create a list of members in that role
            List<Member> membersInARole = roleView.data.get(role)

            // Determine which member in the role lives closest to the given member
            log.info("Calculating distance from " + member.firstName + " " + member.lastName + " to all " +
                    membersInARole.size() + " members in role " + role)
            membersInARole.each { Member memberInARole ->
                // Skip members that live in the same household
                if (member.fullAddress != memberInARole.fullAddress) {
                    DistanceMatrixElement distanceMatrixElement = findDistance(member, memberInARole.fullAddress)

                    if ((minDistance == null) ||
                            (minDistance.distance.inMeters > distanceMatrixElement.distance.inMeters)) {
                        minMember   = memberInARole
                        minDistance = distanceMatrixElement
                    }
                }
            }
            log.info("The closest $role to '$minMember.fullName' is '$member.fullName'")

            member.setProperty("Minimum Commute Distance In Meters to " + role, minDistance.distance.inMeters)
            member.setProperty("Minimum Commute Distance to " + role,           minDistance.distance.humanReadable)
            member.setProperty("Minimum Commute Time In Seconds to " + role,    minDistance.duration.inSeconds)
            member.setProperty("Minimum Commute Time to " + role,               minDistance.duration.humanReadable)
            member.setProperty("Minimum Commute to " + role,                    minMember.fullName)
        }
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