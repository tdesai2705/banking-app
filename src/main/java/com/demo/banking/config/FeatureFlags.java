package com.demo.banking.config;

import io.rollout.flags.RoxFlag;
import io.rollout.flags.RoxInt;
import io.rollout.rox.server.Rox;
import io.rollout.rox.server.RoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Configuration
public class FeatureFlags {
    private static final Logger log = LoggerFactory.getLogger(FeatureFlags.class);

    @Value("${feature.management.key:}")
    private String featureManagementKey;

    // Boolean Feature Flags
    public final RoxFlag enableLoanApprovals = new RoxFlag(false);
    public final RoxFlag enableWireTransfers = new RoxFlag(false);
    public final RoxFlag enablePremiumFeatures = new RoxFlag(false);
    public final RoxFlag maintenanceMode = new RoxFlag(false);
    public final RoxFlag enableTransactionHistory = new RoxFlag(true);

    // Number Feature Flags
    public final RoxInt maxTransferAmount = new RoxInt(10000, new int[]{5000, 10000, 50000, 100000});
    public final RoxInt maxDailyTransactions = new RoxInt(10, new int[]{5, 10, 20, 50});

    @PostConstruct
    public void init() {
        if (featureManagementKey == null || featureManagementKey.trim().isEmpty()) {
            log.warn("Feature Management key not configured. Using default flag values.");
            return;
        }

        try {
            log.info("Initializing Feature Management...");

            // Register flags with CloudBees
            Rox.register("", this);

            // Setup with timeout
            RoxOptions options = new RoxOptions.Builder().build();
            Rox.setup(featureManagementKey, options)
                .get(15, TimeUnit.SECONDS);

            log.info("Feature Management initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Feature Management: {}", e.getMessage());
            log.warn("Using default flag values");
        }
    }
}
