package com.grademe.grademe.controller;

import com.grademe.grademe.beans.User;
import com.grademe.grademe.service.SecurityService;
import com.grademe.grademe.service.UserService;
import com.grademe.grademe.service.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserValidator userValidator;

    //TODO change build user from angular
    @PostMapping("/registration")
    public@ResponseBody String registration(@ModelAttribute("userForm") User userForm, BindingResult bindingResult) {
        userValidator.validate(userForm, bindingResult);

        if (bindingResult.hasErrors()) {
            return "registration";
        }

        userService.save(userForm);

        securityService.autoLogin(userForm.getUsername(), userForm.getPasswordConfirm());

        return "Successfully Registered";
    }

    //TODO change to use auto login functionality
    @GetMapping("/login")
    public @ResponseBody String login(String error, String logout) {
        if (error != null) {
            System.out.println("here error");
            return "error : Your username and password is invalid.";
        }

        if (logout != null){
            return "message : You have been logged out successfully.";
        }

        System.out.println("Success here");
        return "Successfully login!";
    }

}
