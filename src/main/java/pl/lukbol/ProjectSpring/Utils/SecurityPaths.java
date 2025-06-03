package pl.lukbol.ProjectSpring.Utils;

public class SecurityPaths {

    public static final String[] PUBLIC_ENDPOINTS = {
            "/user/register",
            "/login",
            "/loginPage",
            "/",
            "/user/resetPasswordEmail/**",
            "/user/resetSite",
            "/user/resetPassword",
            "/activate/**",
            "/registerPage",
            "/h2-console/**",
            "/test",
            "/test/**",
            "/error"
    };

    public static final String[] CLIENT_ADMIN_ENDPOINTS = {
            "/user/deleteUser",
            "/user/apply",
            "/userDetails",
            "/user/login-history",
            "/user/logout"
    };
}
