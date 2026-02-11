package com.photo.listener;

import com.photo.util.SessionManager;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * 清理过期 Session 映射，避免内存残留
 */
@Component
public class SessionCleanupListener implements HttpSessionListener {
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        SessionManager.removeMappings(se.getSession());
    }
}
