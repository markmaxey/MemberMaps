package org.greenvilleoaks

import groovy.util.logging.Log4j
import org.greenvilleoaks.view.RoleView
import org.greenvilleoaks.view.View

@Log4j
public final class Members {
    final Config config

    public Members(Config config) {
        this.config = config
    }

    public List<Member> createMembers() {
        List<Member> members      = loadMembers()
        List<Member> geodedicInfo = loadGeodedicInfo()

        createGeodedicInfo4Members(members, geodedicInfo)

        storeGeodedicInfo(geodedicInfo)

        return members
    }

    private List<Member> loadMembers() {
        log.info("Loading members from '$config.membersCsvFileName' ...")
        List<Member> members = []

        new Csv(config.membersCsvFileName).load().each {
            members << new Member(it, config.propertyNames, config.dateFormatter)
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


    private List<Member> loadGeodedicInfo() {
        log.info("Loading cached geodedic information from '$config.geodedicCsvFileName' ...")
        List<Member> members = []

        new Csv(config.geodedicCsvFileName, config.geodedicCsvHeaders).load().each {
            members << new Member(it, config.propertyNames, config.dateFormatter)
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



    /**
     * @return The create with geodedic information
     */
    private void createGeodedicInfo4Members(final List<Member> members, final List<Member> geodedicInfo) {
        config.context.apiKey = config.apiKey

        Geodedic geodedic = new Geodedic(
                config.centralPointAddress,
                geodedicInfo,
                new Google(config.context),
                new RoleView(config.propertyNames.role, members)
        )

        geodedic.create(members)
    }
}
