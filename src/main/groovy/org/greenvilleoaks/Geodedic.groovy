package org.greenvilleoaks

import com.google.maps.model.AddressComponent
import com.google.maps.model.AddressComponentType
import com.google.maps.model.GeocodingResult
import groovy.util.logging.Log4j
import org.greenvilleoaks.beans.DistanceBean
import org.greenvilleoaks.beans.MemberBean
import org.greenvilleoaks.storage.FileUtils
import org.greenvilleoaks.view.View

import java.lang.reflect.UndeclaredThrowableException

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
     * @return  A string containing the log of addresses with conflicts, mismatches, or issues
     */
    public void create(
            final List<MemberBean> members,
            final View roleView,
            final List<String> memberRoleCommute,
            final List<MemberBean> geodedicAddresses,
            final List<DistanceBean> distanceCache,
            final Distance distance,
            final String badAddressesFileName=null) {
        log.info("Geocoding members addresses ...")
        StringBuffer badAddresses = new StringBuffer()

        try {
            // Geocode any addresses that were missing from the Geodedic CSV file
            // Use JDK 8 fork/join to work on each member in a different thread in parallel efficiently
            members.parallelStream().forEach({member ->
                create(member, roleView, memberRoleCommute, geodedicAddresses, distanceCache, distance, badAddresses)
            })

            log.info("Finished geocoding members addresses")
        }
        catch (UndeclaredThrowableException ex) {
            log.error("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@")
            log.error(ex.undeclaredThrowable.message, ex.undeclaredThrowable)
            log.error("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@")
            
            throw ex
        }
        finally {
            if (badAddressesFileName) logBadAddresses(badAddresses.toString(), badAddressesFileName)
        }
    }



    private void logBadAddresses(String badAddresses, String fileName) {
        log.warn("Logging to disk the following questionable addresses to $fileName:\n" + badAddresses)

        FileUtils.createParentDirs(fileName)
        new File(fileName).write(badAddresses)

        log.info("Finished logging bad addresses to disk")
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
     * @param badAddresses         The log of addresses with conflicts, mismatches, or issues
     */
    public void create(
            final MemberBean member,
            final View roleView,
            final List<String> memberRoleCommute,
            final List<MemberBean> geodedicAddresses,
            final List<DistanceBean> distanceCache,
            final Distance distance,
            final StringBuffer badAddresses) {
        log.info("Creating geodedic information for '" + member.directoryName + "' in thread " + Thread.currentThread().toString())
        MemberBean geodedicInfo
        synchronized (geodedicAddresses) {
            geodedicInfo = findGeodedicInfo4Address(member, geodedicAddresses)
        }
        if (!geodedicInfo) {
            log.info("Geodedic information for '" + member.directoryName + "' was NOT cached.  Going to Google to create information from scratch ...")

            try {
                createGeodedicInfo4AMember(member, roleView, memberRoleCommute, distanceCache, distance)

                // Add the address' geodedic information so that subsequent
                // members at the same address or subsequent runs of the
                // program won't have to use the Google APIs that are both
                // slow and have limits on their usage.
                synchronized (geodedicAddresses) {
                    geodedicAddresses << member
                }
            }
            catch (GoogleException ex) {
                badAddresses.append("\t")
                badAddresses.append(ex.message)
                badAddresses.append("\n")
                log.error(ex.message)
            }
        }
        else {
            log.info("Geodedic information for '" + member.directoryName + "' was cached.")
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
            final MemberBean member,
            final View roleView,
            final List<String> memberRoleCommute,
            final List<DistanceBean> distanceCache,
            final Distance distance) {
        log.info("Creating geocoding information for " + member.directoryName)

        geocode(member)

        distance.addDistance(member, centralAddress, distanceCache)
        distance.addDistancesFromMembers2Roles(member, roleView, memberRoleCommute, distanceCache)
    }



    /**
     * Having loaded the persistent information from a file instead of asking Google for it,
     * save this information in the member.
     *
     * @param memberRoleCommute    The shortest commute will be found for each member to another member in each of these roles.
     * @param geodedicInfo         Cached geodedic information about an address of a member
     * @param member               The member to update with the geodedic information
     */
    private static void addCachedGeodedicInfo2Member(
            final List<String> memberRoleCommute,
            final MemberBean geodedicInfo,
            final MemberBean member) {
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



    private void geocode(MemberBean member) {
        GeocodingResult[] results = google.geocode(member.fullAddress)

        if (!results || (results.length == 0)) {
            throw new GoogleException("No address was found for '$member.fullAddress'")
        }
        else if (results && results.length > 1) {
            String message = "${results.length} addresses were found for '$member.fullAddress':\n"
            results.each { message += "\t" + it.formattedAddress + "\n" }
            throw new GoogleException(message)
        }
        else {
            verifyZipReturnedMatchesMemberZip(extractZipCodeFromGeocodeResults(results), member.zip, member.fullAddress)
            
            member.latitude         = results[0].geometry.location.lat
            member.longitude        = results[0].geometry.location.lng
            member.formattedAddress = results[0].formattedAddress
        }
    }

    
    /** Throw an exception if the given zip code doesn't match that of the member (if one was provided) */
    protected void verifyZipReturnedMatchesMemberZip(String googleZip, Integer memberZip, String fullAddress) {
        if (googleZip && memberZip && !"".equals(memberZip) && !"".equals(googleZip) && !googleZip.equals(Integer.toString(memberZip))) {
            throw new GoogleException("When trying to geocode '$fullAddress', Google returned a zip of '$googleZip' which didn't match the zip code provided '$memberZip'")
        }
    }



    /**
     * @param results    The results from calling the Google Geocode API
     * @return The zip code returned by Google 
     */
    protected String extractZipCodeFromGeocodeResults(final GeocodingResult[] results) {
        // Extract the postal code from the returned address
        AddressComponent addressComponent = results[0].addressComponents.find {
            it.types.find { AddressComponentType.POSTAL_CODE.equals(it) }
        }
        
        if (!addressComponent || !addressComponent.longName || "".equals(addressComponent.longName)) {
            String msg = "Cannot find an addressComponent that has a POSTAL_CODE"
            StringBuilder stringBuilder = new StringBuilder("$msg:\n")
            results.each { GeocodingResult result ->
                stringBuilder.append("\tresult\n")
                result.addressComponents.each { AddressComponent ac ->
                    stringBuilder.append("\t\taddressComponent\n")
                    ac.types.each { stringBuilder.append("\t\t\ttype=${it.toString()} longName=${ac.longName} shortName=${ac.shortName}\n") }
                }
            }
            log.warn(stringBuilder.toString())
            return null
        }
        
        return addressComponent.longName
    }


    private static MemberBean findGeodedicInfo4Address(
            final MemberBean memberToBeFound,
            final List<MemberBean> members) {
        return members.find {
            memberToBeFound.address.equals(it.address) &&
            memberToBeFound.city.equals(it.city) &&
            memberToBeFound.zip.equals(it.zip)
        }
    }
}