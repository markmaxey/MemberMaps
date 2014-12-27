package org.greenvilleoaks

import groovy.util.logging.Log4j
import org.greenvilleoaks.view.RoleView

@Log4j
public final class Members {
    final Config config

    public Members(final Config config) {
        this.config = config
    }

    public List<Member> createMembers() {
        List<Member> members      = loadMembers()
        List<Member> geodedicAddresses = loadGeodedicAddresses()
        List<DistanceBean> distanceCache = loadDistanceCacheData()

        createGeodedicInfo4Members(members, geodedicAddresses, distanceCache)

        storeGeodedicInfo(geodedicAddresses)
        storeDistanceCacheData(distanceCache)

        return members
    }

    private List<Member> loadMembers() {
        log.info("Loading members from '$config.membersCsvFileName' ...")
        List<Member> members = []

        new Csv(config.membersCsvFileName).load().each {
            members << new Member(it, config.propertyNames, config.dateFormatter, config.memberRoleCommute)
        }

        log.info("Loaded ${members.size()} members")

        computeNumInHousehold(members)

        return members
    }


    /**
     * Compute the number of people in each household (address)
     * @param members
     */
    public void computeNumInHousehold(List<Member> members) {
        log.info("Computing the number of people in each household ...")

        Map<String, Integer> fullAddress2NumInHousehold = [:]
        members.each { Member member ->
            Integer numInHousehold = fullAddress2NumInHousehold.get(member.fullAddress)
            if (numInHousehold == null) {
                numInHousehold = new Integer(1)
                fullAddress2NumInHousehold.put(member.fullAddress, numInHousehold)
            }
            else {
                fullAddress2NumInHousehold.put(member.fullAddress, new Integer(numInHousehold + 1))
            }
        }

        members.each { Member member ->
            member.numInHousehold = fullAddress2NumInHousehold.get(member.fullAddress)
        }
    }


    private List<Member> loadGeodedicAddresses() {
        log.info("Loading cached geodedic information from '$config.geodedicCsvFileName' ...")
        List<Member> members = []

        new Csv(config.geodedicCsvFileName, config.geodedicCsvHeaders).load().each {
            members << new Member(it, config.propertyNames, config.dateFormatter, config.memberRoleCommute)
        }

        log.info("Loaded ${members.size()} addresses with geodedic information")

        return members
    }



    private void storeGeodedicInfo(List<Member> geodedicInfo) {
        log.info("Caching geodedic information to '$config.geodedicCsvFileName' ...")

        List<Map<String, Object>> geodedicListOfMaps = []
        geodedicInfo.each { geodedicListOfMaps << it.toMap(config.propertyNames)}

        new Csv(config.geodedicCsvFileName, config.geodedicCsvHeaders).store(geodedicListOfMaps)

        log.info("Cached ${geodedicListOfMaps.size()} addresses with geodedic information")
    }



    public List<DistanceBean> loadDistanceCacheData() {
        log.info("Loading cached data between two addresses '$config.distanceDataCacheCsvFileName' ...")
        List<DistanceBean> distanceCache = []

        new Csv(config.distanceDataCacheCsvFileName, DistanceBean.csvHeaders).load().each {
            distanceCache << new DistanceBean(
                    it.get("address1"),
                    it.get("address2"),
                    Long.valueOf(it.get("distanceInMeters")),
                    Long.valueOf(it.get("durationInSeconds")),
                    it.get("distanceHumanReadable"),
                    it.get("durationHumanReadable"))
        }

        log.info("Loaded ${distanceCache.size()} cached distance data between two addresses")

        return distanceCache
    }



    public void storeDistanceCacheData(List<DistanceBean> distanceCache) {
        log.info("Caching distance data between addresses to '$config.distanceDataCacheCsvFileName' ...")

        List<Map<String, Object>> distanceCacheListOfMaps = []
        distanceCache.each { distanceCacheListOfMaps << it.toMap()}

        new Csv(config.distanceDataCacheCsvFileName, DistanceBean.csvHeaders).store(distanceCacheListOfMaps)

        log.info("Cached ${distanceCacheListOfMaps.size()} distance data between two addresses")
    }



    /**
     * @return The create with geodedic information
     */
    private void createGeodedicInfo4Members(
            final List<Member> members, 
            final List<Member> geodedicAddresses,
            final List<DistanceBean> distanceCache) {
        config.context.apiKey = config.apiKey

        Google google = new Google(config.context)

        Geodedic geodedic = new Geodedic(config.centralPointAddress, google)

        geodedic.create(
                members,
                new RoleView(config.propertyNames.role, members),
                config.memberRoleCommute,
                geodedicAddresses,
                distanceCache,
                new Distance(google)
        )
    }
}
