package org.idmunit.teststep;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ddsteps.step.TestStep;
import org.idmunit.IdMUnitException;
import org.idmunit.connector.Connector;

public class TestStepExecute implements TestStep {
    private final static String STR_RANGE_DELIMITER = "-";
    private final static String OP_REPEAT_RANGE = "RepeatOpRange";

    static Log LOG = LogFactory.getLog(TestStepExecute.class);
    private Connector connector;
    private Map<String, Collection<String>> data;
    private Map operationData;
    private String operation;
    
/**
 * Instantiate and initialize the transaction object
 */
    public TestStepExecute(Map operationalDataMap, Connector idmUnitConnector, String operation, Map<String, Collection<String>> data) {
        this.connector = idmUnitConnector;
        this.operation = operation;
        this.data = data;
        this.operationData = operationalDataMap;
        
    }

    /**
     * Execute the test step.  If the data contains a RepeatOpRange, the test step will be
     * repeated for each iteration through the specified range (i.e. ten through one hundred specified as 10-100)
     */
    public void runStep() throws IdMUnitException {
        //Determine whether or not a repeat range has been specified
        String repeatRange = (String)operationData.get(OP_REPEAT_RANGE);
        boolean rangeInputDetected = (repeatRange!=null && repeatRange.length()>0);
        if(!rangeInputDetected) {
            //Process a single non-repeated transaction
            connector.execute(operation, data);
        } else {
            //Repeat operation range was detected, for each iteration perform the following:
            //  1. Replace range counter for each data field
            //  2. Execute the test step
            //  3. If an error occurs add it to a list
            //  4. If there are any errors in the list after completion, fail the test with a report of broken iterations
            int rangeStart = Integer.parseInt(StringUtils.substringBefore(repeatRange, STR_RANGE_DELIMITER));
            int rangeEnd = Integer.parseInt(StringUtils.substringAfter(repeatRange, STR_RANGE_DELIMITER));
            LOG.info("### Repeat Operation Range detected: Start:" + rangeStart + " End: " + rangeEnd);
            for(int ctr=rangeStart;ctr<=rangeEnd;++ctr) {
                LOG.info("### Execute repeated operation iteration: " + ctr);
                //  1. Replace range counter for each data field //TODO: Leverage Data Injectors for this purpose if possible
                //CommonUtil.interpolateCounter(m_data, ctr); //TODO: must refactor  TestStepAdd... to leverage Attributes rather than DataRowBeans, who's members are immutable
                //Process the current repeated transaction
                //m_connection.addObject(m_data);
            }
        }
    }
}
