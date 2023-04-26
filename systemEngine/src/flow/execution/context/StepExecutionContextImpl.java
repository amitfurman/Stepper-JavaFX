package flow.execution.context;

import datadefinition.api.DataDefinitions;
import java.util.*;

public class StepExecutionContextImpl implements StepExecutionContext {

    private final Map<String, Object> dataValues;
    private final Map<String, DataDefinitions> name2DD;
    private final Map<String, String> outputName2alias;
    private final Map<String, String> stepName2alias;
    private final List<String> logsList;
    private final List<String> summaryLinesList;


    public StepExecutionContextImpl(Map<String, DataDefinitions> originalDDMap, Map<String,String> originalOutputAliasMap, Map<String, String> originalStepName2alias) {
        dataValues = new HashMap<>();
        name2DD = new HashMap<>(originalDDMap);
        outputName2alias = new HashMap<>(originalOutputAliasMap);
        stepName2alias = new HashMap<>(originalStepName2alias);
        logsList = new LinkedList<>();
        summaryLinesList = new LinkedList<>(); //need list?
    }

    ///////////overview the exception
    @Override
    public <T> T getDataValue(String dataName, Class<T> expectedDataType) {

        //return the data definition from the name
        DataDefinitions theExpectedDataDefinition = name2DD.get(dataName);

        if (expectedDataType.isAssignableFrom(theExpectedDataDefinition.getType())) {
            Object aValue = dataValues.get(dataName);
            if (aValue != null) {
                // If the value exists, cast and return it
                return expectedDataType.cast(aValue);
            } else {
                // If the value does not exist, throw an exception or return a default value as needed
                // For example, throw an exception:
                throw new NullPointerException("Data value for " + dataName + " is null.");
            }
        }
        else {
            // error handling of some sort...
            // If the data definition is not found or expected data type is not compatible, throw an exception or handle the error as needed
            // For example, throw an exception:
            throw new IllegalArgumentException("Data definition for " + dataName + " is not found or expected data type is not compatible.");
        }

    }

    @Override
    public boolean storeDataValue(String dataName, Object value) {
        // assuming that from the data name we can get to its data definition
        DataDefinitions theData = name2DD.get(dataName);

        // we have the DD type so we can make sure that its from the same type
        if (theData.getType().isAssignableFrom(value.getClass())) {
            String stepName,outputAlias = null;
            for (String key : outputName2alias.keySet()) {
                if (key.endsWith("." + dataName)) {
                    stepName = key.substring(0, key.lastIndexOf("."));
                    outputAlias = outputName2alias.get(key);
                    break;
                }
            }
            dataValues.put(outputAlias, value);
        } else {
            // error handling of some sort...
        }

        return false;
    }

    @Override
    public void storeLogLine(String logLine) {
        logsList.add(logLine);
    }

    @Override
    public void setSummaryLine(String summaryLine) {

    }


}