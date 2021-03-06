package xyz.murasakichigo.community.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import xyz.murasakichigo.community.model.AccessToken;
import xyz.murasakichigo.community.model.CommunityUser;
import xyz.murasakichigo.community.model.GithubUser;
import xyz.murasakichigo.community.mapper.IUserMapper;
import xyz.murasakichigo.community.provider.GithubProvider;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.UUID;

/*授权登陆的控制器*/
@Controller
public class AuthorizeController {


    private final GithubProvider githubProvider;
    private final AccessToken accessToken;
    private final IUserMapper userMapper;

    public AuthorizeController(AccessToken accessToken, GithubProvider githubProvider, IUserMapper userMapper) {
        this.accessToken = accessToken;
        this.githubProvider = githubProvider;
        this.userMapper = userMapper;
    }

    /*用于验证账户，成功后会创建/更新本地数据库的Token和登录时间*/
    @GetMapping("/callback")
    public String callback(@RequestParam(name = "code") String code,          /*需要交换访问令牌的临时用户*/
                           @RequestParam(name = "state") String state      /*本地服务器生成的匹配验证码*/
                           ,HttpServletRequest request) {
        /*用于验证state；应置于拦截器内*/
//        String localState = "1";
//        if (localState.equals(state)) {System.out.println("state OK!");}

        /*封装获取的accessToken的数据*/
        accessToken.setCode(code);
        accessToken.setRedirect_uri(accessToken.getRedirect_uri());
        accessToken.setState(state);
        accessToken.setClient_id(accessToken.getClient_id());
        accessToken.setClient_secret(accessToken.getClient_secret());

        /*传入dto,获取accessToken*/
        String accessToken = githubProvider.getAccessToken(this.accessToken);     /*C + A + v: 快速生成变量*/
        /*传入token,获取user*/
        GithubUser githubUser = githubProvider.getUser(accessToken);

        /*用于验证登陆github账户,可以获取GithubUser中的所有信息(本案例获取了login(用户名) id 更新时间)
         * 步骤:
         * 1.参照github提供的api,需要本地服务器提供连接,内容包含:
         *   client_id:传入服务器id   redirect_uri= 获取返回的登陆页面 scope:获取user信息  state: 生成随机字符串,防止跨站攻击
         *   github服务器会返回code(用于登陆的临时码)和state
         * 2.将client_id   client_secret   redirect_uri   code   state 5项数据再次传至git(post方法,https://github.com/login/oauth/access_token)
         *   会返回(响应)此格式的数据:access_token=e72e16c7e42f292c6912e7710c838347ae178b4a&token_type=bearer
         * 3.解析其中的access_token(可以使用split),再次请求git(get方法,https://api.github.com/user?access_token=)
         *   返回完整的user的信息        */


        /*判断是否获取到了user（user非空且有真实gitID）*/
        if (githubUser != null && githubUser.getId() != 0) {

            String datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());/*获取当前时间的timestamp*/
            String token = UUID.randomUUID().toString();
            CommunityUser communityUser = userMapper.findUserByGithub_account_id(Long.toString(githubUser.getId()));
            /*判断user是否已经存在于sql*/
            if (communityUser != null) {
                /*如果存在，则更新*/
                communityUser.setGmt_last_login(datetime);    /*更新登陆时间*/
                communityUser.setToken(token);                  /*更新token*/
                communityUser.setAvatar_url(githubUser.getAvatar_url());            /*头像*/
                userMapper.updateUser(communityUser);
            } else {
                /*如果不存在，则创建*/
                communityUser = new CommunityUser();
                communityUser.setGithub_account_id(Long.toString(githubUser.getId()));    /*设置aid*/
                communityUser.setUsername(githubUser.getLogin());                         /*设置name*/
                communityUser.setPassword("githubUser");                         /*设置password*/
                communityUser.setToken(token);                                      /*设置token*/
                communityUser.setGmt_create(datetime);                              /*生成时间*/
                communityUser.setGmt_last_login(datetime);                         /*最近登陆*/
                communityUser.setAvatar_url(githubUser.getAvatar_url());            /*头像*/
                userMapper.createUser(communityUser);
            }
            /*已弃用，更换为shiro认证*/
//            /*手动赋予cookie*/  /*此链接是从/callback返回,也就是说只有登陆的时候才会获取cookie*/
//            response.addCookie(new Cookie("token", token));
            /*通过shiro进行user认证*/
            GitHubAccountLoginByShiro(communityUser.getUsername());
            request.getSession().setAttribute("communityUser",communityUser);
            /*无论成功(携带session)与否重定向 至/login*/
            return "redirect:/homepage";
        } else {
            System.out.println("can not find user！");
            return "redirect:/loginFailed";
        }
    }

    private void GitHubAccountLoginByShiro(String username) {
        Subject subject = SecurityUtils.getSubject();
        /*设置密码因为实际上shiro登陆还是需要传入password*/
        UsernamePasswordToken token = new UsernamePasswordToken(username,"githubUser");
        subject.login(token);
    }

    /*test账号*/
    @GetMapping("/special")
    public String specialLogin(HttpServletRequest request){
        /*使用shiro登陆*/
        testAccountLoginByShiro("tester01");

        /*已弃用，改用shiro登陆*/
//        response.addCookie(new Cookie("token","e91a9f6b-02b1-4ea3-a4e9-908340e2c125"));

        CommunityUser user = (CommunityUser) SecurityUtils.getSubject().getPrincipal();
        request.getSession().setAttribute("communityUser",user);
        return "redirect:/homepage";
    }

    private void testAccountLoginByShiro(String username) {
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(username,"testaccountpassword");
        subject.login(token);
    }
}
