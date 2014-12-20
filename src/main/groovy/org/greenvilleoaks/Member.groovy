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

    final DateTimeFormatter dateFormatter


    /**
     * Construct a member bean
     *
     * @param memberMap A map where the keys are the values in the propertyNames map and the values are the values of the bean
     * @param propertyNames A map where the keys are the property names of the bean and the values are the keys in the memberMap
     * @param dateFormatter A date formatter (for the birthday)
     */
    public Member(
            final Map<String, String> memberMap,
            final Map<String, String> propertyNames,
            final DateTimeFormatter dateFormatter) {
        this.dateFormatter    = dateFormatter

        fullName              = memberMap.get(propertyNames.fullName)
        lastName              = memberMap.get(propertyNames.lastName)
        firstName             = memberMap.get(propertyNames.firstName)

        address               = memberMap.get(propertyNames.address)
        city                  = memberMap.get(propertyNames.city)
        zip                   = intValueOf(memberMap.get(propertyNames.zip))
        fullAddress           = "$address, $city $zip"
        formattedAddress      = memberMap.get(propertyNames.formattedAddress)
        numInHousehold        = intValueOf(memberMap.get(propertyNames.numInHousehold))

        birthday              = dateValueOf(memberMap.get(propertyNames.birthday))
        age                   = (birthday) ? Period.between(birthday, LocalDate.now()).years : null
        grade                 = memberMap.get(propertyNames.grade)

        latitude              = doubleValueOf(memberMap.get(propertyNames.latitude))
        longitude             = doubleValueOf(memberMap.get(propertyNames.longitude))

        distanceInMeters      = longValueOf(memberMap.get(propertyNames.distanceInMeters))
        distanceHumanReadable = memberMap.get(propertyNames.distanceHumanReadable)

        durationInSeconds     = longValueOf(memberMap.get(propertyNames.durationInSeconds))
        durationHumanReadable = memberMap.get(propertyNames.durationHumanReadable)
    }


    /**
     * @param propertyNames A map where the keys are the property names of the bean and the values are the keys in the memberMap
     * @return A member map where the keys are the values of the propertyNames and the values are the property values of the member bean
     */
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

        map.put(propertyNames.get("birthday"), valueOf(birthday))
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


    private Integer intValueOf(String value) {
        return ((value != null) ? Integer.valueOf(value) : null)
    }


    private Long longValueOf(String value) {
        return ((value != null) ? Long.valueOf(value) : null)
    }

    private LocalDate dateValueOf(String value) {
        return (value != null) ? LocalDate.parse(value, dateFormatter) : null
    }


    private Double doubleValueOf(String value) {
        return (value != null) ? Double.valueOf(value) : null
    }


    private String valueOf(Integer value) {
        return value ? Integer.valueOf(value) : null
    }

    private String valueOf(Long value) {
        return value ? Long.valueOf(value) : null
    }

    private String valueOf(Double value) {
        return value ? Double.valueOf(value) : null
    }

    private String valueOf(LocalDate value) {
        return value ? value.format(dateFormatter) : null
    }
}
