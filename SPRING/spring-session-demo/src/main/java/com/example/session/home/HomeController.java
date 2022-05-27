package com.example.session.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Class desc.
 *
 * @author o118014_D
 * @since 2022-03-31
 */

@Controller
public class HomeController {

    @GetMapping("/")
    public void home(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("its home");
    }
}
