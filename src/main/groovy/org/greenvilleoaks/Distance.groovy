package org.greenvilleoaks

import com.google.maps.model.DistanceMatrix
import com.google.maps.model.DistanceMatrixElement
import com.google.maps.model.DistanceMatrixElementStatus
import com.google.maps.model.DistanceMatrixRow
import com.google.maps.model.Duration
import groovy.util.logging.Log4j
import org.greenvilleoaks.view.View

@Log4j
final class Distance {
    /** A wrapper around the Google APIs */
    private final Google google


    public Distance(final Google google) {
        this.google = google
    }


    public void addDistance(
            final Member member,
            final String destinationAddress,
            final List<DistanceBean> distanceCache) {
        DistanceMatrixElement distanceMatrixElement = findDistance(member.fullAddress, destinationAddress, distanceCache)

        if (distanceMatrixElement) {
            member.commuteDistance2CentralPointInMeters      = distanceMatrixElement.distance.inMeters
            member.commuteDistance2CentralPointHumanReadable = distanceMatrixElement.distance.humanReadable

            member.commuteTime2CentralPointInSeconds         = distanceMatrixElement.duration.inSeconds
            member.commuteTime2CentralPointHumanReadable     = distanceMatrixElement.duration.humanReadable
        }
    }


    public DistanceMatrixElement findDistance(
            final String sourceAddress,
            final String destinationAddress,
            final List<DistanceBean> distanceCache) {
        // Check to see if the distance information for these two addresses has been cached
        DistanceBean distanceBean
        synchronized (distanceCache) {
            distanceBean = distanceCache.find {
                // The order of the addresses is unimportant
                (it.address1.equals(sourceAddress) && it.address2.equals(destinationAddress)) ||
                        (it.address1.equals(destinationAddress) && it.address2.equals(sourceAddress))
            }
        }

        DistanceMatrixElement distanceMatrixElement
        if (distanceBean == null) {
            // Generate the cached information 
            distanceMatrixElement = googleDistance(sourceAddress, destinationAddress)
            
            synchronized (distanceCache) {
                distanceCache << new DistanceBean(
                        sourceAddress,
                        destinationAddress,
                        distanceMatrixElement.distance.inMeters,
                        distanceMatrixElement.duration.inSeconds,
                        distanceMatrixElement.distance.humanReadable,
                        distanceMatrixElement.duration.humanReadable)
            }
        }
        else {
            distanceMatrixElement = new DistanceMatrixElement()

            distanceMatrixElement.distance               = new com.google.maps.model.Distance()
            distanceMatrixElement.distance.inMeters      = distanceBean.distanceInMeters
            distanceMatrixElement.distance.humanReadable = distanceBean.distanceHumanReadable

            distanceMatrixElement.duration               = new Duration()
            distanceMatrixElement.duration.inSeconds     = distanceBean.durationInSeconds
            distanceMatrixElement.duration.humanReadable = distanceBean.durationHumanReadable
        }
        
        return distanceMatrixElement
    }


    private DistanceMatrixElement googleDistance(
            final String sourceAddress,
            final String destinationAddress) {
        DistanceMatrix distanceMatrix = google.distanceMatrix(sourceAddress, destinationAddress)

        if (!distanceMatrix || !distanceMatrix.rows ||
                (distanceMatrix.rows.size() == 0) ||
                !distanceMatrix.rows[0] ||
                !distanceMatrix.rows[0].elements ||
                (distanceMatrix.rows[0].elements.size() == 0) ||
                !distanceMatrix.rows[0].elements[0]) {
            throw new GoogleException("Can't find distance from '$sourceAddress' to '$destinationAddress'")
        }
        else if (distanceMatrix && distanceMatrix.rows && distanceMatrix.rows.size() > 1) {
            throw new GoogleException("${distanceMatrix.rows.size()} distance matrix rows were found from '$sourceAddress' to '$destinationAddress'")
        }
        else if (distanceMatrix && distanceMatrix.rows && distanceMatrix.rows[0].elements.size() > 1) {
            throw new GoogleException("${distanceMatrix.rows[0].elements.size()} distance matrix elements were found from '$sourceAddress' to '$destinationAddress'")
        }
        else if (distanceMatrix && distanceMatrix.rows && distanceMatrix.rows[0].elements[0].status != DistanceMatrixElementStatus.OK ) {
            throw new GoogleException("${distanceMatrix.rows[0].elements.size()} distance matrix elements were found from '$sourceAddress' to '$destinationAddress'")
        }
        else {
            return distanceMatrix.rows[0].elements[0]
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
    public void addDistancesFromMembers2Roles(
            final Member member,
            final View roleView,
            final List<String> memberRoleCommute,
            final List<DistanceBean> distanceCache) {
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
                    DistanceMatrixElement distanceMatrixElement = findDistance(member.fullAddress, memberInARole.fullAddress, distanceCache)

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
}
