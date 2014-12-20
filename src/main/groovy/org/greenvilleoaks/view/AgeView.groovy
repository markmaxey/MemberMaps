package org.greenvilleoaks.view

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.greenvilleoaks.Member

/** Members grouped by their age */
@ToString
@EqualsAndHashCode
final class AgeView extends View {
    public AgeView(final String name, final List<Member> members) { super(name, members) }

    public Map<String, List<Member>> createViewData(final List<Member> members) {
        return create(members, { Member member ->
            String ageBin
            int age = Integer.valueOf(member.age)

            if (age < 20) {
                ageBin = Integer.toString(age)
            } else if (age < 30) {
                ageBin = "20s"
            } else if (age < 40) {
                ageBin = "30s"
            } else if (age < 50) {
                ageBin = "40s"
            } else if (age < 60) {
                ageBin = "50s"
            } else if (age < 70) {
                ageBin = "60s"
            } else if (age < 80) {
                ageBin = "70s"
            } else if (age < 90) {
                ageBin = "80s"
            } else if (age < 100) {
                ageBin = "90s"
            }
            else {
                ageBin = "100s"
            }

            return ageBin
        })
    }
}