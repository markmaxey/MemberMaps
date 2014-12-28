package org.greenvilleoaks.map

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.InputStreamContent
import com.google.api.client.json.JsonFactory
import com.google.api.services.mapsengine.MapsEngine
import com.google.api.services.mapsengine.MapsEngineScopes
import com.google.api.services.mapsengine.model.Project
import com.google.api.services.mapsengine.model.ProjectsListResponse
import com.google.api.services.mapsengine.model.Schema
import com.google.api.services.mapsengine.model.Table
import com.google.maps.clients.BackOffWhenRateLimitedRequestInitializer
import com.google.maps.clients.HttpRequestInitializerPipeline
import groovy.util.logging.Log4j
import org.greenvilleoaks.config.CsvColumnMappings
import org.greenvilleoaks.storage.Csv
import org.greenvilleoaks.beans.MemberBean
import org.greenvilleoaks.view.View

import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger

/**
 * https://developers.google.com/maps-engine/documentation/workflow-overview
 */
@Log4j
final class Workflow {
    private final MapsEngine        engine
    private final List<MemberBean>  members
    private final Map<String, View> views
    private final CsvColumnMappings csvColumnMappings

    public Workflow(
            final List<MemberBean> members,
            final Map<String, View> views,
            final HttpTransport httpTransport,
            final JsonFactory jsonFactory,
            final CsvColumnMappings csvColumnMappings,
            final String applicationName,
            final File secretsFile) {
        enableHttpLogging()
        
        this.members           = members
        this.views             = views
        this.csvColumnMappings = csvColumnMappings

        log.info("Authorizing")
        GoogleCredential credential = new Auth().authorizeService(
                httpTransport, jsonFactory, Arrays.asList(MapsEngineScopes.MAPSENGINE), secretsFile)
        log.info("Authorization successful for user '" + credential.getServiceAccountUser() +
                "' and service account ID '" + credential.getServiceAccountId() + "'")


        // Set up the required initializers to 1) authenticate the request and 2) back off if we
        // start hitting the server too quickly.
        HttpRequestInitializer requestInitializers = new HttpRequestInitializerPipeline(
                Arrays.asList(credential, new BackOffWhenRateLimitedRequestInitializer()));

        
        // The MapsEngine object will be used to perform the requests.
        log.info("Creating MapEngine for $applicationName")
        engine = new MapsEngine.Builder(httpTransport, jsonFactory, requestInitializers)
                .setApplicationName(applicationName)
                .build();
    }


    private void enableHttpLogging() {
        ConsoleHandler consoleHandler = new ConsoleHandler()
        consoleHandler.setLevel(Level.FINEST)
        Logger.getLogger(HttpTransport.class.getName()).setLevel(Level.FINEST)
        Logger.getLogger(HttpTransport.class.getName()).addHandler(consoleHandler)
    }


    public void run(final String projectId) {
        findAllProjects()
        
        String fileName = System.properties.getProperty("user.home") + "\\Documents\\GO_Members_Map\\dummy.txt"
        log.info("Creating an empty table in Maps Engine, under project ID " + projectId);
        Table table = createTable(projectId, [fileName], "goTableName", "goTableDescription", "Unique Id", ["tag1, tag2"]);
        log.info("Table created, ID is: " + table.getId());

        log.info("Uploading the data files.");
        uploadFile(table, createCsvInputStream(members, csvColumnMappings), fileName, "text/csv");
        log.info("Done.");

        def tableList = engine.tables().list()
        println tableList
    }


    private List<Project> findAllProjects() {
        log.info("Creating list of all projects readable by the current user ...")
        ProjectsListResponse projectList = engine.projects().list().execute()
        List<Project> allProjects =  projectList.getProjects()
        
        if (allProjects.isEmpty()) {
            String msg = "No projects were found that are readable by the current user."
            throw new RuntimeException(msg)
        }
        else {
            log.info("All projects readable by the current user: ")
            projectList.getProjects().each { Project project ->
                log.info("\tname='" + project.getName() + "' id='" + project.getId() + "'")
            }
        }
        
        return allProjects
    }


    private void deleteAll() {
        engine.tables().delete("ID")
    }



    private InputStream createCsvInputStream(
            final List<MemberBean> members,
            final Map<String, String> propertyNames) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()

        List<Map<String, Object>> membersMaps = []
        members.each { membersMaps << it.toMap(propertyNames) }

        // Store the CSV into the stream
        new Csv(propertyNames.values()).store(membersMaps, byteArrayOutputStream)

        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray())
    }



    /** Creates an empty table in your maps engine account. */
    private Table createTable(
            final String projectId, 
            final List<String> fileNames,
            final String tableName,
            final String tableDescription,
            final String schemaPrimaryKey,
            final List<String> tags) throws IOException {
        // Note that we need a com.google.api.services.mapsengine.model.File, not a java.io.File
        List<com.google.api.services.mapsengine.model.File> files = new ArrayList<>(fileNames.size());
        fileNames.each { files <<  new com.google.api.services.mapsengine.model.File().setFilename(it) }

        // Build the table, including the minimal schema
        Table newTable = new Table()
                .setName(tableName)
                .setDescription(tableDescription)
                .setSchema(new Schema().setPrimaryKey(schemaPrimaryKey))
                .setProjectId(projectId)
//                .setFiles(files)
                .setTags(tags);

        return engine.tables().upload(newTable).execute();
    }


    /** Uploads the file data to the empty table. */
    private void uploadFile(Table table, InputStream inputStream, String fileName, String contentType) throws IOException {
        // Upload!
        engine.tables().files().insert(
                table.getId(), 
                fileName, 
                new InputStreamContent(contentType, inputStream)).execute();
    }
}
