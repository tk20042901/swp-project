package com.swp.project.listener;
import com.swp.project.listener.event.UserDisabledEvent;
import org.springframework.context.event.EventListener;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;

@Component
public class UserDisabledEventListener {

    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    public UserDisabledEventListener(FindByIndexNameSessionRepository<? extends Session> sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @EventListener
    public void onUserDisabledEvent(UserDisabledEvent event) {
        String username = event.username();
        sessionRepository.findByPrincipalName(username).values()
                .forEach(session -> sessionRepository.deleteById(session.getId()));
    }
}
