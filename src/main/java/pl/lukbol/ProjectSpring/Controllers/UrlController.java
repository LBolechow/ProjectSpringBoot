package pl.lukbol.ProjectSpring.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UrlController {
    @RequestMapping(value = "/loginPage")
    public ModelAndView getLogin() {
        return new ModelAndView("login");
    }

    @RequestMapping(value = "/registerPage")
    public ModelAndView getRegister() {
        return new ModelAndView("register");
    }

    @RequestMapping(value = "/profile")
    public ModelAndView getProfile() {
        return new ModelAndView("profile");
    }

    @RequestMapping(value = "/")
    public ModelAndView getMainPage() {
        return new ModelAndView("main");
    }

    @RequestMapping(value = "/main")
    public ModelAndView getMainPage2() {
        return new ModelAndView("main");
    }


}
