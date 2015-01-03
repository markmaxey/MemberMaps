package org.greenvilleoaks

import org.greenvilleoaks.beans.DistanceBean
import groovy.util.logging.Log4j
import org.greenvilleoaks.beans.MemberBean
import org.greenvilleoaks.config.Config
import org.greenvilleoaks.config.CsvColumnMappings
import org.greenvilleoaks.storage.Csv
import org.greenvilleoaks.view.RoleView

import java.time.format.DateTimeFormatter

/**
 * Load/Store all member information including cached and/or created
 * geocoded and/or distance information between two addresses
 */
@Log4j
public final class Members {
    final Config config

    public Members(final Config config) {
        this.config = config
    }


    public List<MemberBean> createMembers() {
        List<MemberBean> members = loadMembers()
        loadBonusMemberData(members)
        
        List<MemberBean> geodedicAddresses = loadGeodedicAddresses()

        List<DistanceBean> distanceCache = loadDistanceCacheData()

        createGeodedicInfo4Members(members, geodedicAddresses, distanceCache)
        
        storeMembers(members)

        storeGeodedicInfo(geodedicAddresses)

        storeDistanceCacheData(distanceCache)

        return members
    }



    /** Load member information from a file */
    private List<MemberBean> loadMembers() {
        log.info("Loading members from '$config.membersCsvFileName' ...")
        List<MemberBean> members = []

        new Csv(config.membersCsvFileName).load().each {
            String lastName = it.get(config.membersCsvColumnMappings.lastName)
            if (lastName || !"".equals(lastName.trim())) {
                members << new MemberBean(it, config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)
            }
        }

        log.info("Loaded ${members.size()} members")

        computeNumInHousehold(members)

        return members
    }



    /** Store the modified member information in a CSV file */
    private void storeMembers(List<MemberBean> memberBeans) {
        String fileName = config.memberStatsDirName + "\\" + "Members.csv"
        
        log.info("Storing enhanced membership information to '$fileName' ...")

        List<Map<String, Object>> storedMemberListOfMaps = 
                storeAggregatedMembers(memberBeans, fileName, config.membersCsvColumnMappings, config.dateFormatter)

        log.info("Storing ${storedMemberListOfMaps.size()} enhanced membership")
    }



    /**
     * This method will create a CSV file containing a row for each address in the membership.
     * Members sharing the same address (in the same household) will have their information aggregated together.
     *  
     * @param memberBeans
     * @param fileName
     * @param membersCsvColumnMappings
     * @return The list of maps used to populate the CSV file.
     */
    public static List<Map<String, Object>> storeAggregatedMembers(
            final List<MemberBean> memberBeans,
            final String fileName,
            final CsvColumnMappings membersCsvColumnMappings,
            final DateTimeFormatter dateTimeFormatter) {
        List<Map<String, Object>> storedMemberListOfMaps = []
        memberBeans.each { MemberBean memberBean ->
            Map<String, Object> alreadyAddedMemberMap = findMemberInListOfMemberMaps(storedMemberListOfMaps, memberBean, membersCsvColumnMappings)
            if (alreadyAddedMemberMap) {
                aggregateMembersAtSameAddress(alreadyAddedMemberMap, memberBean, membersCsvColumnMappings, dateTimeFormatter)
            } else {
                storedMemberListOfMaps << memberBean.toMap(membersCsvColumnMappings)
            }
        }

        new Csv(fileName, storedMemberListOfMaps.get(0).keySet()).store(storedMemberListOfMaps)
        
        return storedMemberListOfMaps
    }



    protected static void aggregateMembersAtSameAddress(
            final Map<String, Object> alreadyAddedMemberMap, 
            final MemberBean memberBean,
            final CsvColumnMappings membersCsvColumnMappings,
            final DateTimeFormatter dateTimeFormatter) {
        alreadyAddedMemberMap.put(membersCsvColumnMappings.firstName, alreadyAddedMemberMap.get(membersCsvColumnMappings.firstName) + ", " + memberBean.firstName)
        alreadyAddedMemberMap.put(membersCsvColumnMappings.age,       alreadyAddedMemberMap.get(membersCsvColumnMappings.age)       + ", " + memberBean.age)
        alreadyAddedMemberMap.put(membersCsvColumnMappings.birthday,  alreadyAddedMemberMap.get(membersCsvColumnMappings.birthday)  + ", " + (memberBean.birthday ? memberBean.birthday.format(dateTimeFormatter) : "???"))
        alreadyAddedMemberMap.put(membersCsvColumnMappings.grade,     alreadyAddedMemberMap.get(membersCsvColumnMappings.grade)     + ", " + memberBean.grade)
    }



    protected static Map<String, Object> findMemberInListOfMemberMaps(
            final List<Map<String, Object>> storedMemberListOfMaps,
            final MemberBean memberBean,
            final CsvColumnMappings membersCsvColumnMappings) {
        return storedMemberListOfMaps.find {
            it.get(membersCsvColumnMappings.formattedAddress).equals(memberBean.formattedAddress)
        }
    }


    /** Load member information from a file */
    private List<MemberBean> loadBonusMemberData(List<MemberBean> members) {
        List<MemberBean> bonusMembers = []

        try {
            log.info("Loading bonus members from '$config.bonusMembersCsvFileName' ...")

            new Csv(config.bonusMembersCsvFileName).load().each {
                bonusMembers << new MemberBean(it, config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)
            }

            log.info("Loaded ${bonusMembers.size()} bonus members")

            // Merge the "bonus" member information with standard information loaded in bulk
            mergeBonusMembers(bonusMembers, members)
        }
        catch (Throwable throwable) {
            log.warn(throwable.message)
        }
        
        return bonusMembers
    }


    /**
     * Merge the information found in bonusMembers with members.
     * The merge matches a bonus member with a member by using a compound primary
     * key comprised of any combination of first name, last name, and address.
     * Once a match is found, then any non-null non-primary key values in the bonus member
     * is copied into the member.
     *  
     * @param bonusMembers
     * @param members
     */
    protected void mergeBonusMembers(List<MemberBean> bonusMembers, List<MemberBean> members) {
        bonusMembers.each { MemberBean bonusMember ->
            List<MemberBean> matchingMembers = findAllMembersMatchingTheBonusMember(bonusMember, members)

            if (matchingMembers != null) {
                matchingMembers.each { MemberBean matchingMember ->
                    config.membersCsvColumnMappings.metaClass.getProperties().each {
                        String propertyName  = it.name
                        if (!"class".equals(propertyName)) {
                            String propertyValue = bonusMember.getProperty(propertyName)
                            setBonusMemberProperty(matchingMember, propertyName, propertyValue)
                        }
                    }
                }
            }
        }
    }
    
    
    protected void setBonusMemberProperty(final MemberBean memberBean, final String propertyName, final String propertyValue) {
        // Don't merge the primary keys or properties whose values are not specified in the bonus information
        if (!"firstName".equals(propertyName) &&
                !"lastName".equals(propertyName) &&
                !"address".equals(propertyName)  &&
                !"city".equals(propertyName) &&
                !"zip".equals(propertyName) &&
                (propertyValue != null)) {
            try {
                memberBean.setProperty(propertyName, propertyValue)
            }
            catch (Throwable ex) {
                ex.printStackTrace()
                throw new RuntimeException("'$propertyName' is an invalid bonus member property", ex)
            }
        }
    }
    
    

    /**
     * @param bonusMember
     * @param members
     * @return A list of members matching the first name, last name, and full address of the "bonus" member.  If the
     *         information wasn't specified for the bonus member, then that particular attribute is ignored in the match.
     */
    protected List<MemberBean> findAllMembersMatchingTheBonusMember(final MemberBean bonusMember, final List<MemberBean> members) {
        List<MemberBean> matchingMembers = members.findAll {
            ((bonusMember.firstName == null) || bonusMember.firstName.equals(it.firstName)) &&
                    ((bonusMember.lastName == null) || bonusMember.lastName.equals(it.lastName)) &&
                    ((bonusMember.fullAddress == null) || bonusMember.fullAddress.equals(it.fullAddress))
        }
        matchingMembers
    }


    /**
     * Compute the number of people in each household (address)
     * @param members
     */
    public void computeNumInHousehold(List<MemberBean> members) {
        log.info("Computing the number of people in each household ...")

        Map<String, Integer> fullAddress2NumInHousehold = [:]
        members.each { MemberBean member ->
            Integer numInHousehold = fullAddress2NumInHousehold.get(member.fullAddress)
            if (numInHousehold == null) {
                numInHousehold = new Integer(1)
                fullAddress2NumInHousehold.put(member.fullAddress, numInHousehold)
            }
            else {
                fullAddress2NumInHousehold.put(member.fullAddress, new Integer(numInHousehold + 1))
            }
        }

        members.each { MemberBean member ->
            member.numInHousehold = fullAddress2NumInHousehold.get(member.fullAddress)
        }
    }


    private List<MemberBean> loadGeodedicAddresses() {
        log.info("Loading cached geodedic information from '$config.geodedicCsvFileName' ...")
        List<MemberBean> members = []

        new Csv(config.geodedicCsvFileName, config.geodedicCsvHeaderList).load().each {
            members << new MemberBean(it, config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)
        }

        log.info("Loaded ${members.size()} addresses with geodedic information")

        return members
    }



    private void storeGeodedicInfo(List<MemberBean> geodedicInfo) {
        log.info("Caching geodedic information to '$config.geodedicCsvFileName' ...")

        List<Map<String, Object>> geodedicListOfMaps = []
        geodedicInfo.each { geodedicListOfMaps << it.toMap(config.membersCsvColumnMappings)}

        new Csv(config.geodedicCsvFileName, config.geodedicCsvHeaderList).store(geodedicListOfMaps)

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
            final List<MemberBean> members,
            final List<MemberBean> geodedicAddresses,
            final List<DistanceBean> distanceCache) {
        Google google = new Google(config.google.context)

        Geodedic geodedic = new Geodedic(config.centralPointAddress, google)

        geodedic.create(
                members,
                new RoleView(config.membersCsvColumnMappings.role, members),
                config.memberRoleCommuteList,
                geodedicAddresses,
                distanceCache,
                new Distance(google)
        )
    }
}
