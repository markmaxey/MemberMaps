package org.greenvilleoaks.beans

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

@ToString(includeNames = true, includeFields = true)
@EqualsAndHashCode
public class MemberBean {
    String     fullName
    String     lastName
    String     firstName
    String     fullAddress
    String     address
    String     city
    Integer    zip
    Integer    age
    String     grade
    LocalDate  birthday
    String     role
    
    String primaryKey

    Integer numInHousehold
    String formattedAddress
    Double latitude
    Double longitude
    Long   commuteDistance2CentralPointInMeters
    String commuteDistance2CentralPointHumanReadable
    Long   commuteTime2CentralPointInSeconds
    String commuteTime2CentralPointHumanReadable

    final DateTimeFormatter dateFormatter
    final List<String> memberRoleCommute


    /**
     * Construct a member bean
     *
     * @param memberMap A map where the keys are the values in the csvColumnMappings map and the values are the values of the bean
     * @param propertyNames A map where the keys are the property names of the bean and the values are the keys in the memberMap
     * @param dateFormatter A date formatter (for the birthday)
     * @param memberRoleCommute A list of roles that should be used to compute the minimum distance from a member to any member in that role
     *                          These roles will be used to create dynamic properties on the bean.
     */
    public MemberBean(
            final Map<String, String> memberMap,
            final Map<String, String> propertyNames,
            final DateTimeFormatter dateFormatter,
            final List<String> memberRoleCommute) {
        this.dateFormatter     = dateFormatter
        this.memberRoleCommute = memberRoleCommute

        fullName              = memberMap.get(propertyNames.fullName)
        lastName              = memberMap.get(propertyNames.lastName)
        firstName             = memberMap.get(propertyNames.firstName)

        address               = memberMap.get(propertyNames.address)
        city                  = memberMap.get(propertyNames.city)
        zip                   = intValueOf(memberMap.get(propertyNames.zip))
        formattedAddress      = memberMap.get(propertyNames.formattedAddress)

        birthday              = dateValueOf(memberMap.get(propertyNames.birthday))
        age                   = (birthday) ? Period.between(birthday, LocalDate.now()).years : null
        grade                 = memberMap.get(propertyNames.grade)
        role                  = memberMap.get(propertyNames.role)

        latitude              = doubleValueOf(memberMap.get(propertyNames.latitude))
        longitude             = doubleValueOf(memberMap.get(propertyNames.longitude))

        commuteDistance2CentralPointInMeters      = longValueOf(memberMap.get(propertyNames.commuteDistance2CentralPointInMeters))
        commuteDistance2CentralPointHumanReadable = memberMap.get(propertyNames.commuteDistance2CentralPointHumanReadable)

        commuteTime2CentralPointInSeconds     = longValueOf(memberMap.get(propertyNames.commuteTime2CentralPointInSeconds))
        commuteTime2CentralPointHumanReadable = memberMap.get(propertyNames.commuteTime2CentralPointHumanReadable)

        if (address && city && zip) {
            fullAddress = address + ", " + city + " " + zip
        } else if (address && city) {
            fullAddress = address + ", " + city
        } else if (address && zip) {
            fullAddress = address + " " + zip
        } else if (city && zip) {
            fullAddress = city + " " + zip
        } else if (address) {
            fullAddress = address
        } else if (city) {
            fullAddress = city
        } else if (zip) {
            fullAddress = zip
        }
        else {
            fullAddress = null
        }
        

        // Generate the primary key / unique Id
        primaryKey = fullAddress + " " + lastName + ", " + firstName + " " + UUID.randomUUID().toString()


        // Initialize the dynamic properties driven by the roles
        memberRoleCommute.each { String role ->
            this.metaClass.("Minimum Commute Distance In Meters to " + role) = memberMap.get("Minimum Commute Distance In Meters to " + role)
            this.metaClass.("Minimum Commute Distance to " + role)           = memberMap.get("Minimum Commute Distance to " + role)
            this.metaClass.("Minimum Commute Time In Seconds to " + role)    = memberMap.get("Minimum Commute Time In Seconds to " + role)
            this.metaClass.("Minimum Commute Time to " + role)               = memberMap.get("Minimum Commute Time to " + role)
            this.metaClass.("Minimum Commute to " + role)                    = memberMap.get("Minimum Commute to " + role)
        }
    }


    /**
     * @param propertyNames A map where the keys are the property names of the bean and the values are the keys in the memberMap
     * @return A member map where the keys are the values of the csvColumnMappings and the values are the property values of the member bean
     */
    public Map<String, String> toMap(Map<String, String> propertyNames) {
        Map<String, String> map = new LinkedHashMap<String, String>()

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
        map.put(propertyNames.get("role"), role)
        map.put(propertyNames.get("primaryKey"), primaryKey)

        map.put(propertyNames.get("latitude"),  valueOf(latitude))
        map.put(propertyNames.get("longitude"), valueOf(longitude))

        map.put(propertyNames.get("commuteDistance2CentralPointInMeters"), valueOf(commuteDistance2CentralPointInMeters))
        map.put(propertyNames.get("commuteDistance2CentralPointHumanReadable"), commuteDistance2CentralPointHumanReadable)

        map.put(propertyNames.get("commuteTime2CentralPointInSeconds"), valueOf(commuteTime2CentralPointInSeconds))
        map.put(propertyNames.get("commuteTime2CentralPointHumanReadable"), commuteTime2CentralPointHumanReadable)

        memberRoleCommute.each { String role ->
            map.put("Minimum Commute Distance In Meters to " + role, this.("Minimum Commute Distance In Meters to " + role))
            map.put("Minimum Commute Distance to " + role,           this.("Minimum Commute Distance to " + role))
            map.put("Minimum Commute Time In Seconds to " + role,    this.("Minimum Commute Time In Seconds to " + role))
            map.put("Minimum Commute Time to " + role,               this.("Minimum Commute Time to " + role))
            map.put("Minimum Commute to " + role,                    this.("Minimum Commute to " + role))
        }

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
