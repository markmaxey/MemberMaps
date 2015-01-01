package org.greenvilleoaks

import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import groovy.util.logging.Log4j
import org.apache.log4j.FileAppender
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.log4j.PatternLayout
import org.greenvilleoaks.beans.MemberBean
import org.greenvilleoaks.config.Config
import org.greenvilleoaks.map.Workflow
import org.greenvilleoaks.storage.FileUtils
import org.greenvilleoaks.storage.Spreadsheet
import org.greenvilleoaks.view.*

/**
 * This is the "main" class containing all the control logic.
 */
@Log4j
class MemberMap {
    /**
     * The high level workflow logic
     * @param argv
     */
    public static void main(final String[] argv) {
        Config config = loadConfig(argv)
        
        addFileLogAppender(config.memberStatsDirName)
        
        log.info("Generating a members map and spreadsheet ...")
        log.info(config.toString())

        List<MemberBean> members = new Members(config).createMembers()

        Map<String, View> views = new Views().createAndStoreViews(config, members)


        /*
        new Workflow(
                members,
                views,
                new NetHttpTransport(),
                new GsonFactory(),
                config.membersCsvColumnMappings,
                config.centralPointName,
                new File(config.google.jsonKeyFileName)).run(config.google.projectId)
        */
    }


    private static void addFileLogAppender(final String dirName) {
        String fileName = dirName + "\\" + "MemberMap.log"

        if (!FileUtils.createParentDirs(fileName))
            throw new RuntimeException("Can't create the parent directories for '$fileName'")
        
        FileAppender fileAppender = new FileAppender()
        fileAppender.setName("FileLogger")
        fileAppender.setFile(fileName)
        fileAppender.setLayout(new PatternLayout("%d %-5p %c{1} - %m%n"))
        fileAppender.setThreshold(Level.DEBUG)
        fileAppender.setAppend(true)
        fileAppender.activateOptions()

        Logger.getRootLogger().addAppender(fileAppender)
    }


    private static Config loadConfig(final String[] argv) {
        Config config
        if (argv.length == 0) {
            config = new Config()
        }
        else {
            File configFile = new File(argv[0])
            if (configFile.exists()) {
                config = new ConfigSlurper().parse(new URL("file:///" + argv[0]))
            }
            else {
                config = new Config()
            }
        }
        
        return config.init()
    }
}
