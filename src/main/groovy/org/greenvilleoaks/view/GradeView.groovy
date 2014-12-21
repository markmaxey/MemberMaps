package org.greenvilleoaks.view

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.greenvilleoaks.Member

/** Members grouped by the grade they are in */
@ToString
@EqualsAndHashCode
final class GradeView extends View {
    public GradeView(final String name, final List<Member> members) { super(name, members) }

    public Map<String, List<Member>> createViewData(final List<Member> members) {
        return create(members, { Member member -> member.grade ?: NULL_BIN_NAME})
    }
}