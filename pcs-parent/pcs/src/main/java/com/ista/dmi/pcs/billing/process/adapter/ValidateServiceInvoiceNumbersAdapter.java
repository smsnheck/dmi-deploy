package com.ista.dmi.pcs.billing.process.adapter;

import com.ista.dmi.common.exception.DMIExceptionBuilder;
import com.ista.dmi.pcs.billing.business.contractmanagement.invoice.InvoiceFeedbackMessageBusinessService;
import com.ista.dmi.pcs.billing.domain.BpmnErrorCode;
import com.ista.dmi.pcs.billing.domain.contractmanagement.invoice.InvoiceFeedbackCode;
import com.ista.dmi.pcs.billing.domain.contractmanagement.invoice.InvoiceFeedbackMessage;
import com.ista.dmi.pcs.billing.domain.contractmanagement.invoice.InvoiceFeedbackMessageType;
import com.ista.dmi.pcs.billing.process.dataaccessor.CreateServiceInvoiceDataAccessor;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Optional;

/**
 * Validate service invoice numbers adapter.
 */
@Named
public class ValidateServiceInvoiceNumbersAdapter implements JavaDelegate {

  @Inject
  private CreateServiceInvoiceDataAccessor dataAccessor;

  @Inject
  private InvoiceFeedbackMessageBusinessService invoiceFeedbackMessageBusinessService;

  @Override
  public void execute(DelegateExecution delegateExecution) throws Exception {

    delegateExecution.removeVariableLocal(CreateServiceInvoiceDataAccessor.SERVICE_INVOICE_CREATION_FAILED_POSITION);

    String serviceInvoiceTransactionId = dataAccessor.getServiceInvoiceTransactionId();
    List<InvoiceFeedbackMessage> invoiceFeedbackMessages = invoiceFeedbackMessageBusinessService.list(serviceInvoiceTransactionId);
    boolean provideServiceInvoice = true;

    if (invoiceFeedbackMessages != null && !invoiceFeedbackMessages.isEmpty()) {
      getFirstErrorForAction(invoiceFeedbackMessages, InvoiceFeedbackCode.InvoiceFeedbackCodeAction.RETRY)
        .ifPresent(invoiceFeedbackMessage -> {
            dataAccessor.setServiceInvoiceFeedbackMessageId(invoiceFeedbackMessage.getId());
            dataAccessor.setServiceInvoiceCreationFailedPosition(invoiceFeedbackMessage.getInvoiceFeedbackMessageFailureType().name());
            throw new BpmnError(BpmnErrorCode.INVALID_SERVICE_INVOICE_NUMBERS.name());
          }
        );

      getFirstErrorForAction(invoiceFeedbackMessages, InvoiceFeedbackCode.InvoiceFeedbackCodeAction.USER_TASK)
        .ifPresent(invoiceFeedbackMessage -> {
            dataAccessor.setServiceInvoiceFeedbackMessageId(invoiceFeedbackMessage.getId());
            dataAccessor.setServiceInvoiceCreationFailedPosition(invoiceFeedbackMessage.getInvoiceFeedbackMessageFailureType().name());
            throw new BpmnError(BpmnErrorCode.INVALID_SERVICE_INVOICE_NUMBERS.name());
          }
        );

      Optional<InvoiceFeedbackMessage> result = invoiceFeedbackMessages.stream()
        .filter(invoiceFeedbackMessage -> InvoiceFeedbackMessageType.ERROR.equals(invoiceFeedbackMessage.getInvoiceFeedbackMessageType()))
        .findFirst();
      if (result.isPresent()) {
        throw DMIExceptionBuilder.aDMIException()
          .withMessageTitle("Error from SAP on service invoice numbers validation")
          .withObjectForJsonString(result.get())
          .build();
      }

      provideServiceInvoice = verifyServiceInvoiceProvisioning(invoiceFeedbackMessages);
    }
    dataAccessor.setProvideServiceInvoice(provideServiceInvoice);
  }

  /**
   * Filter for first error.
   *
   * @param invoiceFeedbackMessages invoiceFeedbackMessages
   * @param action action
   * @return InvoiceFeedbackMessage
   */
  private Optional<InvoiceFeedbackMessage> getFirstErrorForAction(List<InvoiceFeedbackMessage> invoiceFeedbackMessages, InvoiceFeedbackCode.InvoiceFeedbackCodeAction action) {
    return invoiceFeedbackMessages.stream()
      .filter(invoiceFeedbackMessage -> InvoiceFeedbackMessageType.ERROR.equals(invoiceFeedbackMessage.getInvoiceFeedbackMessageType()))
      .filter(invoiceFeedbackMessage -> InvoiceFeedbackCode.getEnum(invoiceFeedbackMessage.getCode()) != null)
      .filter(invoiceFeedbackMessage -> InvoiceFeedbackCode.getEnum(invoiceFeedbackMessage.getCode()).getAction().equals(action))
      .findFirst();
  }

  private boolean verifyServiceInvoiceProvisioning(List<InvoiceFeedbackMessage> invoiceFeedbackMessages) {
    boolean provideServiceInvoice = true;

    Optional<InvoiceFeedbackMessage> infoFeedbackMessageOptional = invoiceFeedbackMessages.stream()
      .filter(invoiceFeedbackMessage -> InvoiceFeedbackMessageType.INFORMATION.equals(invoiceFeedbackMessage.getInvoiceFeedbackMessageType()))
      .filter(invoiceFeedbackMessage -> InvoiceFeedbackCode.FEEDBACK_CODE_254.equals(InvoiceFeedbackCode.getEnum(invoiceFeedbackMessage.getCode()))
        || InvoiceFeedbackCode.FEEDBACK_CODE_074.equals(InvoiceFeedbackCode.getEnum(invoiceFeedbackMessage.getCode())))
      .findFirst();

    if (infoFeedbackMessageOptional.isPresent()) {
      provideServiceInvoice = false;
      dataAccessor.setServiceInvoiceFeedbackMessageId(infoFeedbackMessageOptional.get().getId());
    }

    return provideServiceInvoice;
  }
}
