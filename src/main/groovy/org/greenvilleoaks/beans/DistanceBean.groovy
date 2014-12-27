package org.greenvilleoaks.beans

import groovy.transform.Immutable

@Immutable
class DistanceBean {
    String address1, address2
    long distanceInMeters, durationInSeconds
    String distanceHumanReadable, durationHumanReadable
    
    public static final List<String> csvHeaders = [
            "address1", "address2",
            "distanceInMeters",  "distanceHumanReadable",
            "durationInSeconds", "durationHumanReadable"
    ]

    public Map<String, String> toMap() {
        Map<String, String> map = new LinkedHashMap<String, String>()
        map.put("address1", address1)
        map.put("address2", address2)
        map.put("distanceInMeters", Long.toString(distanceInMeters))
        map.put("distanceHumanReadable", distanceHumanReadable)
        map.put("durationInSeconds", Long.toString(durationInSeconds))
        map.put("durationHumanReadable", durationHumanReadable)
        
        return map
    }
}
