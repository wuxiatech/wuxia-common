package cn.wuxia.common.cached.session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import cn.wuxia.common.util.ServletUtils;

/**
 * rewrite HttpServletRequest,change getSession method,use memcached session
 * implement
 * 
 * @author songlin.li
 * @version 1.0.0
 */
public class SessionHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private HttpServletResponse response;

    private MemcachedHttpSession httpSession;

    private MemcachedSessionManager sessionManager;

    private RequestEventSubject requestEventSubject;

    public SessionHttpServletRequestWrapper(HttpServletRequest request, HttpServletResponse response,
            MemcachedSessionManager sessionManager, RequestEventSubject requestEventSubject) {
        super(request);
        this.response = response;
        this.sessionManager = sessionManager;
        this.requestEventSubject = requestEventSubject;
    }

    @Override
    public HttpSession getSession(boolean create) {
        if (httpSession != null && httpSession.expired == false)
            return httpSession;
        httpSession = sessionManager.createSession(this, response, requestEventSubject, create);
        return httpSession;
    }

    @Override
    public HttpSession getSession() {
        return getSession(true);
    }

    @Override
    public String getRemoteAddr() {
        return ServletUtils.getRemoteIP((HttpServletRequest) super.getRequest());
    }

}
