package com.mongodb.javabasic.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.bson.Document;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.javabasic.model.TspConfig;
import com.mongodb.javabasic.model.TspIdConfigMapDocumentSuggested;
import com.mongodb.javabasic.model.TspRoute;
import com.mongodb.javabasic.repositories.TspConfigRepository;
import com.mongodb.javabasic.repositories.TspCountryInfoRepository;
import com.mongodb.javabasic.repositories.TspIdConfigMapDocumentRepository;
import com.mongodb.javabasic.repositories.TspIdConfigMapDocumentSuggestedRepository;
import com.mongodb.javabasic.repositories.TspPortInfoRepository;
import com.mongodb.javabasic.repositories.TspRouteRepository;
import com.mongodb.javabasic.service.AggregationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.server.PathParam;

@RestController
@RequestMapping(path = "/")
public class TSPController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private AggregationService aggregationService;

    @Autowired
    private TspConfigRepository configRepository;
    @Autowired
    private TspRouteRepository routeRepository;
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
        populateShareData(session, map);
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

    private void populateShareData(HttpSession session, Map<String, Object> map) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Document doc = mongoTemplate.getCollection("sessions")
                .find(Filters.and(Filters.eq("session.principal", name),
                        Filters.eq("session.shareData.channel", "web")))
                .first();
        if (doc != null) {
            Document shareData = doc.get("session", Document.class).get("shareData", Document.class);
            ((List<String>) map.get("search")).addAll(
                    shareData.getList("search", String.class));
            map.put("views", (int) map.get("views") + shareData.get("views", Integer.class));
        }
    }

    @GetMapping("/cache-1mb")
    @Cacheable(value = "cache", key = "'1mb'")
    public Document cache1mb() {
        return new Document("value", randomString(1 * 1024 * 1024/3));
    }

    @GetMapping("/cache-5mb")
    @Cacheable(value = "cache", key = "'5mb'")
    public Document cache5mb() {
        return new Document("value", randomString(5 * 1024 * 1024/3));
    }

    @GetMapping("/cache-10mb")
    @Cacheable(value = "cache", key = "'10mb'")
    public Document cache10mb() {
        return new Document("value", randomString(10 * 1024 * 1024/3));
    }

    private String randomString(int size) {
        byte[] byteArray = new byte[size];
        UniformRandomProvider randomProvider = RandomSource.XO_RO_SHI_RO_128_PP.create();
        randomProvider.nextBytes(byteArray);
        return new String(byteArray);
    }

    @GetMapping("/config")
    public List<TspConfig> config() {
        // Combine config & fare_family_mapping
        return configRepository.getConfig(List.of(Map.entry("office_id", List.of("MNLCX08DM")), Map.entry("channel", List.of("MOB"))));
    }

    // secure_flight??

    @Autowired
    private TspCountryInfoRepository countryInfoRepository;
    @Autowired
    private TspPortInfoRepository portInfoRepository;

    @GetMapping("/country/init")
    public String countryInit() {
        MongoCollection<Document> coll = mongoTemplate.getCollection("ori_country_info");
        coll.createIndex(Indexes.ascending("ports.port_code"));
        coll.createIndex(Indexes.ascending("ports.airports.iata_airport_code"));
        try {
            aggregationService.getPipelineResults("ori_country_info", "country_to_port.json", Document.class);
            coll = mongoTemplate.getCollection("port_info");
            coll.createIndex(Indexes.ascending("port_code"));
            coll.createIndex(Indexes.ascending("airports.iata_airport_code"));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    @GetMapping("/country")
    public String country() {
        StopWatch watch = new StopWatch();
        watch.start("country");
        countryInfoRepository.findCountriesByAirportCodes(Set.of("ALV", "GRG"));
        watch.stop();
        watch.start("port");
        portInfoRepository.findPortsByAirportCodes(Set.of("ALV", "GRG"));
        watch.stop();
        return watch.prettyPrint();
    }

    @Autowired
    private TspIdConfigMapDocumentRepository idConfigRepository;
    @Autowired
    private TspIdConfigMapDocumentSuggestedRepository idConfigSuggestedRepository;

    @GetMapping("/id_config/init")
    public List<TspIdConfigMapDocumentSuggested> idConfigsInit() {
        MongoCollection<Document> coll = mongoTemplate.getCollection("tsp_id_config");
        coll.createIndex(Indexes.ascending("channel", "tsp_id", "used"));
        coll.createIndex(Indexes.ascending("used"), new IndexOptions().expireAfter(10000L, TimeUnit.HOURS));
        String[] channels = { "MOB", "WCMP" };
        EasyRandom generator = new EasyRandom(
                new EasyRandomParameters().seed(new Date().getTime()).stringLengthRange(5, 5));
        List<TspIdConfigMapDocumentSuggested> l = generator.objects(TspIdConfigMapDocumentSuggested.class, 30000)
                .map(e -> {
                    e.setId(null);
                    e.setCreateTime(new Date());
                    e.setUpdateTime(new Date());
                    if (Math.random() > 0.5)
                        e.setUsed(null);
                    e.setChannel(channels[Math.random() > 0.5 ? 0 : 1]);
                    return e;
                }).collect(Collectors.toList());
        return idConfigSuggestedRepository.saveAll(l);
    }

    @GetMapping("/id_config/{channel}/all")
    public List<TspIdConfigMapDocumentSuggested> idConfigsAll(@PathVariable String channel) {
        StopWatch watch = new StopWatch();
        watch.start("old");
        idConfigRepository.findByChannel(channel);
        watch.stop();
        watch.start("new");
        List<TspIdConfigMapDocumentSuggested> list = idConfigSuggestedRepository.findByChannel(channel);
        watch.stop();
        logger.info(watch.prettyPrint());
        return list;
    }

    @GetMapping("/id_config/{channel}")
    public List<TspIdConfigMapDocumentSuggested> idConfigs(@PathVariable String channel,
            @RequestParam("used") boolean used) {
        StopWatch watch = new StopWatch();
        watch.start("old");
        idConfigRepository.findByChannelAndUsed(channel, used);
        watch.stop();
        watch.start("new");
        List<TspIdConfigMapDocumentSuggested> list = idConfigSuggestedRepository.findByChannelAndUsedExists(channel,
                used);
        watch.stop();
        logger.info(watch.prettyPrint());
        return list;
    }

    @GetMapping("/id_config/{channel}/{tspId}")
    public TspIdConfigMapDocumentSuggested idConfig(@PathVariable String channel, @PathVariable String tspId) {
        StopWatch watch = new StopWatch();
        watch.start("old");
        idConfigRepository.findByChannelAndTspId(channel, tspId);
        watch.stop();
        watch.start("new");
        TspIdConfigMapDocumentSuggested doc = idConfigSuggestedRepository.findByTspId(tspId);
        watch.stop();
        logger.info(watch.prettyPrint());
        return doc;
    }

    @GetMapping("/id_config/{channel}/count")
    public Long idConfigCount(@PathVariable String channel, @PathParam("used") boolean used) {
        StopWatch watch = new StopWatch();
        watch.start("old");
        idConfigRepository.countByChannelAndUsed(channel, used);
        watch.stop();
        watch.start("new");
        Long count = idConfigSuggestedRepository.countByChannelAndUsedExists(channel, used);
        watch.stop();
        logger.info(watch.prettyPrint());
        return count;
    }

    @GetMapping(value = { "/route/{dep}", "/route/{dep}/{arr}" })
    public List<TspRoute> route(@PathVariable String dep, @PathVariable(required = false) String arr) {
        return routeRepository.getRoutes(dep, arr);
    }
}
