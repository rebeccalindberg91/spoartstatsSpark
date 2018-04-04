/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.sportstatsspark;

import static spark.Spark.*;
import spark.servlet.SparkApplication;
import sportstats.service.AddSportService;
import sportstats.service.AddTeamService;
import sportstats.service.GetAllSportsService;
import sportstats.service.GetTeamsBySportIdService;
import sportstats.service.ServiceRunner;
import sportstats.service.SportstatsService;


/**
 *
 * @author Rebecca
 */
public class SampleSparkEndpoint implements SparkApplication {

    @Override
    public void init() {
    
        get("/spark/hello", (request, response) -> "{\"message\": \"Hello, world - from sparkjava endpoint\"}");
        //get("/spark/sport", (request, response) -> new ServiceRunner<>(new GetAllSportsService()).execute());
        get("/spark/hello/:name", (request, response) -> "{\"message\": \"Hello, " + request.params(":name") +" - from sparkjava endpoint with parameter\"}");
        post("/spark/hello2", (request, response) -> "{\"message\": \"Hello, world - from sparkjava endpoint 2\"}");
        get("/spark/sport", (request, response) -> run(new GetAllSportsService()));
        get("/spark/addSport/:name", (request, response) -> run(new AddSportService(request.params(":name"))));
        get("/spark/getTeamsId/:id", (request, response) -> 
            run(new GetTeamsBySportIdService(Long.parseLong(request.params(":id")))));

        get("/spark/addTeam/:name/:id", (request, response) -> 
                run(new AddTeamService(request.params(":name"),(Long.parseLong(request.params(":id"))))));
    }
    
    private String run(SportstatsService service) {
        return new ServiceRunner<>(service).execute();
    }
    
    
}
