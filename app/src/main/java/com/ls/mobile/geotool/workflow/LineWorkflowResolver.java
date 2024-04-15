package com.ls.mobile.geotool.workflow;

/**
 * @deprecated
 */
public class LineWorkflowResolver implements WorkflowInterface, LineStatusInterface {


    public LineWorkflowResolver() {
    }

    /**
     * Status of a new LINE entered manually by the user and
     * validated on screen
     *
     * @return LINE_STATUS_INCOMPLETE corresponding string
     */
    @Override
    public String getNewEntityWrkflwStat() {
        return LINE_STATUS_INCOMPLETE;
    }

    /**
     * Last Status an entity could have
     *
     * @return KEY asociated to status string
     */
    @Override
    public String getWrkflwStatForEnd() {
        return LINE_STATUS_CLOSED;
    }

    /**
     * Return next logic workflow status.
     * Use with care, it doesm't perform any validation of repeated
     * operations over any specific status
     *
     * @param actualWkflwStat
     * @return
     */
    @Override
    @Deprecated
    public String getNextWorkflowStatus(String actualWkflwStat) {
        return null;
    }

    /**
     * Returns the next logic workflow status of the LINE after performing an
     * update operation based on actual state of such POINT
     * Log4G business Rules
     *
     * @param actualWkflwStat
     * @return
     */
    @Override
    @Deprecated
    public String getWrkflwStatusAfterUpdate(String actualWkflwStat) {
        return null;
    }

}