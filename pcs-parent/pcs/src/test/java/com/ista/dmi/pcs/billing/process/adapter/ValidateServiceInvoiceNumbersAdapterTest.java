package com.ista.dmi.pcs.billing.process.adapter;

import com.ista.dmi.common.exception.DMIException;
import com.ista.dmi.pcs.billing.business.contractmanagement.invoice.InvoiceFeedbackMessageBusinessService;
import com.ista.dmi.pcs.billing.domain.contractmanagement.invoice.InvoiceFeedbackMessage;
import com.ista.dmi.pcs.billing.domain.contractmanagement.invoice.InvoiceFeedbackMessageFailureType;
import com.ista.dmi.pcs.billing.domain.contractmanagement.invoice.InvoiceFeedbackMessageType;
import com.ista.dmi.pcs.billing.process.dataaccessor.CreateServiceInvoiceDataAccessor;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ValidateServiceInvoiceNumbersAdapterTest {

  @InjectMocks
  private ValidateServiceInvoiceNumbersAdapter adapter;

  @Mock
  private CreateServiceInvoiceDataAccessor dataAccessorMock;

  @Mock
  private InvoiceFeedbackMessageBusinessService invoiceFeedbackMessageBusinessServiceMock;

  @Mock
  private DelegateExecution executionMock;

  @Test
  public void shouldThrowBpmnErrorWhenAFeedbackTypeExistAndFeedbackMessageFailureIsAfterProcessing() throws Exception {
    // given
    String serviceInvoiceTransactionId = "serviceInvoiceTransactionId";
    List<InvoiceFeedbackMessage> invoiceFeedbackMessages = new ArrayList<>();
    InvoiceFeedbackMessage invoiceFeedbackMessage1 = new InvoiceFeedbackMessage();
    invoiceFeedbackMessage1.setId(1L);
    invoiceFeedbackMessage1.setCode("002");
    invoiceFeedbackMessage1.setInvoiceFeedbackMessageType(InvoiceFeedbackMessageType.ERROR);
    invoiceFeedbackMessage1.setInvoiceFeedbackMessageFailureType(InvoiceFeedbackMessageFailureType.FAILED_AFTER_PROCESSING);
    InvoiceFeedbackMessage invoiceFeedbackMessage2 = new InvoiceFeedbackMessage();
    invoiceFeedbackMessage2.setId(2L);
    invoiceFeedbackMessage2.setCode("026");
    invoiceFeedbackMessage2.setInvoiceFeedbackMessageType(InvoiceFeedbackMessageType.ERROR);
    invoiceFeedbackMessage2.setInvoiceFeedbackMessageFailureType(InvoiceFeedbackMessageFailureType.FAILED_BEFORE_PROCESSING);
    invoiceFeedbackMessages.add(invoiceFeedbackMessage1);
    invoiceFeedbackMessages.add(invoiceFeedbackMessage2);
    when(dataAccessorMock.getServiceInvoiceTransactionId()).thenReturn(serviceInvoiceTransactionId);
    when(invoiceFeedbackMessageBusinessServiceMock.list(serviceInvoiceTransactionId)).thenReturn(invoiceFeedbackMessages);

    // when
    try {
      adapter.execute(executionMock);
    } catch (Exception e) {
      assertThat(e, instanceOf(BpmnError.class));
    }

    // then
    verify(executionMock).removeVariableLocal(CreateServiceInvoiceDataAccessor.SERVICE_INVOICE_CREATION_FAILED_POSITION);
    verify(dataAccessorMock).setServiceInvoiceFeedbackMessageId(invoiceFeedbackMessage1.getId());
    verify(dataAccessorMock).setServiceInvoiceCreationFailedPosition(InvoiceFeedbackMessageFailureType.FAILED_AFTER_PROCESSING.name());
  }

  @Test
  public void shouldThrowBpmnErrorWhenAFeedbackTypeExistAndFeedbackMessageFailureIsBeforeProcessing() throws Exception {
    // given
    String serviceInvoiceTransactionId = "serviceInvoiceTransactionId";
    List<InvoiceFeedbackMessage> invoiceFeedbackMessages = new ArrayList<>();
    InvoiceFeedbackMessage invoiceFeedbackMessage1 = new InvoiceFeedbackMessage();
    invoiceFeedbackMessage1.setId(1L);
    invoiceFeedbackMessage1.setCode("018");
    invoiceFeedbackMessage1.setInvoiceFeedbackMessageType(InvoiceFeedbackMessageType.ERROR);
    invoiceFeedbackMessage1.setInvoiceFeedbackMessageFailureType(InvoiceFeedbackMessageFailureType.FAILED_AFTER_PROCESSING);
    InvoiceFeedbackMessage invoiceFeedbackMessage2 = new InvoiceFeedbackMessage();
    invoiceFeedbackMessage2.setId(2L);
    invoiceFeedbackMessage2.setCode("251");
    invoiceFeedbackMessage2.setInvoiceFeedbackMessageType(InvoiceFeedbackMessageType.ERROR);
    invoiceFeedbackMessage2.setInvoiceFeedbackMessageFailureType(InvoiceFeedbackMessageFailureType.FAILED_BEFORE_PROCESSING);
    invoiceFeedbackMessages.add(invoiceFeedbackMessage1);
    invoiceFeedbackMessages.add(invoiceFeedbackMessage2);
    when(dataAccessorMock.getServiceInvoiceTransactionId()).thenReturn(serviceInvoiceTransactionId);
    when(invoiceFeedbackMessageBusinessServiceMock.list(serviceInvoiceTransactionId)).thenReturn(invoiceFeedbackMessages);

    // when
    try {
      adapter.execute(executionMock);
    } catch (Exception e) {
      assertThat(e, instanceOf(BpmnError.class));
    }

    // then
    verify(executionMock).removeVariableLocal(CreateServiceInvoiceDataAccessor.SERVICE_INVOICE_CREATION_FAILED_POSITION);
    verify(dataAccessorMock).setServiceInvoiceFeedbackMessageId(invoiceFeedbackMessage2.getId());
    verify(dataAccessorMock).setServiceInvoiceCreationFailedPosition(InvoiceFeedbackMessageFailureType.FAILED_BEFORE_PROCESSING.name());
  }

  @Test
  public void shouldPassSuccessfullyWhenNoFeedbackTypeExist() throws Exception {
    // given
    String serviceInvoiceTransactionId = "serviceInvoiceTransactionId";
    when(dataAccessorMock.getServiceInvoiceTransactionId()).thenReturn(serviceInvoiceTransactionId);
    when(invoiceFeedbackMessageBusinessServiceMock.list(serviceInvoiceTransactionId)).thenReturn(Collections.emptyList());

    // when
    adapter.execute(executionMock);

    // then
    verify(executionMock).removeVariableLocal(CreateServiceInvoiceDataAccessor.SERVICE_INVOICE_CREATION_FAILED_POSITION);
    verify(dataAccessorMock, never()).setServiceInvoiceFeedbackMessageId(anyLong());
    verify(dataAccessorMock, never()).setServiceInvoiceCreationFailedPosition(anyString());
    verify(dataAccessorMock).setProvideServiceInvoice(true);
  }

  @Test
  public void shouldThrowADMIExceptionWhenAnUnknownErrorCodeDelivered() throws Exception {
    // given
    String serviceInvoiceTransactionId = "serviceInvoiceTransactionId";
    List<InvoiceFeedbackMessage> invoiceFeedbackMessages = new ArrayList<>();
    InvoiceFeedbackMessage invoiceFeedbackMessage1 = new InvoiceFeedbackMessage();
    invoiceFeedbackMessage1.setId(1L);
    invoiceFeedbackMessage1.setCode("018");
    invoiceFeedbackMessage1.setInvoiceFeedbackMessageType(InvoiceFeedbackMessageType.INFORMATION);
    invoiceFeedbackMessage1.setInvoiceFeedbackMessageFailureType(InvoiceFeedbackMessageFailureType.FAILED_BEFORE_PROCESSING);
    InvoiceFeedbackMessage invoiceFeedbackMessage2 = new InvoiceFeedbackMessage();
    invoiceFeedbackMessage2.setId(2L);
    invoiceFeedbackMessage2.setCode("999");
    invoiceFeedbackMessage2.setInvoiceFeedbackMessageType(InvoiceFeedbackMessageType.ERROR);
    invoiceFeedbackMessage2.setInvoiceFeedbackMessageFailureType(InvoiceFeedbackMessageFailureType.FAILED_AFTER_PROCESSING);
    invoiceFeedbackMessages.add(invoiceFeedbackMessage1);
    invoiceFeedbackMessages.add(invoiceFeedbackMessage2);
    when(dataAccessorMock.getServiceInvoiceTransactionId()).thenReturn(serviceInvoiceTransactionId);
    when(invoiceFeedbackMessageBusinessServiceMock.list(serviceInvoiceTransactionId)).thenReturn(invoiceFeedbackMessages);

    // when
    try {
      adapter.execute(executionMock);
    } catch (Exception e) {
      assertThat(e, instanceOf(DMIException.class));
    }

    // then
    verify(executionMock).removeVariableLocal(CreateServiceInvoiceDataAccessor.SERVICE_INVOICE_CREATION_FAILED_POSITION);
    verify(dataAccessorMock, never()).setServiceInvoiceFeedbackMessageId(anyLong());
    verify(dataAccessorMock, never()).setServiceInvoiceCreationFailedPosition(anyString());
  }

  @Test
  public void shouldSetProvideServiceInvoiceToFalseWhenAFeedbackTypeExistAndFeedbackMessageIsInformation254() throws Exception {
    // given
    String serviceInvoiceTransactionId = "serviceInvoiceTransactionId";
    List<InvoiceFeedbackMessage> invoiceFeedbackMessages = new ArrayList<>();
    InvoiceFeedbackMessage invoiceFeedbackMessage = new InvoiceFeedbackMessage();
    invoiceFeedbackMessage.setId(1L);
    invoiceFeedbackMessage.setCode("254");
    invoiceFeedbackMessage.setInvoiceFeedbackMessageType(InvoiceFeedbackMessageType.INFORMATION);
    invoiceFeedbackMessages.add(invoiceFeedbackMessage);
    when(dataAccessorMock.getServiceInvoiceTransactionId()).thenReturn(serviceInvoiceTransactionId);
    when(invoiceFeedbackMessageBusinessServiceMock.list(serviceInvoiceTransactionId)).thenReturn(invoiceFeedbackMessages);

    // when
    try {
      adapter.execute(executionMock);
    } catch (Exception e) {
      assertThat(e, instanceOf(BpmnError.class));
    }

    // then
    verify(executionMock).removeVariableLocal(CreateServiceInvoiceDataAccessor.SERVICE_INVOICE_CREATION_FAILED_POSITION);
    verify(dataAccessorMock).setServiceInvoiceFeedbackMessageId(invoiceFeedbackMessage.getId());
    verify(dataAccessorMock).setProvideServiceInvoice(false);
  }

  @Test
  public void shouldSetProvideServiceInvoiceToFalseWhenAFeedbackTypeExistAndFeedbackMessageIsInformation074() throws Exception {
    // given
    String serviceInvoiceTransactionId = "serviceInvoiceTransactionId";
    List<InvoiceFeedbackMessage> invoiceFeedbackMessages = new ArrayList<>();
    InvoiceFeedbackMessage invoiceFeedbackMessage = new InvoiceFeedbackMessage();
    invoiceFeedbackMessage.setId(1L);
    invoiceFeedbackMessage.setCode("074");
    invoiceFeedbackMessage.setInvoiceFeedbackMessageType(InvoiceFeedbackMessageType.INFORMATION);
    invoiceFeedbackMessages.add(invoiceFeedbackMessage);
    when(dataAccessorMock.getServiceInvoiceTransactionId()).thenReturn(serviceInvoiceTransactionId);
    when(invoiceFeedbackMessageBusinessServiceMock.list(serviceInvoiceTransactionId)).thenReturn(invoiceFeedbackMessages);

    // when
    try {
      adapter.execute(executionMock);
    } catch (Exception e) {
      assertThat(e, instanceOf(BpmnError.class));
    }

    // then
    verify(executionMock).removeVariableLocal(CreateServiceInvoiceDataAccessor.SERVICE_INVOICE_CREATION_FAILED_POSITION);
    verify(dataAccessorMock).setServiceInvoiceFeedbackMessageId(invoiceFeedbackMessage.getId());
    verify(dataAccessorMock).setProvideServiceInvoice(false);
  }

  @Test
  public void shouldSetProvideServiceInvoiceToTrueWhenAFeedbackTypeExistAndFeedbackMessageIsAnUnexpectedInformation() throws Exception {
    // given
    String serviceInvoiceTransactionId = "serviceInvoiceTransactionId";
    List<InvoiceFeedbackMessage> invoiceFeedbackMessages = new ArrayList<>();
    InvoiceFeedbackMessage invoiceFeedbackMessage = new InvoiceFeedbackMessage();
    invoiceFeedbackMessage.setId(1L);
    invoiceFeedbackMessage.setCode("4711");
    invoiceFeedbackMessage.setInvoiceFeedbackMessageType(InvoiceFeedbackMessageType.INFORMATION);
    invoiceFeedbackMessages.add(invoiceFeedbackMessage);
    when(dataAccessorMock.getServiceInvoiceTransactionId()).thenReturn(serviceInvoiceTransactionId);
    when(invoiceFeedbackMessageBusinessServiceMock.list(serviceInvoiceTransactionId)).thenReturn(invoiceFeedbackMessages);

    // when
    try {
      adapter.execute(executionMock);
    } catch (Exception e) {
      assertThat(e, instanceOf(BpmnError.class));
    }

    // then
    verify(executionMock).removeVariableLocal(CreateServiceInvoiceDataAccessor.SERVICE_INVOICE_CREATION_FAILED_POSITION);
    verify(dataAccessorMock, never()).setServiceInvoiceFeedbackMessageId(anyLong());
    verify(dataAccessorMock).setProvideServiceInvoice(true);
  }
}