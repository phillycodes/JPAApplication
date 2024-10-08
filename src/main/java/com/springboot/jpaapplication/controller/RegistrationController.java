package com.springboot.jpaapplication.controller;

import com.springboot.jpaapplication.model.User;
import com.springboot.jpaapplication.service.EmailService;
import com.springboot.jpaapplication.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.aspectj.weaver.patterns.IToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Controller
public class RegistrationController {

    Logger logger = Logger.getLogger(getClass().getName());

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    /**
     *
     * @param modelAndView
     * @param user
     * @return ModelAndView object that represents the Register Page
     */
    @GetMapping("/register")
    public ModelAndView showRegistrationPage(ModelAndView modelAndView, User user) {

        modelAndView.addObject("user", user);
        modelAndView.setViewName("register");

        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView processRegistrationForm(ModelAndView modelAndView, @Valid User user,
                                                BindingResult bindingResult,
                                                HttpServletRequest request) {
        User userExsits = userService.findByEmail(user.getEmail());

        logger.info(String.valueOf(userExsits));

        if (userExsits != null) {

            modelAndView.addObject("alreadyRegisteredMessage", "Oops! There is already a user " +
                    "registered with the email provided. ");
            modelAndView.setViewName("email");

            bindingResult.reject("email");
        }

        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("register");
        } else {
            user.setEnabled(false);
            user.setConfirmationToken(UUID.randomUUID().toString());

            userService.saveUser(user);

            String appUrl = request.getScheme() + "://" + request.getServerName() + ":8080";

            String message =
                    "To set your password, please click on the link below:\n" + appUrl +
                            "/confirm?token=" + user.getConfirmationToken();

            emailService.sendEmail(user.getEmail(), "Please set a password", message);

            modelAndView.addObject("ConfirmationMessage", "A password set e-mail has been sent to" +
                    user.getEmail());

            modelAndView.setViewName("register");
        }

        return modelAndView;
    }

    @GetMapping("/confirm")
    public ModelAndView confirmRegistration(ModelAndView modelAndView,
                                            @RequestParam("token") String token) {
        User user = userService.findByConfirmationToken(token);

        if (user == null) {
            modelAndView.addObject("invalidToken", "Invalid confirmation link.");
        } else {
            modelAndView.addObject("confirmationToken", user.getConfirmationToken());
        }
        modelAndView.setViewName("confirm");

        return modelAndView;
    }

    @PostMapping("/confirm")
    public ModelAndView confirmRegistration(ModelAndView modelAndView,
                                            BindingResult bindingResult, @RequestParam Map<String
            , String> requestParams, RedirectAttributes redir) {

        User user = userService.findByConfirmationToken(requestParams.get("token"));

        user.setPassword(bCryptPasswordEncoder.encode(requestParams.get("password")));
        user.setEnabled(true);

        userService.saveUser(user);

        modelAndView.setViewName("confirm");
        modelAndView.addObject("successMessage", "Password set successfully");

        return modelAndView;

    }

}
