package org.greenvilleoaks.view

import com.google.api.services.mapsengine.model.DisplayRule
import com.google.api.services.mapsengine.model.PointStyle
import com.google.api.services.mapsengine.model.VectorStyle
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.util.logging.Log4j
import org.greenvilleoaks.Members
import org.greenvilleoaks.beans.MemberBean
import org.greenvilleoaks.config.CsvColumnMappings
import org.greenvilleoaks.map.LayerAdapter
import org.greenvilleoaks.map.StringOperatorEnum

import java.time.format.DateTimeFormatter

/**
 * A perspective of the create.
 */
@ToString
@EqualsAndHashCode
@Log4j
abstract class View {
    public static final String NULL_BIN_NAME = "Unspecified"

    /** The name of the perspective */
    final String name

    /** The perspective data */
    final Map<String, List<MemberBean>> data

    /** The column headers and map keys */
    final String[] headers = ["Category", "Number of Members", "Percentage of Members"]
    
    final Optional<Comparator> comparator


    public View(final String name, final List<MemberBean> members, final Comparator comparator=null) {
        this.name = name
        this.data = createViewData(members)
        this.comparator = (comparator == null) ? Optional.empty() : Optional.of(comparator)
    }



    /**
     * Template method
     * @param members List of member information
     * @return members categorized based on some criteria where the keys are the criteria and the values
     * are the lists of members meeting that criteria.
     */
    abstract Map<String, List<MemberBean>> createViewData(final List<MemberBean> members);


    /**
     * This method defines the display rules for the view when creating a map layer.
     *
     * @param layerAdapter    The adapter layer to Google's Java Client
     * @param pointStyle      The point style
     * @return one list of display rules per category/bin
     */
    abstract Map<String, List<DisplayRule>> createDisplayRules(
            final CsvColumnMappings csvColumnMappings,
            final LayerAdapter layerAdapter, 
            final PointStyle pointStyle)

    

    /** Allow each view to customize its own style if desired.  This method in the base class provides the default style. */
    public VectorStyle createVectorStyle(
            final LayerAdapter layerAdapter,
            final List<DisplayRule> displayRules,
            final String featureInfoContent
    ) {
        layerAdapter.createStyle(displayRules, featureInfoContent)
    }



    protected List<String> sortedDataKeys() {
        List keys = []
        data.keySet().each { keys << it }
        Collections.sort(keys)
        return keys
    }


    protected TreeMap<String, List<DisplayRule>> constructCategory2DisplayRules() {
        Map<String, List<DisplayRule>> category2DisplayRules = comparator.isPresent() ? new TreeMap<String, List<DisplayRule>>(comparator.get()) : new TreeMap<String, List<DisplayRule>>()
        category2DisplayRules
    }


    protected Map<String, List<DisplayRule>> createIdentityDisplayRules(
            final String csvColumnMapping,
            final LayerAdapter layerAdapter,
            final PointStyle pointStyle) {
        TreeMap<String, List<DisplayRule>> category2DisplayRules = constructCategory2DisplayRules()

        sortedDataKeys().each { String category ->
            if (!NULL_BIN_NAME.equals(category)) {
                category2DisplayRules.put(
                        category,
                        [
                                layerAdapter.createDisplayRule(
                                        pointStyle,
                                        [layerAdapter.createFilter(csvColumnMapping, StringOperatorEnum.equals, category)]
                                )
                        ])
            }
        }

        return category2DisplayRules
    }


    protected Map<String, List<DisplayRule>> createContainsDisplayRules(
            final String csvColumnMapping,
            final LayerAdapter layerAdapter,
            final PointStyle pointStyle) {
        TreeMap<String, List<DisplayRule>> category2DisplayRules = constructCategory2DisplayRules()

        sortedDataKeys().each { String category ->
            if (!NULL_BIN_NAME.equals(category)) {
                category2DisplayRules.put(
                        category,
                        [
                                layerAdapter.createDisplayRule(
                                        pointStyle,
                                        [layerAdapter.createFilter(csvColumnMapping, StringOperatorEnum.contains, category)]
                                )
                        ])
            }
        }

        return category2DisplayRules
    }

    
    
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
        data.each { String category, List<MemberBean> members ->
            double percentage = (double) members.size() / (double) totalNumMembers
            Map<String, String> stat = new LinkedHashMap<String, String>()
            stat.put(headers[0], category)
            stat.put(headers[1], String.format("%${Integer.toString(maxNumMembersInAnyCategory).length()}d", members.size()))
            stat.put(headers[2], String.format("%3d", Math.round(percentage * 100)))
            stats << stat
        }
        stats
    }


    private int calculateTheTotalNumberOfMembers() {
        int totalNumMembers = 0
        data.values().each { List<MemberBean> members -> totalNumMembers += members.size() }
        totalNumMembers
    }


    private int calculateTheMaximumNumberOfMembersInAnyCategory() {
        int maxNumMembersInAnyCategory = 0
        data.values().each { List<MemberBean> members ->
            if (maxNumMembersInAnyCategory < members.size()) maxNumMembersInAnyCategory = members.size()
        }
        maxNumMembersInAnyCategory
    }


    protected static void addValue(
            final String filterKey,
            final Map<String, List<MemberBean>> view,
            final MemberBean member) {
        List<MemberBean> entryList = view.get(filterKey)
        if (!entryList) {
            entryList = []
            view.put(filterKey, entryList)
        }

        entryList << member
    }


    protected Map<String, List<MemberBean>> create(
            List<MemberBean> members,
            Closure binName) {
        Map<String, List<MemberBean>> view = [:]

        members.each { MemberBean member ->
            addValue((String)binName.call(member), view, member)
        }

        return view
    }
    
    
    
    public void store(
            final String baseDirName, 
            final CsvColumnMappings csvColumnMappings,
            final DateTimeFormatter dateTimeFormatter) {
        data.each() { String binName, List<MemberBean> memberBeans -> store(csvColumnMappings, dateTimeFormatter, baseDirName, binName, memberBeans) }
    }



    protected void store(
            final CsvColumnMappings csvColumnMappings,
            final DateTimeFormatter dateTimeFormatter,
            final String baseDirName,
            final String binName, 
            final List<MemberBean> memberBeans) {
        String fileName = baseDirName + "\\" + name + "\\" + binName + ".csv"

        log.info("Storing $name view for $binName to '$fileName' ...")

        Members.storeAggregatedMembers(memberBeans, fileName, csvColumnMappings, dateTimeFormatter)
    }
}
