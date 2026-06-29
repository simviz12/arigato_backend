package com.restaurant.application.audit;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@EnableAspectJAutoProxy
public class AuditConfig {
}
