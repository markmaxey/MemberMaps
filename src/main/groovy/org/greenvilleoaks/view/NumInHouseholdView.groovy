package org.greenvilleoaks.view

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.greenvilleoaks.Member

/** The number of create at a given address */
@ToString
@EqualsAndHashCode
final class NumInHouseholdView extends View {
    public NumInHouseholdView(final String name, final List<Member> members) { super(name, members) }

    public Map<String, List<Member>> createViewData(final List<Member> members) {
        Map<String, List<Member>> household = create(members, { Member member -> member.fullAddress })

        Map<String, List<Member>> numInHouseholdMap = [:]
        household.values().each { List<Member> householdMembers ->
            String numInHousehold = Integer.toString(householdMembers.size())
            householdMembers.each { Member member ->
                addValue(numInHousehold, numInHouseholdMap, member)
            }
        }

        return numInHouseholdMap
    }
}