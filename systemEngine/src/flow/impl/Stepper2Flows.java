package flow.impl;

import datadefinition.api.DataDefinitions;
import dto.DTOAllStepperFlows;
import dto.DTOFlowDefinition;
import dto.DTOFlowDefinitionImpl;
import flow.api.CustomMapping;
import flow.mapping.FlowAutomaticMapping;
import flow.mapping.FlowCustomMapping;
import steps.StepDefinitionRegistry;
import steps.api.DataDefinitionDeclaration;
import flow.api.FlowDefinition;
import flow.api.FlowDefinitionImpl;
import flow.api.StepUsageDeclarationImpl;
import flow.execution.runner.FlowExecutor;
import jaxb.schema.generated.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Stepper2Flows {
    private LinkedList<DTOFlowDefinitionImpl> allFlows;
    private DTOAllStepperFlows allStepperFlows;

    public Stepper2Flows(STStepper stepper) {
        allFlows = new LinkedList<>();
        int numberOfFlows = stepper.getSTFlows().getSTFlow().size();
        FlowDefinition flow;
        //each flow
        for (int i = 0; i < numberOfFlows; i++) {
            STFlow currFlow = stepper.getSTFlows().getSTFlow().get(i);
            flow = new FlowDefinitionImpl(currFlow.getName(), currFlow.getSTFlowDescription());

            //each step
            //add steps to flow
            for (STStepInFlow step : currFlow.getSTStepsInFlow().getSTStepInFlow()) {
                StepDefinitionRegistry myEnum = StepDefinitionRegistry.valueOf(step.getName().toUpperCase().replace(" ", "_"));
                if (step.getAlias() != null && step.isContinueIfFailing()) {
                    flow.addStepToFlow(new StepUsageDeclarationImpl(myEnum.getStepDefinition(), step.isContinueIfFailing(), step.getAlias()));
                    flow.addToAlias2StepNameMap(step.getAlias(), step.getName());
                } else if (step.getAlias() != null) {
                    flow.addStepToFlow(new StepUsageDeclarationImpl(myEnum.getStepDefinition(), step.getAlias()));
                    flow.addToAlias2StepNameMap(step.getAlias(), step.getName());
                } else {
                    flow.addStepToFlow(new StepUsageDeclarationImpl(myEnum.getStepDefinition()));
                    flow.addToAlias2StepNameMap(step.getName(), step.getName());
                }

                List<DataDefinitionDeclaration> stepInputs = myEnum.getStepDefinition().inputs();
                for (DataDefinitionDeclaration input : stepInputs) {
                    flow.addToName2DDMap(input.getName(), input.dataDefinition());
                    if (step.getAlias() != null) {
                        flow.addToInputName2AliasMap(step.getAlias(), input.getName(), input.getName());
                    } else {
                        flow.addToInputName2AliasMap(step.getName(), input.getName(), input.getName());
                    }
                }
                List<DataDefinitionDeclaration> stepOutputs = myEnum.getStepDefinition().outputs();
                for (DataDefinitionDeclaration output : stepOutputs) {
                    flow.addToName2DDMap(output.getName(), output.dataDefinition());
                    if (step.getAlias() != null) {
                        flow.addToOutputName2AliasMap(step.getAlias(), output.getName(), output.getName());
                    } else {
                        flow.addToOutputName2AliasMap(step.getName(), output.getName(), output.getName());
                    }
                }
            }

            //check if flow is read only
            flow.setFlowReadOnly();

            //FlowLevelAliasing
            if(currFlow.getSTFlowLevelAliasing()!=null) {
                for (STFlowLevelAlias flowLevelAlias : currFlow.getSTFlowLevelAliasing().getSTFlowLevelAlias()) {
                    if (flow.stepExist(flowLevelAlias.getStep()) && flow.dataExist(flowLevelAlias.getStep(), flowLevelAlias.getSourceDataName())) {
                        DataDefinitions data = flow.getDDFromMap(flowLevelAlias.getSourceDataName());
                        flow.addToName2DDMap(flowLevelAlias.getAlias(), data);
                        if ((flow.getInputName2aliasMap().get(flowLevelAlias.getStep() + "." + flowLevelAlias.getSourceDataName())) != null) {
                            flow.addToInputName2AliasMap(flowLevelAlias.getStep(), flowLevelAlias.getSourceDataName(), flowLevelAlias.getAlias());
                        } else {
                            flow.addToOutputName2AliasMap(flowLevelAlias.getStep(), flowLevelAlias.getSourceDataName(), flowLevelAlias.getAlias());
                        }
                    }
                }
            }

            //add output to flow
            String outputsName = currFlow.getSTFlowOutput();
            List<String> names = Arrays.stream(outputsName.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
            flow.getFlowFormalOutputs().addAll(names);
            flow.validateIfOutputsHaveSameName();
            flow.flowOutputsIsNotExists();


            /////Custom Mapping
            if(currFlow.getSTCustomMappings() != null) {
                for (STCustomMapping customMapping : currFlow.getSTCustomMappings().getSTCustomMapping()) {
                    flow.addToCustomMapping(new CustomMapping(customMapping.getSourceStep(), customMapping.getSourceData(), customMapping.getTargetStep(), customMapping.getTargetData()));
                }
            }

            FlowAutomaticMapping automaticMapping = new FlowAutomaticMapping(flow);
            FlowCustomMapping customMapping = new FlowCustomMapping(flow);

            flow.initMandatoryInputsList();
            flow.freeInputsWithSameNameAndDifferentType();
            flow.mandatoryInputsIsUserFriendly();


            DTOFlowDefinitionImpl DTOflow = new DTOFlowDefinitionImpl(flow);
            allFlows.add(DTOflow);


        }

        allStepperFlows = new DTOAllStepperFlows(allFlows);
    }

    public LinkedList<DTOFlowDefinitionImpl> getAllFlows() {
        return allFlows;
    }

    public DTOAllStepperFlows getAllStepperFlows() {
        return allStepperFlows;
    }
}