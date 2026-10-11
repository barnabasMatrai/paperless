package at.technikum.paperless.security;

import at.technikum.paperless.service.IReminderService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("reminderSecurity")
@AllArgsConstructor
public class ReminderSecurity {

    private final IReminderService reminderService;

    public boolean isOwner(Long reminderId, Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());

        return reminderService.findById(reminderId)
                .getDocument()
                .getUser()
                .getId()
                .equals(userId);
    }
}
