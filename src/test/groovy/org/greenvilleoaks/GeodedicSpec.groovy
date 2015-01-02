package org.greenvilleoaks

import com.google.maps.model.DistanceMatrix
import com.google.maps.model.DistanceMatrixElementStatus
import com.google.maps.model.GeocodingResult
import org.greenvilleoaks.beans.MemberBean
import org.greenvilleoaks.config.Config
import org.greenvilleoaks.mocks.GoogleFaultCode
import org.greenvilleoaks.mocks.GoogleMock
import org.greenvilleoaks.view.RoleView
import spock.lang.Shared
import spock.lang.Specification

class GeodedicSpec extends Specification {
    @Shared Config config = new Config().init()
    @Shared List<String> roles = [
            "Member",
            "Small Group Leader",
            "Member",
            "Member",
            "Member",
            "Member",
            "Member",
            "Member",
            "Member",
            "Member",
            "Member",
            "Member",
            "Small Group Leader",
            "Member",
    ]



    def "Verify Mock"() {
        setup:
        GeocodingResult[] geocodingResults = 
                new GoogleMock(
                        [GoogleFaultCode.none, GoogleFaultCode.none],
                        [] as Set<String>).geocode("1")
        Set<String> addressesGeocoded1 = [] as Set<String>
        DistanceMatrix distanceMatrix1 = 
                new GoogleMock([GoogleFaultCode.none, GoogleFaultCode.none], addressesGeocoded1).
                        distanceMatrix("1", "3")
        Set<String> addressesGeocoded6 = [] as Set<String>
        DistanceMatrix distanceMatrix6 = new GoogleMock([
                GoogleFaultCode.none,
                GoogleFaultCode.none,
                GoogleFaultCode.none,
                GoogleFaultCode.none,
                GoogleFaultCode.none,
                GoogleFaultCode.none,
        ], addressesGeocoded6).distanceMatrix(["0", "1"], ["2", "3", "4"])

        expect:
        geocodingResults.length == 1
        geocodingResults[0].geometry.location.lat == Double.valueOf("1")
        geocodingResults[0].geometry.location.lng == Double.valueOf("1")

        distanceMatrix1.rows.length == 1
        distanceMatrix1.rows[0].elements.length == 1
        distanceMatrix1.rows[0].elements[0].distance.inMeters == 2
        distanceMatrix1.rows[0].elements[0].distance.humanReadable == "2"
        distanceMatrix1.rows[0].elements[0].duration.inSeconds == 2
        distanceMatrix1.rows[0].elements[0].duration.humanReadable == "2"
        distanceMatrix1.rows[0].elements[0].status == DistanceMatrixElementStatus.OK

        distanceMatrix6.rows.length == 2
        distanceMatrix6.rows[0].elements.length == 3
        distanceMatrix6.rows[1].elements.length == 3

        distanceMatrix6.rows[0].elements[0].distance.inMeters == 2
        distanceMatrix6.rows[0].elements[0].distance.humanReadable == "2"
        distanceMatrix6.rows[0].elements[0].duration.inSeconds == 2
        distanceMatrix6.rows[0].elements[0].duration.humanReadable == "2"
        distanceMatrix6.rows[0].elements[0].status == DistanceMatrixElementStatus.OK

        distanceMatrix6.rows[0].elements[1].distance.inMeters == 3
        distanceMatrix6.rows[0].elements[1].distance.humanReadable == "3"
        distanceMatrix6.rows[0].elements[1].duration.inSeconds == 3
        distanceMatrix6.rows[0].elements[1].duration.humanReadable == "3"
        distanceMatrix6.rows[0].elements[1].status == DistanceMatrixElementStatus.OK

        distanceMatrix6.rows[0].elements[2].distance.inMeters == 4
        distanceMatrix6.rows[0].elements[2].distance.humanReadable == "4"
        distanceMatrix6.rows[0].elements[2].duration.inSeconds == 4
        distanceMatrix6.rows[0].elements[2].duration.humanReadable == "4"
        distanceMatrix6.rows[0].elements[2].status == DistanceMatrixElementStatus.OK

        distanceMatrix6.rows[1].elements[0].distance.inMeters == 1
        distanceMatrix6.rows[1].elements[0].distance.humanReadable == "1"
        distanceMatrix6.rows[1].elements[0].duration.inSeconds == 1
        distanceMatrix6.rows[1].elements[0].duration.humanReadable == "1"
        distanceMatrix6.rows[1].elements[0].status == DistanceMatrixElementStatus.OK

        distanceMatrix6.rows[1].elements[1].distance.inMeters == 2
        distanceMatrix6.rows[1].elements[1].distance.humanReadable == "2"
        distanceMatrix6.rows[1].elements[1].duration.inSeconds == 2
        distanceMatrix6.rows[1].elements[1].duration.humanReadable == "2"
        distanceMatrix6.rows[1].elements[1].status == DistanceMatrixElementStatus.OK

        distanceMatrix6.rows[1].elements[2].distance.inMeters == 3
        distanceMatrix6.rows[1].elements[2].distance.humanReadable == "3"
        distanceMatrix6.rows[1].elements[2].duration.inSeconds == 3
        distanceMatrix6.rows[1].elements[2].duration.humanReadable == "3"
        distanceMatrix6.rows[1].elements[2].status == DistanceMatrixElementStatus.OK
    }


    def "Error cases"() {
        setup:
        List<MemberBean> members = []
        List<MemberBean> geodedicAddresses = []
        Set<String> addressesGeocoded = [] as Set<String>

        // No members cached
        List<GoogleFaultCode> faultCodes = [
                GoogleFaultCode.none,
                GoogleFaultCode.none,
                GoogleFaultCode.no_geocode_results,
                GoogleFaultCode.multiple_geocode_results,
                GoogleFaultCode.null_distance_matrix,
                GoogleFaultCode.null_distance_matrix_rows,
                GoogleFaultCode.no_distance_matrix_rows,
                GoogleFaultCode.null_distance_matrix_row_elements,
                GoogleFaultCode.no_distance_matrix_row_elements,
                GoogleFaultCode.multiple_distance_matrix_rows,
                GoogleFaultCode.multiple_distance_matrix_row_elements,
                GoogleFaultCode.distance_matrix_status_not_ok,
                GoogleFaultCode.none,
                GoogleFaultCode.geocode_results_zip_mismatch
        ]

        Google google = new GoogleMock(faultCodes, addressesGeocoded)
        Geodedic geodedic = setupData(1, 13, google, config, roles, members)
        geodedic.create(
                members,
                new RoleView(config.membersCsvColumnMappings.role, members),
                config.memberRoleCommuteList,
                geodedicAddresses, [], new Distance(google))



        expect:
        // All but 2, 3, & 13 were geocoded
        assert addressesGeocoded.size() == 11
        [1,4,5,6,7,8,9,10,11,12].each { int memberNum ->
            assert addressesGeocoded.find { it == Integer.toString(memberNum) } != null
        }

        // Only 1 & 12 were completely successful in geocoding the member
        assert geodedicAddresses.size() == 2
        [1, 12].each { int memberNum ->
            assert geodedicAddresses.find { it.zip == memberNum } != null
        }
        
        // The contents of the geocoded members is asserted in next test case below
    }



    def "Members 3-8 are cached and 1, 2, 9, 10, 11, & 12 have their geodedic information computed."() {
        setup:
        List<MemberBean> members           = []
        List<MemberBean> geodedicAddresses = []
        Set<String> addressesGeocoded  = [] as Set<String>

        // There are 12 members where members 3-8 are cached
        List<GoogleFaultCode> faultCodes = []
        for(int ndx=0; ndx<=12; ndx++) faultCodes << GoogleFaultCode.none

        Google google = new GoogleMock(faultCodes, addressesGeocoded)
        Geodedic geodedic = setupData(1, 12, google, config, roles, members)
        setupData(3, 8, google, config, roles, geodedicAddresses)
        setupCacheData(geodedicAddresses)

        geodedic.create(
                members,
                new RoleView(config.membersCsvColumnMappings.role, members),
                config.memberRoleCommuteList,
                geodedicAddresses, [], new Distance(google))

        
        expect:
        6  == addressesGeocoded.size()
        12 == geodedicAddresses.size()

        [1,2,9,10,11,12].each { int memberNum ->
            assert addressesGeocoded.contains(Integer.toString(memberNum))
            assert geodedicAddresses.find { it.zip == memberNum } != null
        }


        members.each { MemberBean member ->
            assert member.latitude    == Double.valueOf(member.zip)
            assert member.longitude   == Double.valueOf(member.zip)
            assert member.fullAddress == Integer.toString(member.zip)

            assert member.commuteDistance2CentralPointInMeters      == Long.valueOf(member.zip)
            assert member.commuteDistance2CentralPointHumanReadable == Long.toString(member.zip)
            assert member.commuteTime2CentralPointInSeconds         == Long.valueOf(member.zip)
            assert member.commuteTime2CentralPointHumanReadable     == Long.toString(member.zip)

            config.memberRoleCommuteList.each { String role ->
                int d = calculateSmallGroupLeaderExpectedDistanceDuration(member.zip)
                assert member.getProperty("Minimum Commute Distance In Meters to " + role) == d
                assert member.getProperty("Minimum Commute Distance to " + role)           == Integer.toString(d)
                assert member.getProperty("Minimum Commute Time In Seconds to " + role)    == d
                assert member.getProperty("Minimum Commute Time to " + role)               == Integer.toString(d)
                
                if (member.zip in [2,3,4,5,12]) {
                    assert member.getProperty("Minimum Commute to " + role)                    == "1"
                }
                else {
                    assert member.getProperty("Minimum Commute to " + role)                    == "12"
                }
            }
        }
    }
    
    

    private void setupCacheData(List<MemberBean> geodedicAddresses) {
        geodedicAddresses.each { MemberBean member ->
            member.latitude         = Double.valueOf(member.zip)
            member.longitude        = Double.valueOf(member.zip)
            member.formattedAddress = Double.valueOf(member.zip)

            member.commuteDistance2CentralPointInMeters      = Long.valueOf(member.zip)
            member.commuteDistance2CentralPointHumanReadable = member.zip
            member.commuteTime2CentralPointInSeconds         = Long.valueOf(member.zip)
            member.commuteTime2CentralPointHumanReadable     = member.zip

            config.memberRoleCommuteList.each { String role ->
                int d = calculateSmallGroupLeaderExpectedDistanceDuration(member.zip)
                member.setProperty("Minimum Commute Distance In Meters to " + role, d)
                member.setProperty("Minimum Commute Distance to " + role,           Integer.toString(d))
                member.setProperty("Minimum Commute Time In Seconds to " + role,    d)
                member.setProperty("Minimum Commute Time to " + role,               Integer.toString(d))
                
                if (member.zip in [3,4,5]) {
                    // This assumes 3, 4, & 5 are cached with 1 being the closest small group leader
                    member.setProperty("Minimum Commute to " + role, "1")
                }
                else {
                    // This assumes the others cached have 12 as the closest small group leader
                    member.setProperty("Minimum Commute to " + role, "12")
                }
            }
        }
    }


    private static int calculateSmallGroupLeaderExpectedDistanceDuration(int memberValue) {
        // NOTE: This is highly dependent on the definition of roles[]
        // Since 1 is a small group leader, 12 is the closest other small group leader
        // Since 12 is a small group leader, 1 is the closest other small group leader
        if ((memberValue == 1) || (memberValue == 12)) {
            return 11 
        }
        else if (memberValue < 6) {
            return Integer.valueOf(Math.abs(Integer.valueOf(memberValue) - 1))
        }
        else {
            return Integer.valueOf(Math.abs(12 - Integer.valueOf(memberValue)))
        }
    }


    private Geodedic setupData(
            final int startNdx,
            final int endNdx,
            final Google google,
            final Config config,
            final List<String> roles,
            final List<MemberBean> members) {
        for(int ndx=startNdx; ndx <= endNdx; ndx++) {
            members << new MemberBean([
                    "Directory Name": Integer.toString(ndx),
                    "Last Name": Integer.toString(ndx),
                    "Preferred Name": Integer.toString(ndx),
                    "Role": roles.get(ndx),
                    "Zip Code": Integer.toString(ndx)
            ], config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)
        }

        return new Geodedic("0", google)
    }
}