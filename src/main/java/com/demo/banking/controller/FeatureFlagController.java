package com.demo.banking.controller;

import com.demo.banking.config.FeatureFlags;
import io.rollout.context.ContextBuilder;
import io.rollout.context.ContextInterface;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/flags")
public class FeatureFlagController {
    private final FeatureFlags flags;

    public FeatureFlagController(FeatureFlags flags) {
        this.flags = flags;
    }

    @GetMapping
    public Map<String, Object> getFlags(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String accountType) {

        // Create context for targeted flag evaluation
        ContextBuilder contextBuilder = new ContextBuilder();
        if (email != null) {
            contextBuilder.setCustomStringProperty("email", email);
        }
        if (accountType != null) {
            contextBuilder.setCustomStringProperty("accountType", accountType);
        }
        ContextInterface context = contextBuilder.build();

        // Evaluate flags with context
        Map<String, Object> flagValues = new HashMap<>();
        flagValues.put("enableLoanApprovals", flags.enableLoanApprovals.isEnabled(context));
        flagValues.put("enableWireTransfers", flags.enableWireTransfers.isEnabled(context));
        flagValues.put("enablePremiumFeatures", flags.enablePremiumFeatures.isEnabled(context));
        flagValues.put("maintenanceMode", flags.maintenanceMode.isEnabled(context));
        flagValues.put("enableTransactionHistory", flags.enableTransactionHistory.isEnabled(context));
        flagValues.put("maxTransferAmount", flags.maxTransferAmount.getValue(context));
        flagValues.put("maxDailyTransactions", flags.maxDailyTransactions.getValue(context));

        return flagValues;
    }
}
