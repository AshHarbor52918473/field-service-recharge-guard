package cc.infrai.fieldservice;

import java.math.BigDecimal;
import java.util.List;

public final class RechargeDecisionTest {
    public static void main(String[] args) {
        WorkOrder dispatched = new WorkOrder("WO-1", List.of("photo-1"), "DISPATCHED", "Call customer", new BigDecimal("10.00"));
        RechargeDecision decision = RechargeDecision.from(dispatched, new BigDecimal("10.00"));
        if (!decision.rechargeRequired() || !decision.notifyDispatcher()) {
            throw new AssertionError("A dispatched work order at the trigger must arm recharge and notify dispatch.");
        }
        WorkOrder closed = new WorkOrder("WO-2", List.of("photo-2"), "CLOSED", "Completed", new BigDecimal("1.00"));
        if (RechargeDecision.from(closed, new BigDecimal("10.00")).rechargeRequired()) {
            throw new AssertionError("Closed work must not arm recharge.");
        }
        System.out.println("RechargeDecisionTest passed");
    }
}
