/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.sportstatsspark;

import sportstats.rest.shapes.LeagueShape;
import sportstats.rest.shapes.RoundShape;
import sportstats.rest.shapes.SeasonShape;
import sportstats.rest.shapes.SeasonTeamShape;
import sportstats.rest.shapes.SportShape;
import sportstats.rest.shapes.TeamShape;
import com.owlike.genson.Genson;
import java.util.HashMap;
import java.util.Map;
import static spark.Spark.*;
import spark.servlet.SparkApplication;
import sportstats.rest.json.JsonOutputFormatter;
import sportstats.rest.shapes.ResultShape;
import sportstats.rest.shapes.ArenaShape;
import sportstats.rest.shapes.GameArenaShape;
import sportstats.service.AddLeagueService;
import sportstats.service.AddResultService;
import sportstats.service.AddRoundService;
import sportstats.service.AddSeasonService;
import sportstats.service.AddSportService;
import sportstats.service.AddTeamService;
import sportstats.service.AddTeamToSeasonService;
import sportstats.service.AddArenaService;
import sportstats.service.GetAllSportsService;
import sportstats.service.GetAwayGamesByTeamIdService;
import sportstats.service.GetGamesByRoundIdService;
import sportstats.service.GetGamesBySeasonIdService;
import sportstats.service.GetGamesByTeamIdService;
import sportstats.service.GetGamesLostByTeamIdService;
import sportstats.service.GetGamesTiedByTeamIdService;
import sportstats.service.GetGamesWonByTeamIdService;
import sportstats.service.GetHomeGamesByTeamIdService;
import sportstats.service.GetLeaguesBySportIdService;
import sportstats.service.GetSeasonsByLeagueIdService;
import sportstats.service.GetTeamsBySportIdService;
import sportstats.service.ServiceRunner;
import sportstats.service.SportstatsService;
import sportstats.service.SportstatsServiceException;

/**
 *
 * @author Rebecca
 */
public class SportstatsApp implements SparkApplication {

    @Override
    public void init() {

        before((req, res) -> {
            res.type("application/json");
        });

        notFound((req, res) -> {
            res.status(404);
            return createError(404, "404 Not Found");
        });

        internalServerError((req, res) -> {
            res.status(500);
            return createError(500, "500 Internal Server Error");
        });

        get("/spark/hello", (request, response) -> "{\"message\": \"Hello, world - from sparkjava endpoint\"}");
        get("/spark/hello/:name", (request, response) -> "{\"message\": \"Hello, " + request.params(":name") + " - from sparkjava endpoint with parameter\"}");
        post("/spark/hello2", (request, response) -> "{\"message\": \"Hello, world - from sparkjava endpoint 2\"}");

        //Sports
        get("/sports", (request, response) -> run(new GetAllSportsService()));
        post("/sports", (req, res) -> {
            try {
                SportShape newSport = new Genson().deserialize(req.body(), SportShape.class);

                return run(new AddSportService(newSport.name));
            } catch (Exception ex) {
                return createError(ex.getMessage());
            }
        });

        //Leagues
        get("/sports/:id/leagues", (req, res) -> {
            try {
                return run(
                        new GetLeaguesBySportIdService(
                                Long.valueOf(req.params(":id"))
                        )
                );
            } catch (NumberFormatException ex) {
                return createError("SportId should be an integer");
            }
        });
        post("/leagues", (req, res) -> {
            try {
                LeagueShape newLeague = new Genson().deserialize(req.body(), LeagueShape.class);

                return run(new AddLeagueService(newLeague.name, newLeague.sportId));
            } catch (SportstatsServiceException ex) {
                return createError(ex.getMessage());
            } catch (Exception ex) {
                return createError("Wrong shape.");
            }
        });

        //Seasons
        get("/leagues/:id/seasons", (req, res) -> {
            try {
                return run(
                        new GetSeasonsByLeagueIdService(
                                Long.valueOf(req.params(":id"))
                        )
                );
            } catch (NumberFormatException ex) {
                return createError("LeagueId should be an integer");
            }
        });
        post("/seasons", (req, res) -> {
            try {
                SeasonShape newSeason = new Genson().deserialize(req.body(), SeasonShape.class);

                return run(new AddSeasonService(newSeason.year, newSeason.summer, newSeason.leagueId));
            } catch (SportstatsServiceException ex) {
                return createError(ex.getMessage());
            } catch (Exception ex) {
                return createError("Wrong shape.");
            }
        });
        //get("/seasons/:id/teams", (req, res) -> "");
        post("/seasons/:id/teams", (req, res) -> {
            try {
                SeasonTeamShape newSeasonTeam = new Genson().deserialize(req.body(), SeasonTeamShape.class);

                return run(new AddTeamToSeasonService(newSeasonTeam.teamId,
                        newSeasonTeam.seasonId));
            } catch (SportstatsServiceException ex) {
                return createError(ex.getMessage());
            } catch (Exception ex) {
                return createError("Wrong shape.");
            }
        });

        //Teams
        get("/sports/:id/teams", (req, res) -> {
            try {
                return run(
                        new GetTeamsBySportIdService(
                                Long.valueOf(req.params(":id"))
                        )
                );
            } catch (NumberFormatException ex) {
                return createError("SportId should be an integer");
            }
        });

        post("/teams", (req, res) -> {
            try {
                TeamShape newTeam = new Genson().deserialize(req.body(), TeamShape.class);

                return run(new AddTeamService(newTeam.name, newTeam.sportId));
            } catch (SportstatsServiceException ex) {
                return createError(ex.getMessage());
            } catch (Exception ex) {
                return createError("Wrong shape.");
            }
        });

        //Rounds
        post("/rounds", (req, res) -> {
            try {
                RoundShape newRound = new Genson().deserialize(req.body(), RoundShape.class);

                return run(new AddRoundService(newRound.seasonId, newRound.games));
            } catch (SportstatsServiceException ex) {
                return createError(ex.getMessage());
            } catch (Exception ex) {
                return createError("Wrong shape.");
            }
        });

        //Games
        get("/rounds/:id/games", (req, res) -> {
            try {
                return run(new GetGamesByRoundIdService(
                        Long.valueOf(req.params(":id"))));
            } catch (NumberFormatException ex) {
                return createError("RoundId should be an integer");
            }
        });

        get("/seasons/:id/games", (req, res) -> {
            try {
                return run(new GetGamesBySeasonIdService(
                        Long.valueOf(req.params(":id"))));
            } catch (NumberFormatException ex) {
                return createError("SeasonId should be an integer");
            }
        });

        //GamesByTeamId and filter
        get("/teams/:id/games", (req, res) -> {
            try {
                return run(
                        new GetGamesByTeamIdService(
                                Long.valueOf(req.params(":id"))
                        )
                );
            } catch (NumberFormatException ex) {
                return createError("TeamId should be an integer");
            }
        });
        get("/teams/:id/games/wins", (req, res) -> {
            try {
                return run(
                        new GetGamesWonByTeamIdService(
                                Long.valueOf(req.params(":id"))
                        )
                );
            } catch (NumberFormatException ex) {
                return createError("TeamId should be an integer");
            }
        });
        get("/teams/:id/games/ties", (req, res) -> {
            try {
                return run(
                        new GetGamesTiedByTeamIdService(
                                Long.valueOf(req.params(":id"))
                        )
                );
            } catch (NumberFormatException ex) {
                return createError("TeamId should be an integer");
            }
        });
        get("/teams/:id/games/losses", (req, res) -> {
            try {
                return run(
                        new GetGamesLostByTeamIdService(
                                Long.valueOf(req.params(":id"))
                        )
                );
            } catch (NumberFormatException ex) {
                return createError("TeamId should be an integer");
            }
        });
        get("/teams/:id/games/home", (req, res) -> {
            try {
                return run(
                        new GetHomeGamesByTeamIdService(
                                Long.valueOf(req.params(":id"))
                        )
                );
            } catch (NumberFormatException ex) {
                return createError("TeamId should be an integer");
            }
        });
        get("/teams/:id/games/away", (req, res) -> {
            try {
                return run(
                        new GetAwayGamesByTeamIdService(
                                Long.valueOf(req.params(":id"))
                        )
                );
            } catch (NumberFormatException ex) {
                return createError("TeamId should be an integer");
            }
        });

        //Result
        post("/games/:id/result", (req, res) -> {
            try {
                
                ResultShape newResult = new Genson().deserialize(req.body(), ResultShape.class);

                return run(
                        new AddResultService(newResult.gameId,
                                newResult.scoreHomeTeam,
                                newResult.scoreAwayTeam)
                );
            } catch (NumberFormatException ex) {
                return createError("GameId should be an integer");
            } catch (SportstatsServiceException ex) {
                return createError(ex.getMessage());
            } catch (Exception ex) {
                return createError("Wrong shape.");
            }
        });
        
        // ----------
           //Arena
        post("/games/:id/arena", (req, res) -> {
            try {
                
                GameArenaShape newArena = new Genson().deserialize(req.body(), GameArenaShape.class);

                return run(
                        new AddArenaService(newArena.name, newArena.gameId)
                );
            } catch (NumberFormatException ex) {
                return createError("GameId should be an integer");
            } catch (SportstatsServiceException ex) {
                return createError(ex.getMessage());
            } catch (Exception ex) {
                return createError("Wrong shape.");
            }
        });
        //---------

    }

    private String run(SportstatsService service) {
        return new ServiceRunner<>(service).execute();
    }

    private String createError(Integer statusCode, String message) {
        Map<String, Object> errorContent = new HashMap<String, Object>() {
            {
                if (statusCode != null) {
                    put("status", statusCode);
                }
                put("message", message);
            }
        };
        Map<String, Object> error = new HashMap<String, Object>() {
            {
                put("error", errorContent);
            }
        };

        return new JsonOutputFormatter()
                .createOutput(error);
    }

    private String createError(String message) {
        return createError(null, message);
    }
}
