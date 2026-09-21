package cc.infrai.fieldservice;

import java.math.BigDecimal;
import java.util.List;

/** The local state that makes the billing decision explainable. */
public record WorkOrder(
    String workOrderId,
    List<String> photoReferences,
    String dispatchStatus,
    String technicianFollowUp,
    BigDecimal balanceUsd) {
}
