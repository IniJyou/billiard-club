package com.club.common;

import com.club.dto.LoginUser;
import jakarta.servlet.http.HttpSession;

public final class SessionUtils {

    private SessionUtils() {
    }

    public static LoginUser currentUser(HttpSession session) {
        LoginUser user = session == null ? null : (LoginUser) session.getAttribute(SessionKeys.LOGIN_USER);
        if (user == null) {
            throw new BusinessException(401, "登录已失效，请重新登录");
        }
        return user;
    }
}
