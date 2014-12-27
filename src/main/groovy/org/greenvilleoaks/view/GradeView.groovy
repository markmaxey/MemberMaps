package org.greenvilleoaks.view

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.greenvilleoaks.beans.MemberBean

/** Members grouped by the grade they are in */
@ToString
@EqualsAndHashCode
final class GradeView extends View {
    public GradeView(final String name, final List<MemberBean> members) { super(name, members) }

    public Map<String, List<MemberBean>> createViewData(final List<MemberBean> members) {
        return create(members, { MemberBean member -> member.grade ?: NULL_BIN_NAME})
    }
}