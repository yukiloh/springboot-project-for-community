package xyz.murasakichigo.community.intercepor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import xyz.murasakichigo.community.dto.CommunityUser;
import xyz.murasakichigo.community.mapper.IUserMapper;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*session拦截器，通过重写三个拦截方法实现拦截器*/
@Component  /*将session拦截器交由spring管理*/
public class SessionInterceptor implements HandlerInterceptor {

    @Autowired
    private IUserMapper userMapper;


    /*预处理拦截内容，返回布尔值*/
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        System.out.println("checking cookie");
        checkCookie(request);
        return true;
    }

    /*后处理*/
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        System.out.println("post handle");
    }

    /*返回后处理*/
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        System.out.println("after completion");


    }

    /*检查cookie，有则给予commUser*/
    private void checkCookie(HttpServletRequest request){
        /*判断是否携带token的cookie,如果有就判断是否匹配数据库的token,一致则登陆*/
        Cookie[] cookies = request.getCookies();
        /*如果存在cookies,遍历获取名为token的cookie,并通过此cookie查询sql获取user信息*/
        if (cookies != null && cookies.length != 0) {
            for (Cookie c : cookies) {
                String token = c.getName();
                if ("token".equals(token)) {
                    CommunityUser communityUser = userMapper.findUserByToken(c.getValue());
                    if (communityUser != null) {
                        /*与request.setAttribute不同,r.g.s可以在多个页面、重定向后保留session*/
                        request.getSession().setAttribute("communityUser", communityUser);  /*所以返回前端的是communityUser*/
                        break;  /*找到则停止循环*/
                    }
                }
            }
        }
    }



}