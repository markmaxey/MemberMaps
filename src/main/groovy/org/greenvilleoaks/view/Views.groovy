package org.greenvilleoaks.view

import org.greenvilleoaks.beans.MemberBean
import org.greenvilleoaks.config.Config
import org.greenvilleoaks.storage.Spreadsheet

/**
 * A collection of methods related to creating and storing view information about members. 
 */
class Views {
    /**
     * @param config     The config object
     * @param members    The list of all members
     */
    public Map<String, View> createAndStoreViews(final Config config, final List<MemberBean> members) {
        Map<String, View> views = createViews(config, members)

        createStatSpreadsheet(config, views.values(), members)

        storeViewsOnDisk(config, views.values())
        
        return views
    }
    

    /**
     * @param members
     * @return various perspectives of the create
     */
    private Map<String, View> createViews(
            final Config config,
            final List<MemberBean> members) {
        Map<String, View> views = [:]

        views.put(config.membersCsvColumnMappings.city,           new CityView(config.membersCsvColumnMappings.city, members))
        views.put(config.membersCsvColumnMappings.zip,            new ZipView(config.membersCsvColumnMappings.zip, members))
        views.put(config.membersCsvColumnMappings.numInHousehold, new NumInHouseholdView(config.membersCsvColumnMappings.numInHousehold, members))
        views.put(config.membersCsvColumnMappings.age,            new AgeView(config.membersCsvColumnMappings.age, members))
        views.put(config.membersCsvColumnMappings.grade,          new GradeView(config.membersCsvColumnMappings.grade, members))
        views.put(config.membersCsvColumnMappings.role,           new RoleView(config.membersCsvColumnMappings.role, members))
        views.put("Commute Distance in Miles",                    new DistanceView("Commute Distance in Miles", members))
        views.put("Commute Time in Minutes",                      new DurationView("Commute Time in Minutes", members))

        return views
    }


    /**
     * Dump all the perspectives of the create to an Excel workbook
     * @param config   The configuration object 
     * @param views    The collection of all views
     * @param members  The list of all members 
     */
    private void createStatSpreadsheet(
            final Config config,
            final Collection<View> views,
            final List<MemberBean> members) {
        Spreadsheet spreadsheet = new Spreadsheet()

        // Convert the list of members into a list of maps
        List<Map<String, String>> membersListMap = []
        members.each { MemberBean member -> membersListMap << member.toMap(config.membersCsvColumnMappings) }

        // Create the Members tab of the workbook
        spreadsheet.addContent("Members", membersListMap[0].keySet().toArray() as String[], membersListMap)

        // Create a tab per view in the workboook
        views.each { View view ->
            spreadsheet.addContent(view.name, view.headers, view.createStats())
        }

        // Write the workbook to disk
        spreadsheet.writeToFile(config.memberStatsDirName)
    }

    /**
     * Store all the view information into separate CSV files on disk
     *
     * @param config   The configuration object
     * @param views    The collection of all views
     */
    private void storeViewsOnDisk(
            final Config config,
            final Collection<View> views) {
        views.each { it.store(config.memberStatsDirName, config.membersCsvColumnMappings, config.dateFormatter) }
    }
}
