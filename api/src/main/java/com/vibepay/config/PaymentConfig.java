package com.vibepay.config;

import com.vibepay.strategy.InicisPaymentStrategy;
import com.vibepay.strategy.PaymentStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 결제 관련 설정
 * 결제 전략 Bean 등록 및 관리
 */
@Configuration
public class PaymentConfig {

    /**
     * 결제 전략 맵 생성
     * PG사별 전략을 Map으로 관리하여 PaymentService에서 사용
     */
    @Bean
    public Map<String, PaymentStrategy> paymentStrategies(InicisPaymentStrategy inicisPaymentStrategy) {
        Map<String, PaymentStrategy> strategies = new HashMap<>();
        strategies.put("inicisPaymentStrategy", inicisPaymentStrategy);
        // 향후 다른 PG사 추가 시 여기에 등록
        // strategies.put("tossPaymentStrategy", tossPaymentStrategy);
        return strategies;
    }
}