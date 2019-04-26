package watchtower.service.util

import watchtower.service.domain.Task
import watchtower.service.domain.Workflow
import watchtower.service.pogo.enums.WorkflowStatus

import java.time.Instant

class DomainCreator {

    static Workflow createWorkflow(Map fields = [:]) {
        fields.runId = fields.runId?: "35cce421-4712-4da5-856b-6557635e54${generateUniqueNamePart()}d"
        fields.runName = fields.runName ?: "astonishing_majorana${generateUniqueNamePart()}"
        fields.currentStatus = fields.currentStatus ?: WorkflowStatus.SUCCEEDED
        fields.submitTime = fields.submitTime ?: Instant.now()
        fields.startTime = fields.startTime ?: fields.submitTime

        createInstance(Workflow.class, fields)
    }

    static void cleanupDatabase() {
        Task.deleteAll(Task.list())
        Workflow.deleteAll(Workflow.list())
    }

    /**
     * Creates and persists an instance of a class in the database given their creation params
     * @param clazz the class to create the instance of
     * @param params the params to create the instance, it can contain lists too
     * @param persist if the instance to save is persisted in the database (true by default)
     * @param validate if the instance to save needs to be validated (true by default)
     * @return the persisted instance
     */
    private static def createInstance(Class clazz, Map params, Boolean validate = true) {
        Map regularParams = [:]
        Map listParams = [:]
        extractListsFromMap(params, regularParams, listParams)

        def instance = clazz.newInstance(regularParams)

        listParams.each { String k, List v ->
            addAllInstances(instance, k, v)
        }
        instance.save(validate: validate, failOnError: true)
    }

    /**
     * Associates the objects contained in the collection to the corresponding property of the given instance
     * @param instance the instance to populate its collection
     * @param collectionName the name of the collection property
     * @param collection the collection which contains the instances to add
     */
    private static void addAllInstances(def instance, String collectionName, List collection) {
        collection?.each {
            instance."addTo${collectionName.capitalize()}"(it)
        }
    }

    /**
     * Separates the entries whose value is a regular instance from the entries whose value is a list instance of a map
     * @param origin the map with all the instances
     * @param noLists the map to populate with entries whose value is a regular instance
     * @param lists noLists the map to populate with entries whose value is a list instance
     */
    private static void extractListsFromMap(Map origin, Map noLists, Map lists) {
        origin?.each { k, v ->
            if (v instanceof List) {
                lists."${k}" = v
            } else {
                noLists."${k}" = v
            }
        }
    }

    /**
     * Generate a unique string in order to make a name distinguishable
     * @return a random string with a low probability of collision with other generated strings
     */
    private static String generateUniqueNamePart() {
        "${UniqueIdentifierGenerator.generateUniqueId()}"
    }
}

class UniqueIdentifierGenerator {

    private static long uniqueIdentifier = 0

    /**
     * Generates an unique numeric id
     * @return an unique numeric id
     */
    static long generateUniqueId() {
        uniqueIdentifier++
    }
}