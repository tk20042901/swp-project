package com.swp.project.listener;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationListener {

    @EventListener
    public void onApplicationStart(@SuppressWarnings("unused") ApplicationReadyEvent event) {
        System.out.println("Application started successfully");
    }
}
