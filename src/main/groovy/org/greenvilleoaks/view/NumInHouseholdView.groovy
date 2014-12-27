package org.greenvilleoaks.view

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.greenvilleoaks.beans.MemberBean

/** The number of households with a given number of people */
@ToString
@EqualsAndHashCode
final class NumInHouseholdView extends View {
    public NumInHouseholdView(final String name, final List<MemberBean> members) { super(name, members) }

    /**
     * @param members
     * @return A map whose keys are the number of people in the house hold and
     * the values are the people in those households.
     */
    public Map<String, List<MemberBean>> createViewData(final List<MemberBean> members) {
        Map<String, List<MemberBean>> household = create(members, { MemberBean member -> member.fullAddress ?: NULL_BIN_NAME })

        Map<String, List<MemberBean>> numInHouseholdMap = [:]
        household.values().each { List<MemberBean> householdMembers ->
            String numInHousehold = Integer.toString(householdMembers.size())
            householdMembers.each { MemberBean member ->
                addValue(numInHousehold, numInHouseholdMap, member)
            }
        }

        return numInHouseholdMap
    }
}