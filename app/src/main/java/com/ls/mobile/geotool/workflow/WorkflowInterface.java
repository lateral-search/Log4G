package com.ls.mobile.geotool.workflow;

interface WorkflowInterface {

    /**
     * Status of a new entity
     *
     * @return KEY asociated to status string
     */
    String getNewEntityWrkflwStat();

    /**
     * Last Status an entity could have
     *
     * @return KEY asociated to status string
     */
    String getWrkflwStatForEnd();


    /**
     * Return next logic workflow status.
     * Use with care, it doesm't perform any validation of repeated
     * operations over any specific status
     *
     * @param actualWkflwStat
     * @return
     */
    String getNextWorkflowStatus(String actualWkflwStat);
    /**
     * Returns the next logic workflow status of the entity after
     * performing an update operation based on actual state of such
     * entitybusiness Rules
     *
     * @param actualWkflwStat
     * @return
     */
    String getWrkflwStatusAfterUpdate(String actualWkflwStat);


}
