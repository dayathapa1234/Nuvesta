package com.nuvesta.market_data_service.events;

import org.springframework.context.ApplicationEvent;

public class SymbolCatalogLoadedEvent extends ApplicationEvent {

    public SymbolCatalogLoadedEvent(Object source) {
        super(source);
    }
}
