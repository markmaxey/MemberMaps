package org.greenvilleoaks.view

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.greenvilleoaks.Member

/** Members grouped by the city they live in */
@ToString
@EqualsAndHashCode
final class RoleView extends View {
    public RoleView(final String name, final List<Member> members) { super(name, members) }

    public Map<String, List<Member>> createViewData(final List<Member> members) {
        return create(members, { Member member -> member.role ?: NULL_BIN_NAME })
    }
}