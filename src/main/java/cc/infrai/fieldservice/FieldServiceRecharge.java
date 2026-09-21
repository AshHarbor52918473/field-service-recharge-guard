package cc.infrai.fieldservice;

import java.math.BigDecimal;
import java.util.List;

public final class FieldServiceRecharge {
    public static void main(String[] args) throws Exception {
        FieldServiceSettings settings = FieldServiceSettings.fromEnvironment();

        WorkOrder order = new WorkOrder("WO-1042", List.of("photo-receipt-1042"), "DISPATCHED",
            "Technician will confirm the repair window.", new BigDecimal("8.00"));
        BigDecimal trigger = new BigDecimal("10.00");
        RechargeDecision decision = RechargeDecision.from(order, trigger);
        System.out.println(decision.reason());
        if (!decision.rechargeRequired()) return;

        InfraiClient infrai = new InfraiClient(settings.baseUrl(), settings.apiKey());
        infrai.configureAutoRecharge(trigger.toPlainString(), "25.00");
        String result = infrai.sendRechargeNotice(settings.dispatchEmail(), "Recharge armed for " + order.workOrderId(),
            "Dispatch remains active. Technician follow-up: " + order.technicianFollowUp());
        System.out.println("Recharge configuration and notification accepted: " + result);
    }
}
