package com.recruit.recruitmentapplication.security;

import com.recruit.recruitmentapplication.dto.SessionUser;
import com.recruit.recruitmentapplication.entity.Role;
import com.recruit.recruitmentapplication.util.SessionConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        SessionUser loggedInUser = session == null
                ? null
                : (SessionUser) session.getAttribute(SessionConstants.LOGGED_IN_USER);

        if (loggedInUser == null && isPublicJobReadRequest(request)) {
            return true;
        }

        if (loggedInUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return false;
        }

        if (request.getRequestURI().startsWith(request.getContextPath() + "/admin")
                && !Role.ADMIN.equals(loggedInUser.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/error/403");
            return false;
        }

        // SCR-17: Interviewer được xem (read-only) chi tiết đơn ứng tuyển và tải CV
        // cho application được assign; quyền chi tiết (đúng application hay không)
        // do ApplicationService.findDetailForViewer kiểm tra ở tầng service.
        if (isInterviewerApplicationReadRequest(request) && Role.INTERVIEWER.equals(loggedInUser.getRoleName())) {
            return true;
        }

        if (request.getRequestURI().startsWith(request.getContextPath() + "/manage")
                && !canManageRecruitment(loggedInUser)) {
            response.sendRedirect(request.getContextPath() + "/error/403");
            return false;
        }

        if (request.getRequestURI().startsWith(request.getContextPath() + "/interviews")
                && !Role.INTERVIEWER.equals(loggedInUser.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/error/403");
            return false;
        }

        if (request.getRequestURI().startsWith(request.getContextPath() + "/hr")
                && !canManageRecruitment(loggedInUser)) {
            response.sendRedirect(request.getContextPath() + "/error/403");
            return false;
        }

        if (isCompanyWriteRequest(request) && !canManageCompanies(loggedInUser)) {
            response.sendRedirect(request.getContextPath() + "/error/403");
            return false;
        }
        if (isJobWriteRequest(request) && !canManageCompanies(loggedInUser)) {
            response.sendRedirect(request.getContextPath() + "/error/403");
            return false;
        }
        if (isCandidateAccessDenied(request, loggedInUser)) {
            response.sendRedirect(request.getContextPath() + "/error/403");
            return false;
        }
        if (request.getRequestURI().startsWith(request.getContextPath() + "/my-applications")
                && !Role.CANDIDATE.equals(loggedInUser.getRoleName())) {
            response.sendRedirect(request.getContextPath() + "/error/403");
            return false;
        }
        return true;
    }

    private boolean isCompanyWriteRequest(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (!path.startsWith("/companies")) {
            return false;
        }
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        return "/companies/new".equals(path)
                || (path.startsWith("/companies/") && path.endsWith("/edit"));
    }

    private boolean canManageCompanies(SessionUser user) {
        return canManageRecruitment(user);
    }

    private boolean canManageRecruitment(SessionUser user) {
        return Role.ADMIN.equals(user.getRoleName()) || Role.HR_MANAGER.equals(user.getRoleName());
    }

    private boolean isJobWriteRequest(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (!path.startsWith("/jobs")) return false;
        if (path.matches("/jobs/\\d+/apply")) return false;
        if (!"GET".equalsIgnoreCase(request.getMethod())) return true;
        return "/jobs/new".equals(path) || (path.startsWith("/jobs/") && path.endsWith("/edit"));
    }

    private boolean isInterviewerApplicationReadRequest(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.matches("/manage/applications/\\d+")
                || path.matches("/manage/applications/\\d+/documents/\\d+/download");
    }

    private boolean isPublicJobReadRequest(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return "/jobs".equals(path) || path.matches("/jobs/\\d+");
    }

    private boolean isCandidateAccessDenied(HttpServletRequest request, SessionUser user) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (!path.startsWith("/candidates")) return false;
        if (path.equals("/candidates/me")) return !Role.CANDIDATE.equals(user.getRoleName());
        return !canManageRecruitment(user);
    }
}
