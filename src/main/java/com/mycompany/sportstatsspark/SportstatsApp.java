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
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static spark.Spark.*;
import spark.servlet.SparkApplication;
import sportstats.rest.json.JsonOutputFormatter;
import sportstats.rest.shapes.ResultShape;
import sportstats.rest.shapes.ArenaShape;
import sportstats.rest.shapes.GameArenaShape;
import sportstats.service.leagues.AddLeagueService;
import sportstats.service.games.AddResultService;
import sportstats.service.games.AddRoundService;
import sportstats.service.seasons.AddSeasonService;
import sportstats.service.sports.AddSportService;
import sportstats.service.teams.AddTeamService;
import sportstats.service.seasons.AddTeamToSeasonService;
import sportstats.service.games.AddArenaService;
import sportstats.service.games.AddSpectatorsService;
import sportstats.service.sports.GetAllSportsService;
import sportstats.service.games.GetGamesByDateService;
import sportstats.service.games.GetAwayGamesByTeamIdService;
import sportstats.service.games.GetGamesByRoundIdService;
import sportstats.service.games.GetGamesBySeasonIdService;
import sportstats.service.games.GetGamesByTeamIdService;
import sportstats.service.games.GetGamesByTeamIdsService;
import sportstats.service.games.GetGamesLostByTeamIdService;
import sportstats.service.games.GetGamesTiedByTeamIdService;
import sportstats.service.games.GetGamesWonByTeamIdService;
import sportstats.service.games.GetHomeGamesByTeamIdService;
import sportstats.service.leagues.GetLeaguesBySportIdService;
import sportstats.service.seasons.GetSeasonsByLeagueIdService;
import sportstats.service.tables.GetTableBySeasonIds;
import sportstats.service.teams.GetTeamsBySportIdService;
import sportstats.service.ServiceRunner;
import sportstats.service.SportstatsService;
import sportstats.service.SportstatsServiceException;
import sportstats.service.tables.GetAwayTableBySeasonId;
import sportstats.service.tables.GetHomeTableBySeasonId;
import sportstats.service.tables.GetTableByRoundInterval;
import sportstats.service.tables.GetTableBySeasonIdAndDateInterval;

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
        get("/seasons/:ids/table", (req, res) -> {
            try {
                String[] strIds = req.params(":ids").split(",");
                Long[] longIds = new Long[strIds.length];
                
                for (int i=0; i < longIds.length; i++) {
                    longIds[i] = Long.valueOf(strIds[i]);
                }
                
                return run(
                        new GetTableBySeasonIds(longIds)
                );
            } catch (NumberFormatException ex) {
                return createError("SeasonIds should be integers");
            }
        });
        get("/seasons/:id/table/home", (req, res) -> {
            try {
                return run(
                        new GetHomeTableBySeasonId(
                                Long.valueOf(req.params(":id"))
                        )
                );
            } catch (NumberFormatException ex) {
                return createError("SeasonId should be an integer");
            }
        });
        get("/seasons/:id/table/away", (req, res) -> {
            try {
                return run(
                        new GetAwayTableBySeasonId(
                                Long.valueOf(req.params(":id"))
                        )
                );
            } catch (NumberFormatException ex) {
                return createError("SeasonId should be an integer");
            }
        });
        
        get("/seasons/:id/table/rounds/:round1/:round2", (req, res) -> {
            try {
                return run(new GetTableByRoundInterval(
                                Long.valueOf(req.params(":id")),
                                Long.valueOf(req.params(":round1")),
                                Long.valueOf(req.params(":round2"))
                        )
                );
            } catch (NumberFormatException ex) {
                return createError("SeasonId should be an integer");
            }
        });
        
        get("/seasons/:id/table/dates/:fromDate/:toDate", (req, res) -> {
            try {
                return run(new GetTableBySeasonIdAndDateInterval(
                        Long.valueOf(req.params(":id")),
                        Date.valueOf(req.params(":fromDate")), 
                        Date.valueOf(req.params(":toDate"))));
            } catch (NumberFormatException ex) {
                return createError("SeasonId should be an integer");
            } catch (Exception ex){
                return createError("Date should be in format yyyy-mm-dd");
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
        
        get("/:date/games", (req, res) -> {
           try {
               return run (new GetGamesByDateService(
               Date.valueOf(req.params(":date"))));
           } catch (Exception ex) {
               return createError("Date should be in format yyyy-mm-dd");
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
        //GetGamesByTeamIds meetings between two teams
        get("/teams/:firstId/:secondId/games", (req, res) -> {
            try {
                return run(
                        new GetGamesByTeamIdsService(
                                Long.valueOf(req.params(":firstId")),
                                Long.valueOf(req.params(":secondId"))
                        )
                );
            } catch (NumberFormatException ex) {
                return createError("TeamIds should be integers");
            }
        });

        //Result
        post("/games/:id/result", (req, res) -> {
            try {
                
                ResultShape newResult = new Genson().deserialize(req.body(), ResultShape.class);

                return run(
                        new AddResultService(newResult.gameId,
                                newResult.scoreHomeTeam,
                                newResult.scoreAwayTeam,
                                newResult.winType
                        )
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
        
        // spectators
         post("/games/:id/spectators", (req, res) -> {
            try {
                return run(
                        new AddSpectatorsService(
                                Long.valueOf(req.params("spectators")),
                                Long.valueOf(req.params(":id"))
                        ));
            } catch (NumberFormatException ex) {
                return createError("GameId and spectators should be an integer");
            } 
        });

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
