package com.ls.mobile.geotool.workflow;

/**
 *
 */
public class PointWorkflowResolver implements WorkflowInterface, PointStatusInterface {

    public PointWorkflowResolver() {
    }

    /**
     * Status of a new POINT entered manually by the user and
     * validated on screen
     *
     * @return POINT_STATUS_REGISTERED corresponding string
     */
    public String getNewEntityWrkflwStat() {
        return POINT_STATUS_REGISTERED;
    }

    /**
     * Last Status an entity could have
     *
     * @return KEY asociated to status string
     */
    @Override @Deprecated
    public String getWrkflwStatForEnd() {
        return null;
    }

    /**
     * Return next logic workflow status.
     * Use with care, it doesm't perform any validation of repeated operations over any specific
     * status
     *
     * @param actualWkflwStat
     * @return
     */
    public String getNextWorkflowStatus(String actualWkflwStat) {
        String result = "";

        if (actualWkflwStat.equals(POINT_STATUS_PENDING)) {
            result = POINT_STATUS_REGISTERED;
        } else if (actualWkflwStat.equals(POINT_STATUS_ANOMALY)) {
            result = POINT_STATUS_EDITED;
        } else if (actualWkflwStat.equals(POINT_STATUS_REGISTERED)) {
            result = POINT_STATUS_ANOMALY;
        } else if (actualWkflwStat.equals(POINT_STATUS_EDITED)) {
            result = POINT_STATUS_ANOMALY;
        }
        return result;
    }

    /**
     * Returns the next logic workflow status of a POINT after performing an
     * update operation based on actual state of such POINT
     * Log4G business Rules
     *
     * @param actualWkflwStat
     * @return
     */
    public String getWrkflwStatusAfterUpdate(String actualWkflwStat) {
        String result = "";

        if (actualWkflwStat.equals(POINT_STATUS_PENDING)) {
            result = POINT_STATUS_REGISTERED;
        } else if (actualWkflwStat.equals(POINT_STATUS_ANOMALY)) {
            result = POINT_STATUS_EDITED;
        } else if (actualWkflwStat.equals(POINT_STATUS_REGISTERED)) {
            result = POINT_STATUS_REGISTERED;
        } else if (actualWkflwStat.equals(POINT_STATUS_EDITED)) {
            result = POINT_STATUS_EDITED;
        }
        return result;
    }


}
