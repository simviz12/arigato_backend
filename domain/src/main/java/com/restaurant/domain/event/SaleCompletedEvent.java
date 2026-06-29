package com.restaurant.domain.event;

import com.restaurant.domain.model.Sale;
import com.restaurant.domain.model.SaleLine;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class SaleCompletedEvent extends ApplicationEvent {

    private final Sale sale;
    private final List<SaleLine> lines;

    public SaleCompletedEvent(Object source, Sale sale, List<SaleLine> lines) {
        super(source);
        this.sale = sale;
        this.lines = lines;
    }
}
