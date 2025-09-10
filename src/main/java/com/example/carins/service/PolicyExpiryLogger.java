package com.example.carins.service;

import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.InsurancePolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class PolicyExpiryLogger {

    private static final Logger logger = LoggerFactory.getLogger(PolicyExpiryLogger.class);

    private final InsurancePolicyRepository policyRepository;

    public PolicyExpiryLogger(InsurancePolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    @Scheduled(cron = "0 0 0 * * ?") // Run every day at the start of the day
    @Transactional
    public void logExpiredPolicies() {
        try {
            LocalDate today = LocalDate.now();
            List<InsurancePolicy> expiredPolicies = policyRepository.findExpiredNotNotified(today);
            for (InsurancePolicy policy : expiredPolicies) {
                logger.info("Policy {} for car {} expired on {}",
                        policy.getId(),
                        policy.getCar().getId(),
                        policy.getEndDate());
                policy.setExpiryNotified(true);
                policyRepository.save(policy);
            }
        } catch (Exception e) {
            logger.error("Error processing expired policies", e);
        }
    }
}