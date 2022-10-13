package com.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.reggie.common.BaseContext;
import com.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否已经完成登录
 */
@WebFilter(filterName = "loginCheckFiler",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    // 路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;

        /**
         * - 获取本次请求的URI
         * - 判断本次请求是否需要处理
         * - 如果不需要处理，则直接放行
         * - 判断登录状态，如果已登录，则直接放行
         * - 如果未登录则返回未登录结果
         */

        // 获取请求url
        String requestURI = request.getRequestURI();
        log.info("拦截到{}请求",requestURI);

        // 不需要处理的请求路径，包括登录、登出路径、静态资源路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/sendMsg", // 移动端发送短信
                "/user/login" // 移动端登录
        };

        // 判断本次请求是否需要判断
        boolean check = check(urls, requestURI);

        // 不需要处理，则直接放行
        if (check){
            log.info("不需要处理，放行");
            filterChain.doFilter(request,response);
            return;
        }

        // 管理系统用户
        // 拦截并判断是否登录，若登录了则放行，未登录则跳转页面
        // 判断是否登录是依靠判断 session 中是否有 user 这个属性
        if (request.getSession().getAttribute("employee") != null){
            log.info("用户已登录，放行");
            BaseContext.setCurrentId((Long) request.getSession().getAttribute("employee"));
            filterChain.doFilter(request,response);
            return;
        } else {
            // 通过输出流将错误信息写入响应体
            log.info("用户未登录，不放行，跳转页面");
            response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        }


    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls, String requestURI){
        for (String url : urls){
            // 使用路径匹配器，比较请求的路径和 urls 里的路径是否相同
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match){
                return true;
            }
        }
        return false;
    }
}
