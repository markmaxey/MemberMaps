package org.greenvilleoaks

import com.google.api.client.http.HttpTransport
import org.apache.log4j.FileAppender
import org.apache.log4j.Level
import org.apache.log4j.PatternLayout
import org.greenvilleoaks.storage.FileUtils

import java.util.logging.ConsoleHandler
import java.util.logging.Logger

/** Setup the loggers and handlers */
class LogSetup {
    public static void addFileLogAppender(final String dirName) {
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

        org.apache.log4j.Logger.getRootLogger().addAppender(fileAppender)
    }




    public static void enableHttpLogging() {
        ConsoleHandler consoleHandler = new ConsoleHandler()
        consoleHandler.setLevel(java.util.logging.Level.FINEST)
        Logger.getLogger(HttpTransport.class.getName()).setLevel(java.util.logging.Level.FINEST)
        Logger.getLogger(HttpTransport.class.getName()).addHandler(consoleHandler)
    }
}
