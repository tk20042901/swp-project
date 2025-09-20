package com.swp.project.listener;
import com.swp.project.listener.event.UserDisabledEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserDisabledEventListener {

    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    @EventListener
    public void onUserDisabledEvent(UserDisabledEvent event) {
        sessionRepository.findByPrincipalName(event.email()).values()
                .forEach(session -> sessionRepository.deleteById(session.getId()));
    }
}
