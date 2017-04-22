package com.tzg.framework.support.security.csrf;

import com.tzg.tool.support.http.CookieUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * CSRF拦截器。防止CSRF攻击。springmvc拦截器配置:
 * <p>
 * <mvc:interceptors>
 * <bean class="com.tzg.framework.support.security.csrf.CSRFInterceptor"/>
 * </mvc:interceptors>
 * </p>
 *
 * @author 曾林 2016/12/14.
 * @link http://192.168.1.177:3000/%E5%BC%80%E5%8F%91%E6%8C%87%E5%8D%97/%E5%AE%89%E5%85%A8%E8%A7%84%E8%8C%83-%E5%BA%94%E7%94%A8%E7%AF%87
 */
public class CsrfInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger( CsrfInterceptor.class );

    /**
     * 判断请求是否合法，是否存在CSRF攻击。
     * 登录或打开首页时，生成token放入cookie和session中
     * 判断请求cookie、session中的token值是否一致，不一致视为不合法请求
     */
    public boolean preHandle( HttpServletRequest req, HttpServletResponse res, Object handler ) throws Exception {

        // 直接放过http get方法。
        if ( req.getMethod().equalsIgnoreCase( "GET" ) || req.getMethod().equalsIgnoreCase( "POST" ) ) { return true; }

        String reqVal;
        Object sessionVal;
        String csrf = "csrf";    // csrf token名称。无论是请求参数还是session属性

        //从请求参数、http请求头自定义属性、cookie三个地方获取CSRF token定义值，其中任一有值就不继续查找
        {
            reqVal = req.getParameter( csrf );

            if ( StringUtils.isEmpty( reqVal ) ) {
                reqVal = CookieUtil.getVal( req, csrf );
            }

            if ( StringUtils.isEmpty( reqVal ) ) {
                reqVal = req.getHeader( csrf );
            }

        }

        sessionVal = req.getSession().getAttribute( csrf );

        // 判断session中的csrf跟request中的csrf值是否相同，相同则让请求通过
        if ( StringUtils.isEmpty( reqVal ) || StringUtils.isEmpty( sessionVal ) || !reqVal.equals( sessionVal ) ) {
            logger.error( "bad request:{}, csrf token paramVal = [{}], sessionVal = [{}]", req.getRequestURL(), reqVal, sessionVal );
            // A 400 means that the request was malformed. In other words, the data stream sent by the client to the server didn't follow the rules.
            res.sendError( HttpServletResponse.SC_BAD_REQUEST, "非法请求!" );
            return false;
        }

        return true;

    }

    public void postHandle( HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView )
            throws Exception {

    }

    public void afterCompletion( HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex )
            throws Exception {

    }

}
