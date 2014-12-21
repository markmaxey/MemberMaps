package org.greenvilleoaks

public final class Members {
    final Config config

    public Members(Config config) {
        this.config = config
    }

    public List<Member> createMembers() {
        List<Member> members      = loadMembers()
        List<Member> geodedicInfo = loadGeodedicInfo()
        geocodeMembers(members, geodedicInfo)
        storeGeodedicInfo(geodedicInfo)
        return members
    }

    private List<Member> loadMembers() {
        List<Member> members = []

        new Csv(config.membersCsvFileName).load().each {
            members << new Member(it, config.propertyNames, config.dateFormatter)
        }

        computeNumInHousehold(members)

        return members
    }



    public void computeNumInHousehold(List<Member> members) {
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
        List<Member> members = []

        new Csv(config.geodedicCsvFileName, config.geodedicCsvHeaders).load().each {
            members << new Member(it, config.propertyNames, config.dateFormatter)
        }

        return members
    }


    private void storeGeodedicInfo(List<Member> geodedicInfo) {
        List<Map<String, Object>> geodedicListOfMaps = []
        geodedicInfo.each { geodedicListOfMaps << it.toMap(config.propertyNames)}

        new Csv(config.geodedicCsvFileName, config.geodedicCsvHeaders).store(geodedicListOfMaps)
    }



    /**
     * @return The create with geodedic information
     */
    private void geocodeMembers(final List<Member> members, final List<Member> geodedicInfo) {
        config.context.apiKey = config.apiKey

        Geodedic geocode = new Geodedic(config.centralAddress, geodedicInfo, new Google(config.context))

        geocode.create(members)
    }
}
