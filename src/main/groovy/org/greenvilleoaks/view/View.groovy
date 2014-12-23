package org.greenvilleoaks.view

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.greenvilleoaks.Member

/**
 * A perspective of the create.
 */
@ToString
@EqualsAndHashCode
abstract class View {
    public static final String NULL_BIN_NAME = "Unspecified"

    /** The name of the perspective */
    final String name

    /** The perspective data */
    final Map<String, List<Member>> data

    /** The column headers and map keys */
    final String[] headers = ["Category", "Number of Members", "Percentage of Members"]


    public View(final String name, final List<Member> members) {
        this.name = name
        this.data = createViewData(members)
    }



    /**
     * Template method
     * @param members List of member information
     * @return members categorized based on some criteria where the keys are the criteria and the values
     * are the lists of members meeting that criteria.
     */
    abstract Map<String, List<Member>> createViewData(final List<Member> members);



    /**
     * @return histogram statistics of the member's perspective
     */
    public List<Map<String, String>> createStats() {
        int totalNumMembers = calculateTheTotalNumberOfMembers()

        int maxNumMembersInAnyCategory = calculateTheMaximumNumberOfMembersInAnyCategory()

        return histogramStats(totalNumMembers, maxNumMembersInAnyCategory)
    }



    private List<Map<String, String>> histogramStats(int totalNumMembers, maxNumMembersInAnyCategory) {
        List<Map<String, String>> stats = []
        data.each { String category, List<Member> members ->
            double percentage = (double) members.size() / (double) totalNumMembers
            Map<String, String> stat = new LinkedHashMap<String, String>()
            stat.put(headers[0], category)
            stat.put(headers[1], String.format("%${Integer.toString(maxNumMembersInAnyCategory).length()}d", members.size()))
            stat.put(headers[2], String.format("%3d", (int) (percentage * 100)))
            stats << stat
        }
        stats
    }


    private int calculateTheTotalNumberOfMembers() {
        int totalNumMembers = 0
        data.values().each { List<Member> members -> totalNumMembers += members.size() }
        totalNumMembers
    }


    private int calculateTheMaximumNumberOfMembersInAnyCategory() {
        int maxNumMembersInAnyCategory = 0
        data.values().each { List<Member> members ->
            if (maxNumMembersInAnyCategory < members.size()) maxNumMembersInAnyCategory = members.size()
        }
        maxNumMembersInAnyCategory
    }


    protected static void addValue(
            final String filterKey,
            final Map<String, List<Member>> view,
            final Member member) {
        List<Member> entryList = view.get(filterKey)
        if (!entryList) {
            entryList = []
            view.put(filterKey, entryList)
        }

        entryList << member
    }


    protected Map<String, List<Member>> create(
            List<Member> members,
            Closure binName) {
        Map<String, List<Member>> view = [:]

        members.each { Member member ->
            addValue((String)binName.call(member), view, member)
        }

        return view
    }
}
