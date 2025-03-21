package com.mongodb.javabasic.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping(path = "/session")
public class SessionController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private HttpSessionSecurityContextRepository securityContextRepository;

    @GetMapping("/put")
    public Object put(HttpServletRequest request, HttpServletResponse response, HttpSession session,
            @RequestParam("s") String search) {
        Map<String, Object> doc = (LinkedHashMap<String, Object>) session.getAttribute("shareData");
        if (doc == null) {
            doc = new LinkedHashMap<>();
            doc.putAll(Map.of("search", new ArrayList<>(), "views", 1));
        }
        if (search != null)
            ((List) doc.get("search")).add(search);
        String podName = System.getenv("HOSTNAME");
        doc.put("podName", podName);
        doc.put("views", (int) doc.get("views") + 1);
        session.setAttribute("shareData", doc);
        return new Document("principal",
                SecurityContextHolder.getContext().getAuthentication() != null
                        ? SecurityContextHolder.getContext().getAuthentication().getName()
                        : "")
                .append("data", session.getAttribute("shareData"));
    }

    @GetMapping("/clear")
    public Object clear(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        session.removeAttribute("shareData");
        return new Document("principal",
                SecurityContextHolder.getContext().getAuthentication() != null
                        ? SecurityContextHolder.getContext().getAuthentication().getName()
                        : "")
                .append("data", session.getAttribute("shareData"));
    }

    @GetMapping("/login")
    public Object login(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(request.getParameter("uId"),
                "m0001@12345");
        Authentication auth = authenticationManager.authenticate(token);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        return new Document("principal",
                SecurityContextHolder.getContext().getAuthentication() != null
                        ? SecurityContextHolder.getContext().getAuthentication().getName()
                        : "")
                .append("data", session.getAttribute("shareData"));
    }

    @GetMapping("/logout")
    public Object logout(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        if (SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
                !(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken)) {
            new SecurityContextLogoutHandler().logout(request, response,
                    SecurityContextHolder.getContext().getAuthentication());
        }
        return new Document("principal",
                SecurityContextHolder.getContext().getAuthentication() != null
                        ? SecurityContextHolder.getContext().getAuthentication().getName()
                        : "")
                .append("data", session.getAttribute("shareData"));
    }
}
