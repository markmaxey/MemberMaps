package org.greenvilleoaks.view

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.greenvilleoaks.Member

/** The number of households with a given number of people */
@ToString
@EqualsAndHashCode
final class NumInHouseholdView extends View {
    public NumInHouseholdView(final String name, final List<Member> members) { super(name, members) }

    /**
     * @param members
     * @return A map whose keys are the number of people in the house hold and
     * the values are the people in those households.
     */
    public Map<String, List<Member>> createViewData(final List<Member> members) {
        Map<String, List<Member>> household = create(members, { Member member -> member.fullAddress ?: NULL_BIN_NAME })

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