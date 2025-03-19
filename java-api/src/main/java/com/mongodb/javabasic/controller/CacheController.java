package com.mongodb.javabasic.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping(path = "/cache")
public class CacheController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private HttpSessionSecurityContextRepository securityContextRepository;

    @GetMapping("/login")
    public Authentication login(HttpServletRequest request, HttpServletResponse response, HttpSession session) {

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(request.getParameter("uId"),
                "m0001@12345");
        Authentication auth = authenticationManager.authenticate(token);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        Map<String, Object> map = new LinkedHashMap<>();
        map.putAll(Map.of("search", new ArrayList<>(), "channel", "mob", "views", 1));
        session.setAttribute("shareData", map);
        securityContextRepository.saveContext(context, request, response);
        return auth;
    }

    @GetMapping("/session")
    public Object session(HttpServletRequest request, HttpServletResponse response, HttpSession session,
            @RequestParam("s") String search) {
        // get the principal name from session manually
        // session.getAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME);
        if (SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
                !(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken)) {
            Map<String, Object> doc = (LinkedHashMap<String, Object>) session.getAttribute("shareData");
            if (search != null)
                ((List) doc.get("search")).add(search);
            doc.put("views", (int) doc.get("views") + 1);
            session.setAttribute("shareData", doc);
            return doc;
        } else {
            logger.info("unauthenticated");
            return "unauthenticated";
        }
    }

    @GetMapping("/cache")
    @Cacheable(value = "data")
    public Document cache(@RequestParam int size) {
        return new Document("value", RandomStringUtils.randomAscii(size));
    }

    @GetMapping("/no-cache")
    public Document noCache(@RequestParam int size) {
        return new Document("value", RandomStringUtils.randomAscii(size));
    }

}
