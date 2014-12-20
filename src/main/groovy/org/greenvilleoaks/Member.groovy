package org.greenvilleoaks

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

@ToString
@EqualsAndHashCode
public final class Member {
    final String     fullName
    final String     lastName
    final String     firstName
    final String     fullAddress
    final String     address
    final String     city
    final Integer    zip
    final Integer    age
    final String     grade
    final LocalDate  birthday
    final Integer    numInHousehold

    String formattedAddress
    Double latitude
    Double longitude
    Long   distanceInMeters
    String distanceHumanReadable
    Long   durationInSeconds
    String durationHumanReadable

    public Member(Map<String, String> memberMap, Map<String, String> propertyNames, DateTimeFormatter dateFormatter) {
        fullName              = memberMap.get(propertyNames.fullName)
        lastName              = memberMap.get(propertyNames.lastName)
        firstName             = memberMap.get(propertyNames.firstName)

        address               = memberMap.get(propertyNames.address)
        city                  = memberMap.get(propertyNames.city)
        zip                   = intValueOf(memberMap.get(propertyNames.zip))
        fullAddress           = memberMap.get(propertyNames.fullAddress) ?: "$address, $city $zip"
        numInHousehold        = intValueOf(memberMap.get(propertyNames.numInHousehold))

        birthday              = dateValueOf(memberMap.get(propertyNames.birthday), dateFormatter)
        age                   = (birthday) ? Period.between(birthday, LocalDate.now()).years : null
        grade                 = memberMap.get(propertyNames.grade)

        latitude              = doubleValueOf(memberMap.get(propertyNames.latitude))
        longitude             = doubleValueOf(memberMap.get(propertyNames.longitude))

        distanceInMeters      = longValueOf(memberMap.get(propertyNames.distanceInMiles))
        distanceHumanReadable = memberMap.get(propertyNames.distanceHumanReadable)

        durationInSeconds     = longValueOf(memberMap.get(propertyNames.distanceInTime))
        durationHumanReadable = memberMap.get(propertyNames.durationHumanReadable)
    }


    public Map<String, String> toMap(Map<String, String> propertyNames) {
        Map<String, String> map = new TreeMap<String, String>()

        map.put(propertyNames.get("fullName"), fullName)
        map.put(propertyNames.get("lastName"), lastName)
        map.put(propertyNames.get("firstName"), firstName)

        map.put(propertyNames.get("address"), address)
        map.put(propertyNames.get("city"), city)
        map.put(propertyNames.get("zip"), valueOf(zip))
        map.put(propertyNames.get("fullAddress"), fullAddress)
        map.put(propertyNames.get("formattedAddress"), formattedAddress)
        map.put(propertyNames.get("numInHousehold"), valueOf(numInHousehold))

        map.put(propertyNames.get("birthday"), birthday ? birthday.format(DateTimeFormatter.ISO_DATE) : "")
        map.put(propertyNames.get("age"), valueOf(age))
        map.put(propertyNames.get("grade"), grade)

        map.put(propertyNames.get("latitude"),  valueOf(latitude))
        map.put(propertyNames.get("longitude"), valueOf(longitude))

        map.put(propertyNames.get("distanceInMeters"), valueOf(distanceInMeters))
        map.put(propertyNames.get("distanceHumanReadable"), distanceHumanReadable)

        map.put(propertyNames.get("durationInSeconds"), valueOf(durationInSeconds))
        map.put(propertyNames.get("durationHumanReadable"), durationHumanReadable)

        return map
    }


    private static <T, E> T headerName2PropertyName(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey()
            }
        }
        return null
    }

    private static Integer intValueOf(String value) {
        return ((value != null) ? Integer.valueOf(value) : null)
    }


    private static Long longValueOf(String value) {
        return ((value != null) ? Long.valueOf(value) : null)
    }

    private static LocalDate dateValueOf(String value, DateTimeFormatter dateFormatter) {
        return (value != null) ? LocalDate.parse(value, dateFormatter) : null
    }


    private static Double doubleValueOf(String value) {
        return (value != null) ? Double.valueOf(value) : null
    }


    private static String valueOf(Integer value) {
        return value ? Integer.valueOf(value) : ""
    }

    private static String valueOf(Long value) {
        return value ? Long.valueOf(value) : ""
    }

    private static String valueOf(Double value) {
        return value ? Double.valueOf(value) : ""
    }
}
