package org.greenvilleoaks.map

import com.google.api.services.mapsengine.MapsEngine
import com.google.api.services.mapsengine.model.Project
import com.google.api.services.mapsengine.model.ProjectsListResponse
import groovy.transform.Immutable
import groovy.util.logging.Log4j

@Immutable
@Log4j
class ProjectAdapter {
    private MapsEngine engine

    public List<Project> findAllProjects() {
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
                log.info("\tname = '${project.getName()}' id = ${project.getId()}")
            }
        }

        return allProjects
    }
}
