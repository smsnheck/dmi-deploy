package com.ista.dmi.pcs.billing.process;

import com.ista.dmi.cds.client.ConsumptionCalculatorResourceClient;
import com.ista.dmi.cds.client.DeviceConsumptionResourceClient;
import com.ista.dmi.cds.client.UserConsumptionResourceClient;
import com.ista.dmi.common.domain.TimeRange;
import com.ista.dmi.common.domain.Uuid;
import com.ista.dmi.common.domain.value.InvoiceModeType;
import com.ista.dmi.common.resource.objects.AddressRO;
import com.ista.dmi.common.resource.objects.TimeRangeRO;
import com.ista.dmi.common.resource.objects.value.AllocationKeyEnumRO;
import com.ista.dmi.common.resource.objects.value.AllocationKeyRO;
import com.ista.dmi.common.soap.OutputManagementServiceClient;
import com.ista.dmi.cus.client.DistributionValueResourceClient;
import com.ista.dmi.cus.client.ServiceRecipientResourceClient;
import com.ista.dmi.ibs.client.BillingResultResourceClient;
import com.ista.dmi.ibs.resource.v1.BillingResultExportRO;
import com.ista.dmi.mdr.client.MeteredValueResourceClient;
import com.ista.dmi.pcs.billing.business.CostListUserListService;
import com.ista.dmi.pcs.billing.business.WorkOrderBusinessService;
import com.ista.dmi.pcs.billing.business.plausibilitycheck.MeteringPointFilterService;
import com.ista.dmi.pcs.billing.domain.BillingProcess;
import com.ista.dmi.pcs.billing.domain.InvoicingPeriodInformation;
import com.ista.dmi.pcs.billing.domain.Task;
import com.ista.dmi.pcs.billing.domain.WorkOrder;
import com.ista.dmi.pcs.billing.integration.InvoicingPeriodInformationIntegrationService;
import com.ista.dmi.pcs.billing.integration.TaskIntegrationService;
import com.ista.dmi.pcs.billing.process.adapter.*;
import com.ista.dmi.pcs.billing.process.api.ProcessStarterService;
import com.ista.dmi.pcs.billing.process.api.ProductConfigurationService;
import com.ista.dmi.pcs.billing.process.api.event.MessageEventEnum;
import com.ista.dmi.pcs.billing.process.common.ProcessTestUtil;
import com.ista.dmi.pcs.billing.process.dataaccessor.*;
import com.ista.dmi.pcs.billing.process.listener.CreateOpenServiceOrdersTaskListener;
import com.ista.dmi.pcs.billing.process.listener.StatusInformationService;
import com.ista.dmi.pcs.billing.process.listener.TaskConnectionListener;
import com.ista.dmi.pcs.billing.process.service.CostAndUserListCreationDateService;
import com.ista.dmi.pcs.billing.process.service.InvoicingPeriodInformationService;
import com.ista.dmi.pcs.billing.soap.InvoiceServiceClient;
import com.ista.dmi.pcs.billing.soap.OrderServiceClient;
import com.ista.dmi.pcs.billing.soap.mapping.InvoiceType;
import com.ista.dmi.pcs.common.AbstractProcessCDITest;
import com.ista.dmi.pds.buildingstructure.PropertyRO;
import com.ista.dmi.pds.client.*;
import com.ista.dmi.pds.customer.CustomerRO;
import com.ista.dmi.pds.productstructure.*;
import com.ista.soa.erp.invoiceservice.v_1_1.FeedbackFaultMsg;
import com.ista.soa.erp.invoiceservice.v_1_1.IstaFaultMsg;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.hamcrest.MatcherAssert;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.jglue.cdiunit.ProducesAlternative;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(CdiRunner.class)
@AdditionalClasses({DetermineFlatRateBillingAdapter.class, ConsumptionCalculationAdapter.class, ErrorConsumptionDeterminationAdapter.class,
  CostAllocationAdapter.class, ReportProcessCompletionAdapter.class, ApplyPlausibilityAdapter.class, ChangedMeteredValuesAdapter.class,
  CreateServiceInvoiceAdapter.class, ProvideServiceInvoiceAndKoliNuliForPrintingAdapter.class, DetermineReviewServiceInvoiceAndKoliNuliAdapter.class,
  DetermineBillingDateAdapter.class,CreateFinalInvoiceAdapter.class, ProvideBillingDocumentsForPrintingAdapter.class,
  TaskConnectionListener.class, CreateOpenServiceOrdersTaskListener.class, ProductConfigurationService.class, InvoicingPeriodInformationService.class,
  CostAndUserListCreationDateService.class, DeleteConsumptionsAdapter.class, UnignoreViolationsAdapter.class, ValidateServiceInvoiceNumbersAdapter.class,
  ValidateFinalInvoiceNumbersAdapter.class, CheckInvoiceInformationAdapter.class, StatusInformationService.class})
public class BillingProcessCDITest extends AbstractProcessCDITest {

  @Override
  protected List<String> getBpmnFileNames() {
    return Arrays.stream(BillingProcess.values())
      .map(BillingProcess::getFileName)
      .collect(toList());
  }

  @ProducesAlternative
  @Produces
  @Mock
  private ConsumptionCalculatorResourceClient consumptionCalculatorResourceClientMock;

  @ProducesAlternative
  @Produces
  @Mock
  private DeviceConsumptionResourceClient deviceConsumptionResourceClientMock;

  @ProducesAlternative
  @Produces
  @Mock
  private UserConsumptionResourceClient userConsumptionResourceClientMock;

  @ProducesAlternative
  @Produces
  @Mock
  private BillingResultResourceClient billingResultResourceClientMock;

  @ProducesAlternative
  @Produces
  @Mock
  private OrderServiceClient orderServiceClientMock;

  @ProducesAlternative
  @Produces
  @Mock
  private MeteringPointSummaryResourceClient meteringPointSummaryResourceClientMock;

  @ProducesAlternative
  @Produces
  @Mock
  private HeatCostAllocatorSummaryResourceClient heatCostAllocatorSummaryResourceClientMock;

  @ProducesAlternative
  @Produces
  @Mock
  private VolumeterSummaryResourceClient volumeterSummaryResourceClientMock;

  @ProducesAlternative
  @Produces
  @Mock
  private BillingMpcResourceClient billingMainProductConfigurationResourceClientMock;

  @ProducesAlternative
  @Produces
  @Mock
  private UsageUnitResourceClient usageUnitResourceClientMock;

  @ProducesAlternative
  @Produces
  @Mock
  private ServiceRecipientResourceClient serviceRecipientResourceClientMock;

  @ProducesAlternative
  @Produces
  @Mock
  private DistributionValueResourceClient distributionValueResourceClientMock;

  @ProducesAlternative
  @Produces
  @Mock
  private InnerFeeResourceClient innerFeeResourceClientMock;

  @ProducesAlternative
  @Produces
  @Mock
  private InvoiceServiceClient invoiceServiceClientMock;

  @ProducesAlternative
  @Produces
  @Mock
  private OutputManagementServiceClient outputManagementServiceClientMock;

  @ProducesAlternative
  @Produces
  @Mock
  private MeteredValueResourceClient meteredValueResourceClientMock;

  @ProducesAlternative
  @Produces
  @Mock
  private CostListUserListService costListUserListServiceMock;

  @ProducesAlternative
  @Produces
  @Mock
  private PropertyResourceClient propertyResourceClientMock;

  @ProducesAlternative
  @Produces
  @Mock
  private CustomerResourceClient customerResourceClientMock;

  @ProducesAlternative
  @Produces
  @Mock
  private ReadingMpcResourceClient readingMpcResourceClientMock;

  @ProducesAlternative
  @Produces
  @Mock
  private InvoicingPeriodInformationIntegrationService invoicingPeriodInformationIntegrationServiceMock;

  @ProducesAlternative
  @Produces
  @Mock
  private WorkOrderBusinessService workOrderBusinessServiceMock;

  @ProducesAlternative
  @Produces
  @Mock
  private TaskIntegrationService taskIntegrationServiceMock;

  @ProducesAlternative
  @Produces
  @Mock
  private MeteringPointResourceClient meteringPointResourceClientMock;

  @ProducesAlternative
  @Produces
  @Mock
  private MeteringPointFilterService meteringPointFilterServiceMock;

  @ProducesAlternative
  @Produces
  @Mock
  private HeatCostAllocatorResourceClient heatCostAllocatorResourceClientMock;

  @ProducesAlternative
  @Produces
  @Mock
  private PropertyInfoTextResourceClient propertyInfoTextResourceClientMock;

  @ProducesAlternative
  @Produces
  @Mock
  private BillingProcessChangeNotificationResourceClient billingProcessChangeNotificationResourceClientMock;

  @Inject
  private Instance<ProcessStarterService> instance;
  private ProcessStarterService processStarterService;

  @Before
  public void setUp(){
    // init custom instances
    processStarterService = instance.get();
    String customerNumber = "0009999999";
    PropertyRO propertyRO = new PropertyRO();
    propertyRO.setCustomerNumber(customerNumber);
    propertyRO.setPropertyNumber("123456789");
    CustomerRO customerRO = new CustomerRO();
    customerRO.setCustomerNumber(customerNumber);
    AddressRO addressRO = new AddressRO();
    addressRO.setPostalCode("12345");
    customerRO.setAddress(addressRO);
    when(propertyResourceClientMock.getProperty(any(Uuid.class))).thenReturn(propertyRO);
    when(customerResourceClientMock.getCustomer(customerNumber)).thenReturn(customerRO);
    when(readingMpcResourceClientMock.getReadingConfigurationsForProperty(any(Uuid.class), any(Uuid.class))).thenReturn(null);
    final Task userTask = new Task();
    userTask.setUuid(Uuid.randomUuid());
    when(taskIntegrationServiceMock.find(any(Uuid.class))).thenReturn(userTask);
    BillingMpcRO billingMpcRO = new BillingMpcRO();
    billingMpcRO.setInvoicingPeriod(new TimeRangeRO());
    when(billingMainProductConfigurationResourceClientMock.getBillingMainProductConfiguration(any(Uuid.class), any(Uuid.class))).thenReturn(billingMpcRO);
  }

  @Produces
  private ProcessEngine getProcessEngine() {
    return processEngineRule.getProcessEngine();
  }

  @Produces
  private RuntimeService getRuntimeService() {
    return getProcessEngine().getRuntimeService();
  }

  @Produces
  private TaskService getTaskService() {
    return getProcessEngine().getTaskService();
  }

  @Produces
  private RepositoryService getRepositoryService() {
    return getProcessEngine().getRepositoryService();
  }

  @Test
  public void shouldPassEachSubProcess() throws IstaFaultMsg, com.ista.soa.erp.orderservice.v_1_2.IstaFault, FeedbackFaultMsg {
    // given
    Uuid billingMainProductConfigurationUuid = Uuid.randomUuid();
    Uuid propertyUuid = Uuid.randomUuid();
    String propertyNumber = "026452998";
    TimeRange invoicingPeriod = new TimeRange("2012-01-01", "2013-01-01");
    String contractOrderNumber = "contractOrderNumber";
    String invoiceTransactionId = "invoiceTransactionId";
    String invoiceNumber = "invoiceNumber";
    List<String> serviceInvoiceAttachmentNumbers = Collections.singletonList("01");
    String finalInvoiceTransactionId = "finalInvoiceTransactionId";
    String finalInvoiceNumber = "finalInvoiceNumber";
    List<String> finalInvoiceAttachmentNumbers = Arrays.asList("01", "02");
    BillingMpcRO billingMpcRO = createBillingMainProductConfigurationRO(billingMainProductConfigurationUuid.getUuid());
    billingMpcRO.setCostAllocations(Collections.singletonList(createConsumptionDependentCostAllocationRO()));
    when(billingMainProductConfigurationResourceClientMock.getBillingMainProductConfiguration(propertyUuid, billingMainProductConfigurationUuid)).thenReturn(billingMpcRO);
    when(invoiceServiceClientMock.createInvoice(eq(contractOrderNumber), any(DateTime.class), eq(false), eq(Collections.singletonList(InvoiceModeType.IN_ARREAR)), eq(InvoiceType.SERVICE_INVOICE))).thenReturn(invoiceTransactionId);
    when(invoiceServiceClientMock.createInvoice(eq(contractOrderNumber), any(DateTime.class), eq(false), eq(Collections.singletonList(InvoiceModeType.IN_ARREAR)), eq(InvoiceType.SERVICE_INVOICE_SIMULATION))).thenReturn(invoiceTransactionId);
    when(invoiceServiceClientMock.createInvoice(eq(contractOrderNumber), any(DateTime.class), eq(false), eq(Collections.singletonList(InvoiceModeType.FINAL_INVOICE)), eq(InvoiceType.FINAL_INVOICE))).thenReturn(finalInvoiceTransactionId);
    String xmlData = "<root><itemElement/></root>";
    when(invoiceServiceClientMock.renderInvoice(invoiceNumber, serviceInvoiceAttachmentNumbers.get(0))).thenReturn(xmlData);
    when(costListUserListServiceMock.provideCostListUserList(propertyUuid, billingMainProductConfigurationUuid)).thenReturn("<CostListUserListDocument/>");
    when(billingResultResourceClientMock.produceBillingResultExport(propertyUuid, billingMainProductConfigurationUuid)).thenReturn(new BillingResultExportRO());
    final InvoicingPeriodInformation invoicingPeriodInformation = new InvoicingPeriodInformation();
    invoicingPeriodInformation.setReleaseDate(new DateTime());
    when(invoicingPeriodInformationIntegrationServiceMock.find(eq(propertyUuid), eq(billingMainProductConfigurationUuid))).thenReturn(invoicingPeriodInformation);
    Long workOrderId = 1L;
    WorkOrder workOrder = new WorkOrder();
    workOrder.setId(workOrderId);
    workOrder.setBillingMainProductConfigurationUuid(billingMainProductConfigurationUuid);
    workOrder.setPropertyUuid(propertyUuid);
    workOrder.setInvoicingPeriod(invoicingPeriod);
    workOrder.setContractOrderNumber(contractOrderNumber);
    workOrder.setTasks(new ArrayList());
    when(workOrderBusinessServiceMock.find(workOrderId)).thenReturn(workOrder);
    when(workOrderBusinessServiceMock.find(eq(propertyUuid), eq(billingMainProductConfigurationUuid), anyString())).thenReturn(workOrder);

    // when
    ProcessInstance mainProcessInstanceBillingProcess = processStarterService.startBillingProcess(propertyNumber, contractOrderNumber, workOrderId);

    // then
    assertThat(mainProcessInstanceBillingProcess.getBusinessKey(), notNullValue());
    assertThat(mainProcessInstanceBillingProcess).hasPassed("IntermediateThrowEvent_StartBillingProcess");

    testSendServiceInvoiceAndKoliNuliProcess(invoiceTransactionId, invoiceNumber, serviceInvoiceAttachmentNumbers, mainProcessInstanceBillingProcess);
    testCustomerDataCollection(mainProcessInstanceBillingProcess);
    testMeteringDataCollection(mainProcessInstanceBillingProcess);
    testConsumptionCalculationProcess(mainProcessInstanceBillingProcess);
    testCostAllocationProcess(finalInvoiceTransactionId, finalInvoiceNumber, finalInvoiceAttachmentNumbers, mainProcessInstanceBillingProcess);
    testProvisionOfBillingProcess(mainProcessInstanceBillingProcess);
    testProcessCompletionProcess(mainProcessInstanceBillingProcess);

    assertThat(mainProcessInstanceBillingProcess).hasPassed("IntermediateThrowEvent_EndBillingProcess");
    assertThat(mainProcessInstanceBillingProcess).isEnded();
    MatcherAssert.assertThat(historyService().createHistoricProcessInstanceQuery().list().size(), equalTo(11));

    verify(consumptionCalculatorResourceClientMock).calculateConsumption(any(Uuid.class), any(Uuid.class));
    verify(deviceConsumptionResourceClientMock, times(2)).getDeviceConsumptions(eq(propertyUuid), eq(billingMainProductConfigurationUuid));
    verify(userConsumptionResourceClientMock).getUserConsumptions(eq(propertyUuid), eq(billingMainProductConfigurationUuid));
    verify(billingResultResourceClientMock).calculateAndProduceBillingResult(any(Uuid.class), any(Uuid.class));
    verify(orderServiceClientMock).updateOrderStatus(any(String.class), any(DateTime.class));
    verify(billingMainProductConfigurationResourceClientMock, times(7)).getBillingMainProductConfiguration(any(Uuid.class), any(Uuid.class));
    verify(meteringPointSummaryResourceClientMock).getMeteringPointSummariesForProperty(any(Uuid.class), any(DateTime.class), any(DateTime.class));
    verify(heatCostAllocatorSummaryResourceClientMock).getHeatCostAllocatorSummariesForProperty(any(Uuid.class), any(DateTime.class), any(DateTime.class));
    verify(volumeterSummaryResourceClientMock).getVolumeterSummariesForProperty(any(Uuid.class), any(DateTime.class), any(DateTime.class));
    verify(usageUnitResourceClientMock).getUsageUnitsForProperty(any(Uuid.class), any(DateTime.class), any(DateTime.class));
    verify(serviceRecipientResourceClientMock).getServiceRecipientsForProperty(any(Uuid.class), any(DateTime.class), any(DateTime.class));
    verify(distributionValueResourceClientMock).getDistributionValues(any(Uuid.class), any(Uuid.class));
    verify(innerFeeResourceClientMock).getInnerFees(any(Uuid.class), any(Uuid.class), any());
    verify(propertyResourceClientMock).getProperty(any(Uuid.class));
    verify(meteringPointResourceClientMock).getMeteringPointsForProperty(any(Uuid.class), any(DateTime.class), any(DateTime.class));
  }

  private void testProcessCompletionProcess(ProcessInstance mainProcessInstanceBillingProcess) {
    // check whether active instances available or not for a process definition
    ProcessInstance subProcessInstanceProcessCompletionProcess = calledProcessInstance(BillingProcess.BILLING_COMPLETION.getProcessDefinitionKey(), mainProcessInstanceBillingProcess);

    assertThat(subProcessInstanceProcessCompletionProcess).isWaitingAt("StartEvent_Billing_Provided")
      .hasVariables(ProcessCompletionProcessDataAccessor.WORK_ORDER_ID);
    execute(job());
    assertThat(subProcessInstanceProcessCompletionProcess).isEnded();

    ProcessTestUtil.assertHistoricVariable(subProcessInstanceProcessCompletionProcess,
      ProcessCompletionProcessDataAccessor.WORK_ORDER_ID);
  }

  private void testProvisionOfBillingProcess(ProcessInstance mainProcessInstanceBillingProcess) {
    ProcessInstance subProcessInstanceProvisionOfBillingProcess = calledProcessInstance(BillingProcess.BILLING_PROVISION.getProcessDefinitionKey(), mainProcessInstanceBillingProcess);
    assertThat(subProcessInstanceProvisionOfBillingProcess.getBusinessKey(), notNullValue());
    assertThat(subProcessInstanceProvisionOfBillingProcess).isWaitingAt("StartEvent_Billing_Prepared")
      .hasVariables(BillingProcessDataAccessor.WORK_ORDER_ID,
        ProvisionOfBillingDataAccessor.FINAL_INVOICE_NUMBER,
        ProvisionOfBillingDataAccessor.FINAL_INVOICE_TRANSACTION_ID,
    ProvisionOfBillingDataAccessor.FINAL_INVOICE_ATTACHMENT_NUMBERS);
    execute(job());

    assertThat(subProcessInstanceProvisionOfBillingProcess).hasPassed("ServiceTask_Provide_Billing_Documents_For_Printing");

    assertThat(subProcessInstanceProvisionOfBillingProcess).isEnded();
  }

  private void testCostAllocationProcess(String finalInvoiceTransactionId, String finalInvoiceNumber, List<String> finalInvoiceAttachmentNumbers, ProcessInstance mainProcessInstanceBillingProcess) {
    ProcessInstance subProcessInstanceCostAllocationProcess = calledProcessInstance(BillingProcess.BILLING_COST_ALLOCATION.getProcessDefinitionKey(), mainProcessInstanceBillingProcess);
    assertThat(subProcessInstanceCostAllocationProcess.getBusinessKey(), notNullValue());
    assertThat(subProcessInstanceCostAllocationProcess).isWaitingAt("ServiceTask_Execute_Cost_Allocation")
      .hasVariables(BillingProcessDataAccessor.WORK_ORDER_ID);
    execute(job());
    assertThat(subProcessInstanceCostAllocationProcess).isWaitingAt("UserTask_Review_Billing");
    complete(task(), withVariables(ProcessVariableConstants.PROCESS_VARIABLE_CONTINUE_USER_TASK, true));
    assertThat(subProcessInstanceCostAllocationProcess).isWaitingAt("ServiceTask_Create_Final_Invoice")
      .hasVariables(BillingProcessDataAccessor.WORK_ORDER_ID);
    execute(job());

    assertThat(subProcessInstanceCostAllocationProcess).hasPassed("ServiceTask_Create_Final_Invoice");
    assertThat(subProcessInstanceCostAllocationProcess).hasVariables(CostAllocationProcessDataAccessor.FINAL_INVOICE_TRANSACTION_ID);
    assertThat(subProcessInstanceCostAllocationProcess).isWaitingAt("ReceiveTask_Retrieve_Final_Invoice_Numbers");
    assertThat(subProcessInstanceCostAllocationProcess).isWaitingFor(MessageEventEnum.MSG_RETRIEVE_FINAL_INVOICE_NUMBERS.name());

    List<EventSubscription> list = runtimeService().createEventSubscriptionQuery().eventType("message").list();

    runtimeService().createMessageCorrelation(MessageEventEnum.MSG_RETRIEVE_FINAL_INVOICE_NUMBERS.name())
      .processInstanceVariableEquals(ProcessVariableConstants.PROCESS_VARIABLE_FINAL_INVOICE_TRANSACTION_ID, finalInvoiceTransactionId)
      .setVariable(ProcessVariableConstants.PROCESS_VARIABLE_FINAL_INVOICE_NUMBER, finalInvoiceNumber)
      .setVariable(ProcessVariableConstants.PROCESS_VARIABLE_FINAL_INVOICE_ATTACHMENT_NUMBERS, finalInvoiceAttachmentNumbers)
    .correlate();

    assertThat(subProcessInstanceCostAllocationProcess).isWaitingAt("ServiceTask_Validate_Final_Invoice_Numbers");
    execute(job());

    assertThat(subProcessInstanceCostAllocationProcess).isEnded();
  }

  private void testConsumptionCalculationProcess(ProcessInstance mainProcessInstanceBillingProcess) {
    ProcessInstance subProcessInstanceConsumptionCalculationProcess = calledProcessInstance(BillingProcess.BILLING_CONSUMPTION_CALCULATION.getProcessDefinitionKey(), mainProcessInstanceBillingProcess);
    assertThat(subProcessInstanceConsumptionCalculationProcess.getBusinessKey(), notNullValue());

    assertThat(subProcessInstanceConsumptionCalculationProcess).isWaitingAt("StartEvent_Calculation_Requested")
      .hasVariables(ConsumptionCalculationProcessDataAccessor.WORK_ORDER_ID);
    execute(job());

    assertThat(subProcessInstanceConsumptionCalculationProcess).isWaitingAt("ServiceTask_Check_For_Invoicing_Related_Information");
    execute(job());

    assertThat(subProcessInstanceConsumptionCalculationProcess).task("UserTask_Display_Invoicing_Related_Information");
    complete(task(), withVariables(ProcessVariableConstants.PROCESS_VARIABLE_CONTINUE_USER_TASK, true));

    assertThat(subProcessInstanceConsumptionCalculationProcess).isWaitingAt("CallActivity_Check_Plausibility");
    testCheckPlausibilityProcess(subProcessInstanceConsumptionCalculationProcess);

    assertThat(subProcessInstanceConsumptionCalculationProcess).isWaitingAt("ServiceTask_Calculate_Consumption")
      .hasVariables(BillingProcessDataAccessor.WORK_ORDER_ID)
      .hasVariables(ConsumptionCalculationProcessDataAccessor.FLAT_RATE_BILLING);
    execute(job());

    assertThat(subProcessInstanceConsumptionCalculationProcess).isWaitingAt("ServiceTask_Check_For_Errors_Consumptions")
      .hasVariables(BillingProcessDataAccessor.WORK_ORDER_ID);
    execute(job());

    assertThat(subProcessInstanceConsumptionCalculationProcess).task("UserTask_Check_Consumptions_Plausibility");
    assertThat(subProcessInstanceConsumptionCalculationProcess).hasVariables(ConsumptionCalculationProcessDataAccessor.ERRORS_IN_CONSUMPTIONS);
    complete(task(), withVariables(ProcessVariableConstants.PROCESS_VARIABLE_CONTINUE_USER_TASK, true));

    assertThat(subProcessInstanceConsumptionCalculationProcess).isEnded();
  }

  private void testCheckPlausibilityProcess(ProcessInstance mainProcessInstanceBillingProcess) {
    ProcessInstance subProcessInstanceCheckPlausibilityProcess = calledProcessInstance(BillingProcess.BILLING_CHECK_PLAUSIBILITY.getProcessDefinitionKey(), mainProcessInstanceBillingProcess);
    assertThat(subProcessInstanceCheckPlausibilityProcess.getBusinessKey(), notNullValue());
    assertThat(subProcessInstanceCheckPlausibilityProcess).isWaitingAt("BusinessRuleTask_Apply_Plausibility")
      .hasVariables(BillingProcessDataAccessor.WORK_ORDER_ID);
    execute(job());

    assertThat(subProcessInstanceCheckPlausibilityProcess).isEnded();
  }

  private void testMeteringDataCollection(ProcessInstance mainProcessInstanceBillingProcess) {
    final ProcessInstance subProcessInstanceMeteringDataCollectionProcess = calledProcessInstance(BillingProcess.BILLING_METERING_DATA_COLLECTION.getProcessDefinitionKey(), mainProcessInstanceBillingProcess);
    assertThat(subProcessInstanceMeteringDataCollectionProcess.getBusinessKey(), notNullValue());
    assertThat(subProcessInstanceMeteringDataCollectionProcess).isWaitingAt("StartEvent_Metering_Data_Collection_Requested")
      .hasVariables(MeteringDataCollectionDataAccessor.WORK_ORDER_ID);;
    execute(job());

    assertThat(subProcessInstanceMeteringDataCollectionProcess).task("UserTask_Check_Open_Service_Orders_Exist");
    complete(task());

    assertThat(subProcessInstanceMeteringDataCollectionProcess).isEnded();
  }

  private void testCustomerDataCollection(ProcessInstance mainProcessInstanceBillingProcess) {
    ProcessInstance subProcessInstanceCustomerDataCollectionProcess = calledProcessInstance(BillingProcess.BILLING_CUSTOMER_DATA_COLLECTION.getProcessDefinitionKey(), mainProcessInstanceBillingProcess);
    assertThat(subProcessInstanceCustomerDataCollectionProcess.getBusinessKey(), notNullValue());
    assertThat(subProcessInstanceCustomerDataCollectionProcess).isWaitingAt("StartEvent_Customer_Data_Collection_Requested")
      .hasVariables(BillingProcessDataAccessor.WORK_ORDER_ID);
    execute(job());

    assertThat(subProcessInstanceCustomerDataCollectionProcess).hasPassed("ExclusiveGateway_Is_Released_For_Billing_End");

    assertThat(subProcessInstanceCustomerDataCollectionProcess).isEnded();
  }

  private void testSendServiceInvoiceAndKoliNuliProcess(String invoiceTransactionId, String invoiceNumber, List<String> serviceInvoiceAttachmentNumbers, ProcessInstance mainProcessInstanceBillingProcess) {
    ProcessInstance subProcessInstanceSendServiceInvoiceAndKoliNuliProcess = calledProcessInstance(BillingProcess.BILLING_SEND_SERVICE_INVOICE_AND_KOLI_NULI.getProcessDefinitionKey(), mainProcessInstanceBillingProcess);
    assertThat(subProcessInstanceSendServiceInvoiceAndKoliNuliProcess.getBusinessKey(), notNullValue());
    assertThat(subProcessInstanceSendServiceInvoiceAndKoliNuliProcess).isWaitingAt("StartEvent_ServiceInvoice_And_KoliNuli_Requested")
      .hasVariables(SendServiceInvoiceAndKoliNuliDataAccessor.WORK_ORDER_ID);
    execute(job());

    assertThat(subProcessInstanceSendServiceInvoiceAndKoliNuliProcess).isWaitingAt("EventBasedGateway_After_Determine_Billing_Date")
        .hasVariables(SendServiceInvoiceAndKoliNuliDataAccessor.WORK_ORDER_ID);
    execute(job());

    assertThat(subProcessInstanceSendServiceInvoiceAndKoliNuliProcess).isWaitingAt("CallActivity_Simulate_Service_Invoice")
      .hasVariables(SendServiceInvoiceAndKoliNuliDataAccessor.WORK_ORDER_ID);
    testCreateServiceInvoiceProcess(invoiceTransactionId, invoiceNumber, serviceInvoiceAttachmentNumbers, subProcessInstanceSendServiceInvoiceAndKoliNuliProcess);

    assertThat(subProcessInstanceSendServiceInvoiceAndKoliNuliProcess).isWaitingAt("ServiceTask_Determine_Review_Service_Invoice_And_Koli_Nuli")
      .hasVariables(SendServiceInvoiceAndKoliNuliDataAccessor.WORK_ORDER_ID)
      .variables()
        .containsEntry(ProcessVariableConstants.PROCESS_VARIABLE_PROVIDE_SERVICE_INVOICE, String.valueOf(true));
    execute(job());

    assertThat(subProcessInstanceSendServiceInvoiceAndKoliNuliProcess).isWaitingAt("UserTask_Review_Service_Invoice_And_Koli_Nuli")
        .hasVariables(SendServiceInvoiceAndKoliNuliDataAccessor.WORK_ORDER_ID);
    complete(task(), withVariables(ProcessVariableConstants.PROCESS_VARIABLE_CONTINUE_USER_TASK, true));

    assertThat(subProcessInstanceSendServiceInvoiceAndKoliNuliProcess).isWaitingAt("ServiceTask_Determine_Billing_Date_For_Service_Invoice")
        .hasVariables(SendServiceInvoiceAndKoliNuliDataAccessor.WORK_ORDER_ID);
    execute(job());

    assertThat(subProcessInstanceSendServiceInvoiceAndKoliNuliProcess).isWaitingAt("CallActivity_Create_Service_Invoice")
      .hasVariables(SendServiceInvoiceAndKoliNuliDataAccessor.WORK_ORDER_ID);
    testCreateServiceInvoiceProcess(invoiceTransactionId, invoiceNumber, serviceInvoiceAttachmentNumbers, subProcessInstanceSendServiceInvoiceAndKoliNuliProcess);

    assertThat(subProcessInstanceSendServiceInvoiceAndKoliNuliProcess).isWaitingAt("IntermediateCatchEvent_Wait_For_Providing_Dates")
      .hasVariables(SendServiceInvoiceAndKoliNuliDataAccessor.COST_LIST_USER_LIST_CREATION_DATE);
    execute(job());

    assertThat(subProcessInstanceSendServiceInvoiceAndKoliNuliProcess).isWaitingAt("ServiceTask_Provide_Service_Invoice_And_Koli_Nuli_For_Printing")
      .hasVariables(SendServiceInvoiceAndKoliNuliDataAccessor.WORK_ORDER_ID,
        SendServiceInvoiceAndKoliNuliDataAccessor.SERVICE_INVOICE_NUMBER,
        SendServiceInvoiceAndKoliNuliDataAccessor.SERVICE_INVOICE_ATTACHMENT_NUMBERS);
    execute(job());
    assertThat(subProcessInstanceSendServiceInvoiceAndKoliNuliProcess).isEnded();
  }

  private void testCreateServiceInvoiceProcess(String invoiceTransactionId, String serviceInvoiceNumber, List<String> serviceInvoiceAttachmentNumbers, ProcessInstance sendServiceInvoiceAndKoliNuliProcess) {
    ProcessInstance subProcessInstanceCreateServiceInvoiceProcess = calledProcessInstance(BillingProcess.BILLING_CREATE_SERVICE_INVOICE.getProcessDefinitionKey(), sendServiceInvoiceAndKoliNuliProcess);
    assertThat(subProcessInstanceCreateServiceInvoiceProcess.getBusinessKey(), notNullValue());
    assertThat(subProcessInstanceCreateServiceInvoiceProcess).isWaitingAt("StartEvent_Create_Service_Invoice")
      .hasVariables(BillingProcessDataAccessor.WORK_ORDER_ID);
    execute(job());

    assertThat(subProcessInstanceCreateServiceInvoiceProcess).isWaitingAt("ServiceTask_Create_Service_Invoice")
      .hasVariables(BillingProcessDataAccessor.WORK_ORDER_ID);
    execute(job());

    assertThat(subProcessInstanceCreateServiceInvoiceProcess).isWaitingAt("ReceiveTask_Retrieve_Service_Invoice_Numbers")
      .hasVariables(BillingProcessDataAccessor.WORK_ORDER_ID);
    assertThat(subProcessInstanceCreateServiceInvoiceProcess).isWaitingFor(MessageEventEnum.MSG_RETRIEVE_SERVICE_INVOICE_NUMBERS.name());

    runtimeService().createMessageCorrelation(MessageEventEnum.MSG_RETRIEVE_SERVICE_INVOICE_NUMBERS.name())
      .processInstanceVariableEquals(CreateServiceInvoiceDataAccessor.SERVICE_INVOICE_TRANSACTION_ID, invoiceTransactionId)
      .setVariable(ProcessVariableConstants.PROCESS_VARIABLE_SERVICE_INVOICE_NUMBER, serviceInvoiceNumber)
      .setVariable(ProcessVariableConstants.PROCESS_VARIABLE_SERVICE_INVOICE_ATTACHMENT_NUMBERS, serviceInvoiceAttachmentNumbers)
      .correlate();

    assertThat(subProcessInstanceCreateServiceInvoiceProcess).isWaitingAt("ServiceTask_Validate_Service_Invoice_Numbers");
    execute(job());

    assertThat(subProcessInstanceCreateServiceInvoiceProcess).isEnded();
  }

  private CostAllocationRO createConsumptionDependentCostAllocationRO() {
    final CostAllocationRO costAllocationRO = new CostAllocationRO();
    costAllocationRO.setType(CostAllocationEnumRO.ENERGY_SERVICE_COST_ALLOCATION);
    final EnergyServiceCostAllocationRO energyServiceCostAllocation = new EnergyServiceCostAllocationRO();
    costAllocationRO.setEnergyServiceCostAllocation(energyServiceCostAllocation);
    final AllocationDefinitionRO consumptionCostAllocation = new AllocationDefinitionRO();
    energyServiceCostAllocation.setConsumptionCostAllocation(consumptionCostAllocation);
    final AllocationKeyRO allocationKey = new AllocationKeyRO();
    allocationKey.setKey("CONSUMPTION_HEAT_COST_ALLOCATORS");
    allocationKey.setType(AllocationKeyEnumRO.CONSUMPTION);
    consumptionCostAllocation.setAllocationKey(allocationKey);
    consumptionCostAllocation.setAllocationKeyUnit(AllocationKeyUnitRO.KWH);
    return costAllocationRO;
  }

}
