package com.restaurant.application.dto.command;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class RegisterPurchaseCommand {
    private UUID productId;
    private UUID distributorId;
    private BigDecimal quantity;
    private BigDecimal totalPricePesos;
}
