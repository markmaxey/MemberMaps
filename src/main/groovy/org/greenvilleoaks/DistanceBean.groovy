package org.greenvilleoaks

import groovy.transform.Immutable

@Immutable
class DistanceBean {
    String address1, address2
    long distanceInMeters, durationInSeconds
    String distanceHumanReadable, durationHumanReadable
}
