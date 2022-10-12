package com.aktimetrix.service.planner;

import com.aktimetrix.core.impl.DefaultContext;
import com.aktimetrix.core.model.MeasurementInstance;
import com.aktimetrix.core.model.ProcessInstance;
import com.aktimetrix.core.model.ProcessPlanInstance;
import com.aktimetrix.core.model.StepInstance;
import com.aktimetrix.core.repository.MeasurementInstanceRepository;
import com.aktimetrix.core.repository.ProcessInstanceRepository;
import com.aktimetrix.core.repository.ProcessPlanRepository;
import com.aktimetrix.core.service.MeasurementInstanceService;
import com.aktimetrix.core.service.ProcessInstanceService;
import com.aktimetrix.service.planner.api.Planner;
import com.aktimetrix.service.planner.processor.CiQProcessCreateProcessor;
import com.aktimetrix.service.planner.service.RouteMapService;
import com.aktimetrix.service.planner.transferobjects.*;
import com.google.common.collect.ArrayListMultimap;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
@Slf4j
public class CiQProcessCreateProcessorTest {


    @SpyBean
    private RouteMapService routeMapServiceMock;


    @Test
    public void contextLoads() {
    }

    @Order(1)
    @Test
    public void create_original_plan_success(@Autowired CiQProcessCreateProcessor processor) {
        doReturn("C").when(routeMapServiceMock).getShipmentIndicator(Mockito.any());
        doReturn(true).when(routeMapServiceMock).isAllMeasurementsCaptured(Mockito.any(), Mockito.any(), Mockito.any());
        DefaultContext context = new DefaultContext();
        context.setProcessInstance(getProcessInstance("DXB", "BOM", true));
        context.setTenant("XX");
        processor.process(context);

    }

    @Order(2)
    @Test
    public void update_original_plan_on_new_process_instance(@Autowired CiQProcessCreateProcessor processor) {
        doReturn("C").when(routeMapServiceMock).getShipmentIndicator(Mockito.any());
        doReturn(true).when(routeMapServiceMock).isAllMeasurementsCaptured(Mockito.any(), Mockito.any(), Mockito.any());
        DefaultContext context = new DefaultContext();
        context.setProcessInstance(getUpdatedProcessInstance("DXB", "BOM"));
        context.setTenant("XX");
        processor.process(context);
    }

    @DisplayName("given origin plan exits and rmp sent" +
            " when change are in the process instance's origin " +
            " version number increases and no change in the plan status")
    @Order(3)
    @Test
    public void update_original_plan_on_new_process_instance_with_difference_in_origin(@Autowired CiQProcessCreateProcessor processor, @Autowired Planner planner, @Autowired ProcessInstanceService processInstanceService) {

        //given
        ProcessInstance processInstance = processInstanceService.saveProcessInstance(getProcessInstance("DXB", "BOM", true));
        planner.createPlan("XX", getPlan("Baseline", processInstance.getId()));
        doReturn("C").when(routeMapServiceMock).getShipmentIndicator(Mockito.any());
        doReturn(true).when(routeMapServiceMock).isAllMeasurementsCaptured(Mockito.any(), Mockito.any(), Mockito.any());
        DefaultContext context = new DefaultContext();
        context.setProcessInstance(getUpdatedProcessInstance("SHJ", "BOM"));
        context.setTenant("XX");
        // when
        processor.process(context);

        // then
    }

    @DisplayName("given Baseline plan exits and rmp sent" +
            " when change are in the process instance's origin " +
            " version number increases and no change in the plan status")
    @Order(3)
    @Test
    public void update_baseline_plan_on_new_process_instance_with_difference_in_origin(@Autowired CiQProcessCreateProcessor processor, @Autowired Planner planner, @Autowired ProcessInstanceService processInstanceService) {

        //given
        ProcessInstance processInstance = processInstanceService.saveProcessInstance(getProcessInstance("DXB", "BOM", true));
        planner.createPlan("XX", getPlan("Baseline", processInstance.getId()));
        doReturn("C").when(routeMapServiceMock).getShipmentIndicator(Mockito.any());
        doReturn(true).when(routeMapServiceMock).isAllMeasurementsCaptured(Mockito.any(), Mockito.any(), Mockito.any());
        DefaultContext context = new DefaultContext();
        context.setProcessInstance(getUpdatedProcessInstance("SHJ", "BOM"));
        context.setTenant("XX");
        // when
        processor.process(context);

        // then
    }

    @DisplayName("when baseline plan exists " +
            " and new process instance is received with change in origin station and no dep is received at origin station")
    @Test
    public void baseline_plan_with_difference_in_origin_and_dep_not_received_at_origin(@Autowired CiQProcessCreateProcessor processor,
                                                                                       @Autowired Planner planner,
                                                                                       @Autowired ProcessInstanceService processInstanceService) {

        //given
        ProcessInstance processInstance = processInstanceService.saveProcessInstance(getProcessInstance("DXB", "BOM", true));
        planner.createPlan("XX", getPlan("Baseline", processInstance.getId()));
//        Mockito.when(routeMapServiceMock.getShipmentIndicator(processInstance)).thenReturn("C");
        doReturn("C").when(routeMapServiceMock).getShipmentIndicator(Mockito.any());
//        Mockito.when(routeMapServiceMock.isDepStepCompleted(Mockito.anyString(), Mockito.any())).thenReturn(false);
        doReturn(true).when(routeMapServiceMock).isAllMeasurementsCaptured(Mockito.any(), Mockito.any(), Mockito.any());
        DefaultContext context = new DefaultContext();
        context.setProcessInstance(getUpdatedProcessInstance("SHJ", "BOM"));
        context.setTenant("XX");
        // when
        processor.process(context);

        // then
    }

    @DisplayName("when baseline plan exists " +
            " and new process instance is received with change in origin station and dep is received at origin station")
    @Test
    public void baseline_plan_with_difference_in_origin_and_dep_received_at_origin(@Autowired CiQProcessCreateProcessor processor, @Autowired Planner planner, @Autowired ProcessInstanceService processInstanceService) {

        //given
        ProcessInstance processInstance = processInstanceService.saveProcessInstance(getProcessInstance("DXB", "BOM", true));
        planner.createPlan("XX", getPlan("Baseline", processInstance.getId()));
        doReturn("C").when(routeMapServiceMock).getShipmentIndicator(Mockito.any());
//        Mockito.when(routeMapServiceMock.isDepStepCompleted(Mockito.anyString(), Mockito.any())).thenReturn(true);
        doReturn(true).when(routeMapServiceMock).isAllMeasurementsCaptured(Mockito.any(), Mockito.any(), Mockito.any());
        DefaultContext context = new DefaultContext();
        context.setProcessInstance(getUpdatedProcessInstance("SHJ", "BOM"));
        context.setTenant("XX");
        // when
        processor.process(context);

        // then
    }


    @DisplayName("when baseline plan exists " +
            " and new process instance is received with no change in origin or destination station or forwarder code")
    @Test
    public void baseline_plan_with_no_difference_in_origin_or_destination_or_forwarder(@Autowired CiQProcessCreateProcessor processor, @Autowired Planner planner, @Autowired ProcessInstanceService processInstanceService) {

        //given
        ProcessInstance processInstance = processInstanceService.saveProcessInstance(getProcessInstance("DXB", "BOM", true));
        planner.createPlan("XX", getPlan("Baseline", processInstance.getId()));
        doReturn("C").when(routeMapServiceMock).getShipmentIndicator(Mockito.any());
//        Mockito.when(routeMapServiceMock.isDepStepCompleted(Mockito.anyString(), Mockito.any())).thenReturn(true);
        doReturn(true).when(routeMapServiceMock).isAllMeasurementsCaptured(Mockito.any(), Mockito.any(), Mockito.any());
        DefaultContext context = new DefaultContext();
        context.setProcessInstance(getUpdatedProcessInstance("DXB", "BOM"));
        context.setTenant("XX");
        // when
        processor.process(context);

        // then
    }

    @DisplayName("when baseline plan exists " +
            " and new process instance is received with no change in origin or destination station or forwarder code and change in the pieces")
    @Test
    public void baseline_plan_with_no_difference_in_origin_or_destination_or_forwarder_with_pcs_difference_dep_received(
            @Autowired MeasurementInstanceService measurementInstanceService,
            @Autowired CiQProcessCreateProcessor processor, @Autowired Planner planner, @Autowired ProcessInstanceService processInstanceService) {

        //given
        ProcessInstance processInstance = processInstanceService.saveProcessInstance(getProcessInstance("DXB", "BOM", true));
        measurementInstanceService.saveMeasurementInstance(getMeasurementInstance(processInstance, "A"));
        planner.createPlan("XX", getPlan("Baseline", processInstance.getId()));
        doReturn("C").when(routeMapServiceMock).getShipmentIndicator(Mockito.any());
//        Mockito.when(routeMapServiceMock.isDepStepCompleted(Mockito.anyString(), Mockito.any())).thenReturn(true);
        doReturn(true).when(routeMapServiceMock).isAllMeasurementsCaptured(Mockito.any(), Mockito.any(), Mockito.any());
        DefaultContext context = new DefaultContext();
        context.setProcessInstance(getUpdatedProcessInstance("DXB", "BOM", 20));
        context.setTenant("XX");
        // when
        processor.process(context);

        // then
    }

    @DisplayName("when baseline plan exists " +
            " and new process instance is received with no change in origin or destination station or forwarder code and change in the pieces")
    @Test
    public void baseline_plan_with_no_difference_in_origin_or_destination_or_forwarder_with_pcs_difference_dep_plantime_expired(
            @Autowired MeasurementInstanceService measurementInstanceService,
            @Autowired CiQProcessCreateProcessor processor, @Autowired Planner planner, @Autowired ProcessInstanceService processInstanceService) {

        //given
        ProcessInstance processInstance = processInstanceService.saveProcessInstance(getProcessInstance("DXB", "BOM", true));
        measurementInstanceService.saveMeasurementInstance(getMeasurementInstance(processInstance, "P"));
        planner.createPlan("XX", getPlan("Baseline", processInstance.getId()));
        doReturn("C").when(routeMapServiceMock).getShipmentIndicator(Mockito.any());
//        Mockito.when(routeMapServiceMock.isDepStepCompleted(Mockito.anyString(), Mockito.any())).thenReturn(true);
        doReturn(true).when(routeMapServiceMock).isAllMeasurementsCaptured(Mockito.any(), Mockito.any(), Mockito.any());
        DefaultContext context = new DefaultContext();
        context.setProcessInstance(getUpdatedProcessInstance("DXB", "BOM", 20));
        context.setTenant("XX");
        // when
        processor.process(context);

        // then
    }

    @DisplayName("when no plan exists and incomplete process instance is received then incomplete rm is created with status as Created")
    @Test
    public void no_plan_exists_for_incomplete_process_instance(@Autowired CiQProcessCreateProcessor processor,
                                                               @Autowired ProcessPlanRepository processPlanRepository) {
        doReturn("C").when(routeMapServiceMock).getShipmentIndicator(Mockito.any());

        DefaultContext context = new DefaultContext();
        ProcessInstance processInstance = getIncompleteProcessInstance("DXB", "BOM");
        processInstance.getSteps().addAll(getExportStepInstances(processInstance.getId(), "DXB", "MCT"));
        processInstance.getSteps().addAll(getTransitImportStepInstances(processInstance.getId(), "DXB", "MCT"));
        context.setProcessInstance(processInstance);
        context.setTenant("XX");
        processor.process(context);

        ProcessPlanInstance plan = processPlanRepository.findPlanByProcessInstanceIdAndCompleteIndicator("XX", processInstance.getId(), "N");
        assertThat(plan.getStatus()).isEqualTo("Created");

    }

    @DisplayName("when incomplete plan exists and a complete process instance is received then existing " +
            "incomplete plan status is updated to Original")
    @Test
    public void incomplete_plan_exists_for_complete_process_instance(@Autowired CiQProcessCreateProcessor processor,
                                                                     @Autowired ProcessPlanRepository processPlanRepository) {
        doReturn("C").when(routeMapServiceMock).getShipmentIndicator(Mockito.any());
        doReturn(true).when(routeMapServiceMock).isAllMeasurementsCaptured(Mockito.any(), Mockito.any(), Mockito.any());

        DefaultContext context = new DefaultContext();
        //given
        context = new DefaultContext();
        ProcessInstance processInstance = getIncompleteProcessInstance("DXB", "BOM");
        String processInstanceId = processInstance.getId();
        processInstance.getSteps().addAll(getExportStepInstances(processInstanceId, "DXB", "MCT"));
        processInstance.getSteps().addAll(getTransitImportStepInstances(processInstanceId, "DXB", "MCT"));
        context.setProcessInstance(processInstance);
        context.setTenant("XX");
        processor.process(context);

        ProcessPlanInstance plan = processPlanRepository.findPlanByProcessInstanceIdAndCompleteIndicator("XX", processInstanceId, "N");
        assertThat(plan.getStatus()).isEqualTo("Created");


        // when
        processInstance = getCompleteProcessInstance("DXB", "BOM");
        processInstance.setId(processInstanceId);
        processInstance.getSteps().addAll(getExportStepInstances(processInstanceId, "DXB", "MCT"));
        processInstance.getSteps().addAll(getTransitImportStepInstances(processInstanceId, "DXB", "MCT"));
        processInstance.getSteps().addAll(getTransitExportStepInstances(processInstanceId, "MCT", "BOM"));
        processInstance.getSteps().addAll(getImportStepInstances(processInstanceId, "MCT", "BOM"));
        context.setProcessInstance(processInstance);
        context.setTenant("XX");
        processor.process(context);


        //then
        plan = processPlanRepository.findPlanByProcessInstanceIdAndCompleteIndicator("XX", processInstanceId, "Y");
        assertThat(plan.getStatus()).isEqualTo("Original");

    }

    @DisplayName("when original plan exists and a incomplete process instance is received then  " +
            "another incomplete plan is created with 'Created' status")
    @Test
    public void original_plan_exists_for_incomplete_process_instance(@Autowired CiQProcessCreateProcessor processor,
                                                                     @Autowired ProcessPlanRepository processPlanRepository) {
        doReturn("C").when(routeMapServiceMock).getShipmentIndicator(Mockito.any());
        doReturn(true).when(routeMapServiceMock).isAllMeasurementsCaptured(Mockito.any(), Mockito.any(), Mockito.any());

        DefaultContext context = new DefaultContext();
        //given
        context = new DefaultContext();
        ProcessInstance processInstance = getIncompleteProcessInstance("DXB", "BOM");
        String processInstanceId = processInstance.getId();
        processInstance.getSteps().addAll(getExportStepInstances(processInstanceId, "DXB", "MCT"));
        processInstance.getSteps().addAll(getTransitImportStepInstances(processInstanceId, "DXB", "MCT"));
        context.setProcessInstance(processInstance);
        context.setTenant("XX");
        processor.process(context);

        ProcessPlanInstance plan = processPlanRepository.findPlanByProcessInstanceIdAndCompleteIndicator("XX", processInstanceId, "N");
        assertThat(plan.getStatus()).isEqualTo("Created");

        processInstance = getCompleteProcessInstance("DXB", "BOM");
        processInstance.setId(processInstanceId);
        processInstance.getSteps().addAll(getExportStepInstances(processInstanceId, "DXB", "MCT"));
        processInstance.getSteps().addAll(getTransitImportStepInstances(processInstanceId, "DXB", "MCT"));
        processInstance.getSteps().addAll(getTransitExportStepInstances(processInstanceId, "MCT", "BOM"));
        processInstance.getSteps().addAll(getImportStepInstances(processInstanceId, "MCT", "BOM"));
        context.setProcessInstance(processInstance);
        context.setTenant("XX");
        processor.process(context);

        plan = processPlanRepository.findPlanByProcessInstanceIdAndCompleteIndicator("XX", processInstanceId, "Y");
        assertThat(plan.getStatus()).isEqualTo("Original");


        context = new DefaultContext();
        processInstance = getIncompleteProcessInstance("SHJ", "BOM");
        processInstanceId = processInstance.getId();
        processInstance.getSteps().addAll(getExportStepInstances(processInstanceId, "SHJ", "MCT"));
        processInstance.getSteps().addAll(getTransitImportStepInstances(processInstanceId, "SHJ", "MCT"));
        context.setProcessInstance(processInstance);
        context.setTenant("XX");
        processor.process(context);

        plan = processPlanRepository.findPlanByProcessInstanceIdAndCompleteIndicator("XX", processInstanceId, "N");
        assertThat(plan.getStatus()).isEqualTo("Created");

    }

    @DisplayName("when original plan exists and a complete process instance is received then  ")
    @Test
    public void original_plan_exists_for_complete_process_instance(@Autowired CiQProcessCreateProcessor processor,
                                                                   @Autowired ProcessPlanRepository processPlanRepository) {
        doReturn("C").when(routeMapServiceMock).getShipmentIndicator(Mockito.any());
        doReturn(true).when(routeMapServiceMock).isAllMeasurementsCaptured(Mockito.any(), Mockito.any(), Mockito.any());

        DefaultContext context = new DefaultContext();
        //given
        context = new DefaultContext();
        ProcessInstance processInstance = getIncompleteProcessInstance("DXB", "BOM");
        String processInstanceId = processInstance.getId();
        processInstance.getSteps().addAll(getExportStepInstances(processInstanceId, "DXB", "MCT"));
        processInstance.getSteps().addAll(getTransitImportStepInstances(processInstanceId, "DXB", "MCT"));
        context.setProcessInstance(processInstance);
        context.setTenant("XX");
        processor.process(context);

        ProcessPlanInstance plan = processPlanRepository.findPlanByProcessInstanceIdAndCompleteIndicator("XX", processInstanceId, "N");
        assertThat(plan.getStatus()).isEqualTo("Created");

        processInstance = getCompleteProcessInstance("DXB", "BOM");
        processInstance.setId(processInstanceId);
        processInstance.getSteps().addAll(getExportStepInstances(processInstanceId, "DXB", "MCT"));
        processInstance.getSteps().addAll(getTransitImportStepInstances(processInstanceId, "DXB", "MCT"));
        processInstance.getSteps().addAll(getTransitExportStepInstances(processInstanceId, "MCT", "BOM"));
        processInstance.getSteps().addAll(getImportStepInstances(processInstanceId, "MCT", "BOM"));
        context.setProcessInstance(processInstance);
        context.setTenant("XX");
        processor.process(context);

        plan = processPlanRepository.findPlanByProcessInstanceIdAndCompleteIndicator("XX", processInstanceId, "Y");
        assertThat(plan.getStatus()).isEqualTo("Original");


        // when

        context = new DefaultContext();
        processInstance = getIncompleteProcessInstance("SHJ", "BOM");
        processInstanceId = processInstance.getId();
        processInstance.getSteps().addAll(getExportStepInstances(processInstanceId, "SHJ", "MCT"));
        processInstance.getSteps().addAll(getTransitImportStepInstances(processInstanceId, "SHJ", "MCT"));
        context.setProcessInstance(processInstance);
        context.setTenant("XX");
        processor.process(context);

        plan = processPlanRepository.findPlanByProcessInstanceIdAndCompleteIndicator("XX", processInstanceId, "N");
        assertThat(plan.getStatus()).isEqualTo("Created");

        processInstance = getCompleteProcessInstance("SHJ", "BOM");
        processInstance.setId(processInstanceId);
        processInstance.getSteps().addAll(getExportStepInstances(processInstanceId, "SHJ", "MCT"));
        processInstance.getSteps().addAll(getTransitImportStepInstances(processInstanceId, "SHJ", "MCT"));
        processInstance.getSteps().addAll(getTransitExportStepInstances(processInstanceId, "MCT", "BOM"));
        processInstance.getSteps().addAll(getImportStepInstances(processInstanceId, "MCT", "BOM"));
        context.setProcessInstance(processInstance);
        context.setTenant("XX");
        processor.process(context);

        // then
        plan = processPlanRepository.findPlanByProcessInstanceIdAndCompleteIndicator("XX", processInstanceId, "Y");
        assertThat(plan.getStatus()).isEqualTo(Constants.STATUS_ORIGINAL);
        assertThat(plan.getVersion()).isEqualTo(2);
        assertThat(plan.getPlanNumber()).isEqualTo(2);
        assertThat(plan.isRmpSent()).isTrue();

    }

    @DisplayName("when baseline plan exists and a complete process instance is received with change in the origin only," +
            " then cancel the existing plan and create new plan with status as 'Baseline'")
    @Test
    public void baseline_plan_exists_for_complete_process_instance(@Autowired CiQProcessCreateProcessor processor,
                                                                   @Autowired ProcessPlanRepository processPlanRepository) {
        doReturn("C").when(routeMapServiceMock).getShipmentIndicator(Mockito.any());
        doReturn(true).when(routeMapServiceMock).isAllMeasurementsCaptured(Mockito.any(), Mockito.any(), Mockito.any());

        DefaultContext context = new DefaultContext();
        //given
        context = new DefaultContext();
        ProcessInstance processInstance = getIncompleteProcessInstance("DXB", "BOM");
        String processInstanceId = processInstance.getId();
        processInstance.getSteps().addAll(getExportStepInstances(processInstanceId, "DXB", "MCT"));
        processInstance.getSteps().addAll(getTransitImportStepInstances(processInstanceId, "DXB", "MCT"));
        context.setProcessInstance(processInstance);
        context.setTenant("XX");
        processor.process(context);

        ProcessPlanInstance plan = processPlanRepository.findPlanByProcessInstanceIdAndCompleteIndicator("XX", processInstanceId, "N");
        assertThat(plan.getStatus()).isEqualTo("Created");

        processInstance = getCompleteProcessInstance("DXB", "BOM");
        processInstance.setId(processInstanceId);
        processInstance.getSteps().addAll(getExportStepInstances(processInstanceId, "DXB", "MCT"));
        processInstance.getSteps().addAll(getTransitImportStepInstances(processInstanceId, "DXB", "MCT"));
        processInstance.getSteps().addAll(getTransitExportStepInstances(processInstanceId, "MCT", "BOM"));
        processInstance.getSteps().addAll(getImportStepInstances(processInstanceId, "MCT", "BOM"));
        context.setProcessInstance(processInstance);
        context.setTenant("XX");
        processor.process(context);

        plan = processPlanRepository.findPlanByProcessInstanceIdAndCompleteIndicator("XX", processInstanceId, "Y");
        assertThat(plan.getStatus()).isEqualTo("Original");


        plan.setStatus(Constants.STATUS_BASELINE);
        processPlanRepository.save(plan);

        // when

        context = new DefaultContext();
        processInstance = getIncompleteProcessInstance("SHJ", "BOM");
        processInstanceId = processInstance.getId();
        processInstance.getSteps().addAll(getExportStepInstances(processInstanceId, "SHJ", "MCT"));
        processInstance.getSteps().addAll(getTransitImportStepInstances(processInstanceId, "SHJ", "MCT"));
        context.setProcessInstance(processInstance);
        context.setTenant("XX");
        processor.process(context);

        plan = processPlanRepository.findPlanByProcessInstanceIdAndCompleteIndicator("XX", processInstanceId, "N");
        assertThat(plan.getStatus()).isEqualTo("Created");

        processInstance = getCompleteProcessInstance("SHJ", "BOM");
        processInstance.setId(processInstanceId);
        processInstance.getSteps().addAll(getExportStepInstances(processInstanceId, "SHJ", "MCT"));
        processInstance.getSteps().addAll(getTransitImportStepInstances(processInstanceId, "SHJ", "MCT"));
        processInstance.getSteps().addAll(getTransitExportStepInstances(processInstanceId, "MCT", "BOM"));
        processInstance.getSteps().addAll(getImportStepInstances(processInstanceId, "MCT", "BOM"));
        context.setProcessInstance(processInstance);
        context.setTenant("XX");
        processor.process(context);

        // then
        plan = processPlanRepository.findPlanByProcessInstanceIdAndCompleteIndicator("XX", processInstanceId, "Y");
        assertThat(plan.getStatus()).isEqualTo(Constants.STATUS_BASELINE);
        assertThat(plan.getVersion()).isEqualTo(2);
        assertThat(plan.getPlanNumber()).isEqualTo(2);
        assertThat(plan.isRmpSent()).isTrue();

    }


    @DisplayName("when baseline plan exists and a complete process instance is received with no change in the origin ," +
            " destination and forwarder, but change in the quantity" +
            " and 'DEP' step is completed ")
    @Test
    public void baseline_plan_exists_for_complete_process_instance_with_quantity_different(@Autowired CiQProcessCreateProcessor processor,
                                                                                           @Autowired ProcessPlanRepository processPlanRepository) {
        doReturn("C").when(routeMapServiceMock).getShipmentIndicator(Mockito.any());
        doReturn(true).when(routeMapServiceMock).isAllMeasurementsCaptured(Mockito.any(), Mockito.any(), Mockito.any());

        DefaultContext context = new DefaultContext();
        //given
        context = new DefaultContext();
        ProcessInstance processInstance = getIncompleteProcessInstance("DXB", "BOM");
        String processInstanceId = processInstance.getId();
        processInstance.getSteps().addAll(getExportStepInstances(processInstanceId, "DXB", "MCT"));
        processInstance.getSteps().addAll(getTransitImportStepInstances(processInstanceId, "DXB", "MCT"));
        context.setProcessInstance(processInstance);
        context.setTenant("XX");
        processor.process(context);

        ProcessPlanInstance plan = processPlanRepository.findPlanByProcessInstanceIdAndCompleteIndicator("XX", processInstanceId, "N");
        assertThat(plan.getStatus()).isEqualTo("Created");

        processInstance = getCompleteProcessInstance("DXB", "BOM");
        processInstance.setId(processInstanceId);
        processInstance.getSteps().addAll(getExportStepInstances(processInstanceId, "DXB", "MCT"));
        processInstance.getSteps().addAll(getTransitImportStepInstances(processInstanceId, "DXB", "MCT"));
        processInstance.getSteps().addAll(getTransitExportStepInstances(processInstanceId, "MCT", "BOM"));
        processInstance.getSteps().addAll(getImportStepInstances(processInstanceId, "MCT", "BOM"));
        context.setProcessInstance(processInstance);
        context.setTenant("XX");
        processor.process(context);

        plan = processPlanRepository.findPlanByProcessInstanceIdAndCompleteIndicator("XX", processInstanceId, "Y");
        assertThat(plan.getStatus()).isEqualTo("Original");

        plan.setStatus(Constants.STATUS_BASELINE);
        processPlanRepository.save(plan);

        // create DEP actual milestone

        // when

        context = new DefaultContext();
        processInstance = getIncompleteProcessInstance("DXB", "BOM");
        processInstanceId = processInstance.getId();
        processInstance.getSteps().addAll(getExportStepInstances(processInstanceId, "DXB", "MCT"));
        processInstance.getSteps().addAll(getTransitImportStepInstances(processInstanceId, "DXB", "MCT"));
        context.setProcessInstance(processInstance);
        context.setTenant("XX");
        processor.process(context);

        plan = processPlanRepository.findPlanByProcessInstanceIdAndCompleteIndicator("XX", processInstanceId, "N");
        assertThat(plan.getStatus()).isEqualTo("Created");

        processInstance = getCompleteProcessInstance("DXB", "BOM");
        processInstance.setId(processInstanceId);
        processInstance.getSteps().addAll(getExportStepInstances(processInstanceId, "DXB", "MCT"));
        processInstance.getSteps().addAll(getTransitImportStepInstances(processInstanceId, "DXB", "MCT"));
        processInstance.getSteps().addAll(getTransitExportStepInstances(processInstanceId, "MCT", "BOM"));
        processInstance.getSteps().addAll(getImportStepInstances(processInstanceId, "MCT", "BOM"));
        context.setProcessInstance(processInstance);
        context.setTenant("XX");
        processor.process(context);

        // then
        plan = processPlanRepository.findPlanByProcessInstanceIdAndCompleteIndicator("XX", processInstanceId, "Y");
        assertThat(plan.getStatus()).isEqualTo(Constants.STATUS_BASELINE);
        assertThat(plan.getVersion()).isEqualTo(2);
        assertThat(plan.getPlanNumber()).isEqualTo(2);
        assertThat(plan.isRmpSent()).isTrue();

    }

    private Collection<? extends StepInstance> getImportStepInstances(String processInstanceId, String boardPoint, String offPoint) {
        List<StepInstance> stepInstances = new ArrayList<>();

        //ARR
        StepInstance arr = new StepInstance();
        arr.setFunctionalCtx("I");
        arr.setCreatedOn(LocalDateTime.now());
        arr.setStepCode("ARR");
        arr.setId(ObjectId.get().toString());
        arr.setProcessInstanceId(processInstanceId);
        arr.setTenant("XX");
        arr.setStatus("Created");
        arr.setGroupCode("F");
        arr.setVersion("1");
        arr.setLocationCode(boardPoint);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("boardPoint", "MCT");
        metadata.put("offPoint", "BOM");
        metadata.put("pieces", 10);
        metadata.put("wt", Double.valueOf(100));
        metadata.put("wtUnit", "K");
        metadata.put("vol", Double.valueOf(1));
        metadata.put("volUnit", "CM");
        metadata.put("flightNumber", "FZ0048");
        metadata.put("flightDate", LocalDate.parse("2022-06-06", DateTimeFormatter.ISO_DATE));
        arr.setMetadata(metadata);
        stepInstances.add(arr);


        //ARR
        StepInstance rcf = new StepInstance();
        rcf.setFunctionalCtx("I");
        rcf.setCreatedOn(LocalDateTime.now());
        rcf.setStepCode("RCF");
        rcf.setId(ObjectId.get().toString());
        rcf.setProcessInstanceId(processInstanceId);
        rcf.setTenant("XX");
        rcf.setStatus("Created");
        rcf.setGroupCode("F");
        rcf.setVersion("1");
        rcf.setLocationCode(boardPoint);
        Map<String, Object> rcfMetadata = new HashMap<>();
        rcfMetadata.put("boardPoint", "MCT");
        rcfMetadata.put("offPoint", "BOM");
        rcfMetadata.put("pieces", 10);
        rcfMetadata.put("wt", Double.valueOf(100));
        rcfMetadata.put("wtUnit", "K");
        rcfMetadata.put("vol", Double.valueOf(1));
        rcfMetadata.put("volUnit", "CM");
        rcfMetadata.put("flightNumber", "FZ0048");
        rcfMetadata.put("flightDate", LocalDate.parse("2022-06-06", DateTimeFormatter.ISO_DATE));
        rcf.setMetadata(rcfMetadata);
        stepInstances.add(rcf);

        //  AWD
        StepInstance awd = new StepInstance();
        awd.setFunctionalCtx("E");
        awd.setCreatedOn(LocalDateTime.now());
        awd.setStepCode("AWD");
        awd.setId(ObjectId.get().toString());
        awd.setProcessInstanceId(processInstanceId);
        awd.setTenant("XX");
        awd.setStatus("Created");
        awd.setGroupCode("S");
        awd.setVersion("1");
        awd.setLocationCode(boardPoint);
        Map<String, Object> awdMetadata = new HashMap<>();
        awdMetadata.put("boardPoint", "DXB");
        awdMetadata.put("offPoint", "BOM");
        awdMetadata.put("reservationPieces", 10);
        awdMetadata.put("reservationWeight", Double.valueOf(100));
        awdMetadata.put("reservationWeightUnit", "CM");
        awdMetadata.put("reservationVolume", Double.valueOf(1));
        awdMetadata.put("reservationVolumeUnit", "CM");
        awd.setMetadata(awdMetadata);
        stepInstances.add(awd);

        //  DLV
        StepInstance dlv = new StepInstance();
        dlv.setFunctionalCtx("E");
        dlv.setCreatedOn(LocalDateTime.now());
        dlv.setStepCode("DLV");
        dlv.setId(ObjectId.get().toString());
        dlv.setProcessInstanceId(processInstanceId);
        dlv.setTenant("XX");
        dlv.setStatus("Created");
        dlv.setGroupCode("S");
        dlv.setVersion("1");
        dlv.setLocationCode(boardPoint);
        Map<String, Object> dlvMetadata = new HashMap<>();
        dlvMetadata.put("boardPoint", "DXB");
        dlvMetadata.put("offPoint", "BOM");
        dlvMetadata.put("reservationPieces", 10);
        dlvMetadata.put("reservationWeight", Double.valueOf(100));
        dlvMetadata.put("reservationWeightUnit", "CM");
        dlvMetadata.put("reservationVolume", Double.valueOf(1));
        dlvMetadata.put("reservationVolumeUnit", "CM");
        dlv.setMetadata(dlvMetadata);
        stepInstances.add(dlv);

        return stepInstances;
    }

    private Collection<? extends StepInstance> getTransitExportStepInstances(String id, String boardPoint, String offPoint) {
        List<StepInstance> stepInstances = new ArrayList<>();

        //DEP
        StepInstance dep = new StepInstance();
        dep.setFunctionalCtx("T");
        dep.setCreatedOn(LocalDateTime.now());
        dep.setStepCode("DEP-T");
        dep.setId(ObjectId.get().toString());
        dep.setProcessInstanceId(id);
        dep.setTenant("XX");
        dep.setStatus("Created");
        dep.setGroupCode("F");
        dep.setVersion("1");
        dep.setLocationCode(boardPoint);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("boardPoint", "DXB");
        metadata.put("offPoint", "MCT");
        metadata.put("pieces", 10);
        metadata.put("wt", Double.valueOf(100));
        metadata.put("wtUnit", "K");
        metadata.put("vol", Double.valueOf(1));
        metadata.put("volUnit", "CM");
        metadata.put("flightNumber", "FZ0048");
        metadata.put("flightDate", LocalDate.parse("2022-06-06", DateTimeFormatter.ISO_DATE));
        dep.setMetadata(metadata);
        stepInstances.add(dep);
        return stepInstances;
    }

    private ProcessInstance getCompleteProcessInstance(String origin, String destination) {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setStatus("Updated");
        processInstance.setProcessType("CiQ");
        processInstance.setProcessCode("A2ATRANSPORT");
        processInstance.setTenant("XX");
        processInstance.setActive(true);
        processInstance.setComplete(true);
        processInstance.setEntityId("888100010001");
        processInstance.setEntityType("cargo.awb");
        processInstance.setCategoryCode("CiQ");
        processInstance.setValid(false);
        processInstance.setVersion(1);
        String processInstanceId = ObjectId.get().toString();
        processInstance.setId(processInstanceId); //507f191e810c19729de860ea
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("origin", origin);
        metadata.put("destination", destination);
        metadata.put("forwarderCode", "9XX");
        metadata.put("reservationPieces", 10);
        metadata.put("reservationWeight", Double.valueOf(100));
        metadata.put("reservationWeightUnit", "K");
        metadata.put("reservationVolumeUnit", "CM");
        metadata.put("reservationVolume", Double.valueOf(1));
        List<Itinerary> itineraries = new ArrayList<>();

        Itinerary itinerary = new Itinerary();
        itinerary.setBoardPoint(new StationInfo("DXB"));
        itinerary.setOffPoint(new StationInfo("MCT"));
        itinerary.setAircraftCategory("RFS");
        itinerary.setTransportInfo(new TransportInfo("FZ", "0046"));
        itinerary.setQuantity(new QuantityInfo(10, 100, 1));
        itinerary.setDepartureDateTimeUTC(new TransportTime(LocalDateTime.now()));
        itinerary.setArrivalDateTimeUTC(new TransportTime(LocalDateTime.now()));
        itineraries.add(itinerary);

        Itinerary itinerary2 = new Itinerary();
        itinerary2.setBoardPoint(new StationInfo("MCT"));
        itinerary2.setOffPoint(new StationInfo("BOM"));
        itinerary2.setAircraftCategory("RFS");
        itinerary2.setTransportInfo(new TransportInfo("FZ", "0048"));
        itinerary2.setQuantity(new QuantityInfo(10, 100, 1));
        itinerary2.setDepartureDateTimeUTC(new TransportTime(LocalDateTime.now()));
        itinerary2.setArrivalDateTimeUTC(new TransportTime(LocalDateTime.now()));
        itineraries.add(itinerary2);

        metadata.put("itineraries", itineraries);
        processInstance.setMetadata(metadata);
        processInstance.setSteps(new ArrayList<>());
        return processInstance;
    }

    private MeasurementInstance getMeasurementInstance(ProcessInstance processInstance, String measurementType) {
        MeasurementInstance depActualMeasurementInstance = new MeasurementInstance();
        depActualMeasurementInstance.setTenant("XX");
        depActualMeasurementInstance.setProcessInstanceId(processInstance.getId());
        depActualMeasurementInstance.setId(ObjectId.get().toString());
        depActualMeasurementInstance.setCreatedOn(ZonedDateTime.now());
        depActualMeasurementInstance.setMeasuredAt("DXB");
        depActualMeasurementInstance.setCode("TIME");
        depActualMeasurementInstance.setValue("2022-06-08 12:00");
        depActualMeasurementInstance.setType(measurementType);
        depActualMeasurementInstance.setStepCode("DEP");
        String stepId = processInstance.getSteps().stream()
                .filter(stepInstance -> stepInstance.getStepCode().equals("DEP")).map(StepInstance::getId)
                .findFirst().orElseThrow(() -> new RuntimeException());
        depActualMeasurementInstance.setStepInstanceId(stepId);
        return depActualMeasurementInstance;
    }


    private ProcessInstance getUpdatedProcessInstance(String origin, String destination, int pcs) {
        ProcessInstance updatedProcessInstance = getUpdatedProcessInstance(origin, destination);
        updatedProcessInstance.getMetadata().put("reservationPieces", pcs);
        return updatedProcessInstance;
    }


    @Test
    public void getProcessPlanInstance(@Autowired ProcessPlanRepository repository) {
        Optional<ProcessPlanInstance> byId = repository.findById("62b72447c0a23b5f031be9ff");
        if (byId.isPresent()) {
            System.out.println(byId.get());
        }
    }

    //    @AfterEach
    public void cleanup(@Autowired ProcessInstanceRepository processInstanceRepository,
                        @Autowired MeasurementInstanceRepository measurementInstanceRepository,
                        @Autowired ProcessPlanRepository processPlanRepository) {
        log.debug("cleanup is called");
        processInstanceRepository.deleteAll();
        processPlanRepository.deleteAll();
        measurementInstanceRepository.deleteAll();
    }


    @Test
    public void create_plan_with_measurements(@Autowired ProcessPlanRepository processPlanRepository) {
        ProcessPlanInstance plan = new ProcessPlanInstance();

        plan.setPlanNumber(1);
        plan.setCreatedOn(LocalDateTime.now());
        plan.setTenant("XX");
        ArrayListMultimap<String, MeasurementInstance> plannedMeasurements = ArrayListMultimap.create();
        MeasurementInstance time = new MeasurementInstance("XX", "TIME", "2022-06-12'T'10:10:00",
                "TIMESTAMP", "1", "1", "FWB", "P", "DXB", ZonedDateTime.now());
        MeasurementInstance pcs = new MeasurementInstance("XX", "PCS", "10",
                "NUMBER", "1", "1", "FWB", "P", "DXB", ZonedDateTime.now());
        MeasurementInstance rcsPcs = new MeasurementInstance("XX", "PCS", "10",
                "NUMBER", "1", "1", "RCS", "P", "DXB", ZonedDateTime.now());
        MeasurementInstance rcsPlanTime = new MeasurementInstance("XX", "TIME", "2022-06-12'T'10:10:00",
                "TIMESTAMP", "1", "1", "RCS", "P", "DXB", ZonedDateTime.now());
        plannedMeasurements.put("FWB", time);
        plannedMeasurements.put("FWB", pcs);
        plannedMeasurements.put("RCS", rcsPcs);
        plannedMeasurements.put("RCS", rcsPlanTime);

//        plan.setPlannedMeasurements(plannedMeasurements);
        ProcessPlanInstance save = processPlanRepository.save(plan);

        Optional<ProcessPlanInstance> byId = processPlanRepository.findById(save.getId());
        assertThat(byId.isPresent()).isTrue();
        log.debug(byId.get().toString());

    }

    private ProcessPlanInstance getPlan(String status, String id) {
        ProcessPlanInstance plan = new ProcessPlanInstance();
        plan.setRmpSent(true);
        String planId = ObjectId.get().toString();
        plan.setId(planId);
        plan.setShipmentIndicator("C");
        plan.setApprovedIndicator("N");
        plan.setProcessInstanceId(id);
        plan.setDirectTruckingIndicator("N");
        plan.setPhaseNumber("1");
        plan.setStatus(status);
        plan.setFlightSpecificIndicator("F");
        plan.setCompleteInd("Y");
        plan.setActiveInd("Y");
        plan.setVersion(1);
        plan.setTenant("XX");
        plan.setEntityId("888100010001");
        plan.setEntityType("cargo.awb");
        plan.setCreatedOn(LocalDateTime.now());
        return plan;
    }

    private ProcessInstance getProcessInstance(String origin, String destination, boolean complete) {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setStatus("Created");
        processInstance.setProcessType("CiQ");
        processInstance.setProcessCode("A2ATRANSPORT");
        processInstance.setTenant("XX");
        processInstance.setActive(complete);
        processInstance.setComplete(complete);
        processInstance.setEntityId("888100010001");
        processInstance.setEntityType("cargo.awb");
        processInstance.setCategoryCode("CiQ");
        processInstance.setValid(complete);
        processInstance.setVersion(1);
        String processInstanceId = ObjectId.get().toString();
        processInstance.setId(processInstanceId); //507f191e810c19729de860ea
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("origin", origin);
        metadata.put("destination", destination);
        metadata.put("forwarderCode", "9XX");
        metadata.put("reservationPieces", 10);
        metadata.put("reservationWeight", Double.valueOf(100));
        metadata.put("reservationWeightUnit", "K");
        metadata.put("reservationVolumeUnit", "CM");
        metadata.put("reservationVolume", Double.valueOf(1));
        List<Itinerary> itineraries = new ArrayList<>();
        Itinerary itinerary = new Itinerary();
        itinerary.setBoardPoint(new StationInfo("DXB"));
        itinerary.setOffPoint(new StationInfo("MCT"));
        itinerary.setAircraftCategory("RFS");
        itinerary.setTransportInfo(new TransportInfo("FZ", "0046"));
        itinerary.setQuantity(new QuantityInfo(10, 100, 1));
        itinerary.setDepartureDateTimeUTC(new TransportTime(LocalDateTime.now()));
        itinerary.setArrivalDateTimeUTC(new TransportTime(LocalDateTime.now()));
        metadata.put("itineraries", itineraries);
        processInstance.setMetadata(metadata);
        processInstance.setSteps(getStepInstances(processInstance.getId(), "DXB", "BOM"));
        return processInstance;
    }

    private ProcessInstance getIncompleteProcessInstance(String origin, String destination) {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setStatus("Created");
        processInstance.setProcessType("CiQ");
        processInstance.setProcessCode("A2ATRANSPORT");
        processInstance.setTenant("XX");
        processInstance.setActive(true);
        processInstance.setComplete(false);
        processInstance.setEntityId("888100010001");
        processInstance.setEntityType("cargo.awb");
        processInstance.setCategoryCode("CiQ");
        processInstance.setValid(false);
        processInstance.setVersion(1);
        String processInstanceId = ObjectId.get().toString();
        processInstance.setId(processInstanceId); //507f191e810c19729de860ea
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("origin", origin);
        metadata.put("destination", destination);
        metadata.put("forwarderCode", "9XX");
        metadata.put("reservationPieces", 10);
        metadata.put("reservationWeight", Double.valueOf(100));
        metadata.put("reservationWeightUnit", "K");
        metadata.put("reservationVolumeUnit", "CM");
        metadata.put("reservationVolume", Double.valueOf(1));
        List<Itinerary> itineraries = new ArrayList<>();
        Itinerary itinerary = new Itinerary();
        itinerary.setBoardPoint(new StationInfo("DXB"));
        itinerary.setOffPoint(new StationInfo("MCT"));
        itinerary.setAircraftCategory("RFS");
        itinerary.setTransportInfo(new TransportInfo("FZ", "0046"));
        itinerary.setQuantity(new QuantityInfo(10, 100, 1));
        itinerary.setDepartureDateTimeUTC(new TransportTime(LocalDateTime.now()));
        itinerary.setArrivalDateTimeUTC(new TransportTime(LocalDateTime.now()));
        metadata.put("itineraries", itineraries);
        processInstance.setMetadata(metadata);
        processInstance.setSteps(new ArrayList<>());
        return processInstance;
    }

    private ProcessInstance getUpdatedProcessInstance(String origin, String destination) {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setStatus("Created");
        processInstance.setProcessType("CiQ");
        processInstance.setProcessCode("A2ATRANSPORT");
        processInstance.setTenant("XX");
        processInstance.setActive(false);
        processInstance.setComplete(true);
        processInstance.setEntityId("888100010001");
        processInstance.setEntityType("cargo.awb");
        processInstance.setCategoryCode("CiQ");
        processInstance.setValid(true);
        processInstance.setVersion(1);
        processInstance.setId("507f191e810c19729de860ea"); //507f191e810c19729de860ea
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("origin", origin);
        metadata.put("destination", destination);
        metadata.put("forwarderCode", "9XX");
        metadata.put("reservationPieces", 10);
        metadata.put("reservationWeight", Double.valueOf(100));
        metadata.put("reservationWeightUnit", "K");
        metadata.put("reservationVolumeUnit", "CM");
        metadata.put("reservationVolume", Double.valueOf(1));
        processInstance.setMetadata(metadata);
        processInstance.setSteps(getStepInstances(processInstance.getId(), "DXB", "BOM"));
        return processInstance;
    }

    private List<StepInstance> getStepInstances(String processInstanceId, String boardPoint, String offPoint) {

        List<StepInstance> stepInstances = new ArrayList<>();

        //DEP
        StepInstance dep = new StepInstance();
        dep.setFunctionalCtx("E");
        dep.setCreatedOn(LocalDateTime.now());
        dep.setStepCode("DEP");
        dep.setId(ObjectId.get().toString());
        dep.setProcessInstanceId(processInstanceId);
        dep.setTenant("XX");
        dep.setStatus("Created");
        dep.setGroupCode("F");
        dep.setVersion("1");
        dep.setLocationCode(boardPoint);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("boardPoint", "DXB");
        metadata.put("offPoint", "MCT");
        metadata.put("pieces", 10);
        metadata.put("wt", Double.valueOf(100));
        metadata.put("wtUnit", "K");
        metadata.put("vol", Double.valueOf(1));
        metadata.put("volUnit", "CM");
        metadata.put("flightNumber", "FZ0046");
        metadata.put("flightDate", LocalDate.parse("2022-06-06", DateTimeFormatter.ISO_DATE));
        dep.setMetadata(metadata);
        stepInstances.add(dep);

        //ARR
        StepInstance arrT = new StepInstance();
        arrT.setFunctionalCtx("T");
        arrT.setCreatedOn(LocalDateTime.now());
        arrT.setStepCode("ARR-T");
        arrT.setId(ObjectId.get().toString());
        arrT.setProcessInstanceId(processInstanceId);
        arrT.setTenant("XX");
        arrT.setStatus("Created");
        arrT.setGroupCode("F");
        arrT.setVersion("1");
        arrT.setLocationCode(offPoint);
        Map<String, Object> arrTMetadata = new HashMap<>();
        arrTMetadata.put("boardPoint", "DXB");
        arrTMetadata.put("offPoint", "MCT");
        arrTMetadata.put("pieces", 10);
        arrTMetadata.put("wt", Double.valueOf(100));
        arrTMetadata.put("wtUnit", "K");
        arrTMetadata.put("vol", Double.valueOf(1));
        arrTMetadata.put("volUnit", "CM");
        arrTMetadata.put("flightNumber", "FZ0046");
        arrTMetadata.put("flightDate", LocalDate.parse("2022-06-06", DateTimeFormatter.ISO_DATE));
        arrT.setMetadata(arrTMetadata);
        stepInstances.add(arrT);

        //RCF
        StepInstance rcfT = new StepInstance();
        rcfT.setFunctionalCtx("T");
        rcfT.setCreatedOn(LocalDateTime.now());
        rcfT.setStepCode("RCF-T");
        rcfT.setId(ObjectId.get().toString());
        rcfT.setProcessInstanceId(processInstanceId);
        rcfT.setTenant("XX");
        rcfT.setStatus("Created");
        rcfT.setGroupCode("F");
        rcfT.setVersion("1");
        rcfT.setLocationCode(offPoint);
        Map<String, Object> rcfTMetadata = new HashMap<>();
        rcfTMetadata.put("boardPoint", "DXB");
        rcfTMetadata.put("offPoint", "MCT");
        rcfTMetadata.put("pieces", 10);
        rcfTMetadata.put("wt", Double.valueOf(100));
        rcfTMetadata.put("wtUnit", "K");
        rcfTMetadata.put("vol", Double.valueOf(1));
        rcfTMetadata.put("volUnit", "CM");
        rcfTMetadata.put("flightNumber", "FZ0046");
        rcfTMetadata.put("flightDate", LocalDate.parse("2022-06-06", DateTimeFormatter.ISO_DATE));
        rcfT.setMetadata(rcfTMetadata);
        stepInstances.add(rcfT);

        //  FWB
        StepInstance fwb = new StepInstance();
        fwb.setFunctionalCtx("E");
        fwb.setCreatedOn(LocalDateTime.now());
        fwb.setStepCode("FWB");
        fwb.setId(ObjectId.get().toString());
        fwb.setProcessInstanceId(processInstanceId);
        fwb.setTenant("XX");
        fwb.setStatus("Created");
        fwb.setGroupCode("S");
        fwb.setVersion("1");
        fwb.setLocationCode(boardPoint);
        Map<String, Object> fwbMetadata = new HashMap<>();
        fwbMetadata.put("boardPoint", "DXB");
        fwbMetadata.put("offPoint", "BOM");
        fwbMetadata.put("reservationPieces", 10);
        fwbMetadata.put("reservationWeight", Double.valueOf(100));
        fwbMetadata.put("reservationWeightUnit", "CM");
        fwbMetadata.put("reservationVolume", Double.valueOf(1));
        fwbMetadata.put("reservationVolumeUnit", "CM");
        fwb.setMetadata(fwbMetadata);
        stepInstances.add(fwb);


        //  LAT
        StepInstance lat = new StepInstance();
        lat.setFunctionalCtx("E");
        lat.setCreatedOn(LocalDateTime.now());
        lat.setStepCode("LAT");
        lat.setId(ObjectId.get().toString());
        lat.setProcessInstanceId(processInstanceId);
        lat.setTenant("XX");
        lat.setStatus("Created");
        lat.setGroupCode("S");
        lat.setVersion("1");
        lat.setLocationCode(boardPoint);
        Map<String, Object> latMetadata = new HashMap<>();
        latMetadata.put("boardPoint", "DXB");
        latMetadata.put("offPoint", "BOM");
        latMetadata.put("reservationPieces", 10);
        latMetadata.put("reservationWeight", Double.valueOf(100));
        latMetadata.put("reservationWeightUnit", "CM");
        latMetadata.put("reservationVolume", Double.valueOf(1));
        latMetadata.put("reservationVolumeUnit", "CM");
        lat.setMetadata(latMetadata);
        stepInstances.add(lat);

        //  LAT
        StepInstance rcs = new StepInstance();
        rcs.setFunctionalCtx("E");
        rcs.setCreatedOn(LocalDateTime.now());
        rcs.setStepCode("RCS");
        rcs.setId(ObjectId.get().toString());
        rcs.setProcessInstanceId(processInstanceId);
        rcs.setTenant("XX");
        rcs.setStatus("Created");
        rcs.setGroupCode("S");
        rcs.setVersion("1");
        rcs.setLocationCode(boardPoint);
        Map<String, Object> rcsMetadata = new HashMap<>();
        rcsMetadata.put("boardPoint", "DXB");
        rcsMetadata.put("offPoint", "BOM");
        rcsMetadata.put("reservationPieces", 10);
        rcsMetadata.put("reservationWeight", Double.valueOf(100));
        rcsMetadata.put("reservationWeightUnit", "CM");
        rcsMetadata.put("reservationVolume", Double.valueOf(1));
        rcsMetadata.put("reservationVolumeUnit", "CM");
        rcs.setMetadata(rcsMetadata);
        stepInstances.add(rcs);
        return stepInstances;
    }

    private List<StepInstance> getTransitImportStepInstances(String processInstanceId, String boardPoint, String offPoint) {

        List<StepInstance> stepInstances = new ArrayList<>();
        //ARR
        StepInstance arrT = new StepInstance();
        arrT.setFunctionalCtx("T");
        arrT.setCreatedOn(LocalDateTime.now());
        arrT.setStepCode("ARR-T");
        arrT.setId(ObjectId.get().toString());
        arrT.setProcessInstanceId(processInstanceId);
        arrT.setTenant("XX");
        arrT.setStatus("Created");
        arrT.setGroupCode("F");
        arrT.setVersion("1");
        arrT.setLocationCode(offPoint);
        Map<String, Object> arrTMetadata = new HashMap<>();
        arrTMetadata.put("boardPoint", "DXB");
        arrTMetadata.put("offPoint", "MCT");
        arrTMetadata.put("pieces", 10);
        arrTMetadata.put("wt", Double.valueOf(100));
        arrTMetadata.put("wtUnit", "K");
        arrTMetadata.put("vol", Double.valueOf(1));
        arrTMetadata.put("volUnit", "CM");
        arrTMetadata.put("flightNumber", "FZ0046");
        arrTMetadata.put("flightDate", LocalDate.parse("2022-06-06", DateTimeFormatter.ISO_DATE));
        arrT.setMetadata(arrTMetadata);
        stepInstances.add(arrT);

        //ARR
        StepInstance rcfT = new StepInstance();
        rcfT.setFunctionalCtx("T");
        rcfT.setCreatedOn(LocalDateTime.now());
        rcfT.setStepCode("RCF-T");
        rcfT.setId(ObjectId.get().toString());
        rcfT.setProcessInstanceId(processInstanceId);
        rcfT.setTenant("XX");
        rcfT.setStatus("Created");
        rcfT.setGroupCode("F");
        rcfT.setVersion("1");
        rcfT.setLocationCode(offPoint);
        Map<String, Object> rcfTMetadata = new HashMap<>();
        rcfTMetadata.put("boardPoint", "DXB");
        rcfTMetadata.put("offPoint", "MCT");
        rcfTMetadata.put("pieces", 10);
        rcfTMetadata.put("wt", Double.valueOf(100));
        rcfTMetadata.put("wtUnit", "K");
        rcfTMetadata.put("vol", Double.valueOf(1));
        rcfTMetadata.put("volUnit", "CM");
        rcfTMetadata.put("flightNumber", "FZ0046");
        rcfTMetadata.put("flightDate", LocalDate.parse("2022-06-06", DateTimeFormatter.ISO_DATE));
        rcfT.setMetadata(rcfTMetadata);
        stepInstances.add(rcfT);

        return stepInstances;
    }

    private List<StepInstance> getExportStepInstances(String processInstanceId, String boardPoint, String offPoint) {

        List<StepInstance> stepInstances = new ArrayList<>();

        //DEP
        StepInstance dep = new StepInstance();
        dep.setFunctionalCtx("E");
        dep.setCreatedOn(LocalDateTime.now());
        dep.setStepCode("DEP");
        dep.setId(ObjectId.get().toString());
        dep.setProcessInstanceId(processInstanceId);
        dep.setTenant("XX");
        dep.setStatus("Created");
        dep.setGroupCode("F");
        dep.setVersion("1");
        dep.setLocationCode(boardPoint);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("boardPoint", "DXB");
        metadata.put("offPoint", "MCT");
        metadata.put("pieces", 10);
        metadata.put("wt", Double.valueOf(100));
        metadata.put("wtUnit", "K");
        metadata.put("vol", Double.valueOf(1));
        metadata.put("volUnit", "CM");
        metadata.put("flightNumber", "FZ0046");
        metadata.put("flightDate", LocalDate.parse("2022-06-06", DateTimeFormatter.ISO_DATE));
        dep.setMetadata(metadata);
        stepInstances.add(dep);


        //  FWB
        StepInstance fwb = new StepInstance();
        fwb.setFunctionalCtx("E");
        fwb.setCreatedOn(LocalDateTime.now());
        fwb.setStepCode("FWB");
        fwb.setId(ObjectId.get().toString());
        fwb.setProcessInstanceId(processInstanceId);
        fwb.setTenant("XX");
        fwb.setStatus("Created");
        fwb.setGroupCode("S");
        fwb.setVersion("1");
        fwb.setLocationCode(boardPoint);
        Map<String, Object> fwbMetadata = new HashMap<>();
        fwbMetadata.put("boardPoint", "DXB");
        fwbMetadata.put("offPoint", "BOM");
        fwbMetadata.put("reservationPieces", 10);
        fwbMetadata.put("reservationWeight", Double.valueOf(100));
        fwbMetadata.put("reservationWeightUnit", "CM");
        fwbMetadata.put("reservationVolume", Double.valueOf(1));
        fwbMetadata.put("reservationVolumeUnit", "CM");
        fwb.setMetadata(fwbMetadata);
        stepInstances.add(fwb);


        //  LAT
        StepInstance lat = new StepInstance();
        lat.setFunctionalCtx("E");
        lat.setCreatedOn(LocalDateTime.now());
        lat.setStepCode("LAT");
        lat.setId(ObjectId.get().toString());
        lat.setProcessInstanceId(processInstanceId);
        lat.setTenant("XX");
        lat.setStatus("Created");
        lat.setGroupCode("S");
        lat.setVersion("1");
        lat.setLocationCode(boardPoint);
        Map<String, Object> latMetadata = new HashMap<>();
        latMetadata.put("boardPoint", "DXB");
        latMetadata.put("offPoint", "BOM");
        latMetadata.put("reservationPieces", 10);
        latMetadata.put("reservationWeight", Double.valueOf(100));
        latMetadata.put("reservationWeightUnit", "CM");
        latMetadata.put("reservationVolume", Double.valueOf(1));
        latMetadata.put("reservationVolumeUnit", "CM");
        lat.setMetadata(latMetadata);
        stepInstances.add(lat);

        //  LAT
        StepInstance rcs = new StepInstance();
        rcs.setFunctionalCtx("E");
        rcs.setCreatedOn(LocalDateTime.now());
        rcs.setStepCode("RCS");
        rcs.setId(ObjectId.get().toString());
        rcs.setProcessInstanceId(processInstanceId);
        rcs.setTenant("XX");
        rcs.setStatus("Created");
        rcs.setGroupCode("S");
        rcs.setVersion("1");
        rcs.setLocationCode(boardPoint);
        Map<String, Object> rcsMetadata = new HashMap<>();
        rcsMetadata.put("boardPoint", "DXB");
        rcsMetadata.put("offPoint", "BOM");
        rcsMetadata.put("reservationPieces", 10);
        rcsMetadata.put("reservationWeight", Double.valueOf(100));
        rcsMetadata.put("reservationWeightUnit", "CM");
        rcsMetadata.put("reservationVolume", Double.valueOf(1));
        rcsMetadata.put("reservationVolumeUnit", "CM");
        rcs.setMetadata(rcsMetadata);
        stepInstances.add(rcs);
        return stepInstances;
    }
}