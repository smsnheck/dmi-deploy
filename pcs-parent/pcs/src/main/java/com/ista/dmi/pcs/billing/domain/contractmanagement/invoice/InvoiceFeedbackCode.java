package com.ista.dmi.pcs.billing.domain.contractmanagement.invoice;

import com.ista.dmi.pcs.billing.domain.ViolationMessage;
import org.apache.commons.lang3.EnumUtils;

import static com.ista.dmi.pcs.billing.domain.ViolationMessage.Key.*;

/**
 * Feedback codes for handling of invoices.
 */
public enum InvoiceFeedbackCode {
  FEEDBACK_CODE_002(InvoiceFeedbackCodeAction.RETRY, CM_LOCK_ERROR_002),
  FEEDBACK_CODE_018(InvoiceFeedbackCodeAction.USER_TASK, CM_INTERFACE_ERROR_018),
  FEEDBACK_CODE_026(InvoiceFeedbackCodeAction.RETRY, CM_LOCK_ERROR_026),
  FEEDBACK_CODE_064(InvoiceFeedbackCodeAction.RETRY, CM_LOCK_ERROR_064),
  FEEDBACK_CODE_067(InvoiceFeedbackCodeAction.USER_TASK, CM_INTERFACE_ERROR_067),
  FEEDBACK_CODE_074(InvoiceFeedbackCodeAction.USER_TASK, CM_INTERFACE_INFORMATION_074),
  FEEDBACK_CODE_103(InvoiceFeedbackCodeAction.USER_TASK, CM_LOCK_ERROR_103),
  FEEDBACK_CODE_251(InvoiceFeedbackCodeAction.RETRY, CM_INTERFACE_ERROR_251),
  FEEDBACK_CODE_254(InvoiceFeedbackCodeAction.USER_TASK, CM_INTERFACE_INFORMATION_254);

  private static final String FEEDBACK_CODE_PREFIX = "FEEDBACK_CODE_";
  private InvoiceFeedbackCodeAction action;
  private ViolationMessage.Key violationMessageKey;

  InvoiceFeedbackCode(InvoiceFeedbackCodeAction action, ViolationMessage.Key violationMessageKey) {
    this.action = action;
    this.violationMessageKey = violationMessageKey;
  }

  /**
   * Getter.
   * @return InvoiceFeedbackCodeAction
   */
  public InvoiceFeedbackCodeAction getAction() {
    return action;
  }

  public ViolationMessage.Key getViolationMessageKey() {
    return violationMessageKey;
  }

  /**
   * GetEnum from value, if parsed enum exists.
   * @param value value
   * @return enum
   */
  public static InvoiceFeedbackCode getEnum(String value) {
    final String enumName = FEEDBACK_CODE_PREFIX + value;
    if (EnumUtils.isValidEnum(InvoiceFeedbackCode.class, enumName)) {
      return InvoiceFeedbackCode.valueOf(enumName);
    }
    return null;
  }

  /**
   * Enum InvoiceFeedbackCodeAction.
   */
  public enum InvoiceFeedbackCodeAction {
    RETRY,
    USER_TASK
  }
}
