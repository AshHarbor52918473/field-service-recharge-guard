package cc.infrai.fieldservice;

import java.math.BigDecimal;

public record RechargeDecision(boolean rechargeRequired, boolean notifyDispatcher, String reason) {
    public static RechargeDecision from(WorkOrder order, BigDecimal triggerBalance) {
        boolean pendingFieldWork = !order.photoReferences().isEmpty()
            && "DISPATCHED".equals(order.dispatchStatus())
            && !order.technicianFollowUp().isBlank();
        boolean rechargeRequired = pendingFieldWork && order.balanceUsd().compareTo(triggerBalance) <= 0;
        String reason = rechargeRequired
            ? "Dispatched work has photos and a technician follow-up below the recharge trigger."
            : "No recharge action is required for this work order.";
        return new RechargeDecision(rechargeRequired, rechargeRequired, reason);
    }
}
