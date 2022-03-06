package com.ssafy.mafiace.api.controller;

import com.ssafy.mafiace.api.response.BaseResponseBody;
import com.ssafy.mafiace.api.response.GameEndRes;
import com.ssafy.mafiace.api.response.GameRoomRes;
import com.ssafy.mafiace.api.response.VoteRes;
import com.ssafy.mafiace.api.service.GameLogService;
import com.ssafy.mafiace.api.service.GameService;
import com.ssafy.mafiace.api.service.SessionService;
import com.ssafy.mafiace.api.service.UserGameLogService;
import com.ssafy.mafiace.api.service.UserHonorService;
import com.ssafy.mafiace.api.service.UserRecordsService;
import com.ssafy.mafiace.api.service.UserService;
import com.ssafy.mafiace.common.model.GameInfo;
import com.ssafy.mafiace.db.entity.Game;
import com.ssafy.mafiace.common.model.MafiaceManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("*")
@Api(value = "게임 방 관리 API", tags = {"GameController"})
@RestController
@RequestMapping("/api/game")
public class GameController {

    @Autowired
    private GameService gameService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private GameLogService gameLogService;

    @Autowired
    UserGameLogService userGameLogService;

    @Autowired
    UserRecordsService userRecordsService;

    @Autowired
    UserService userService;

    @Autowired
    UserHonorService userHonorService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    private Map<String, MafiaceManager> gameManagerMap;

    @PostConstruct
    public void init() {
        gameManagerMap = new ConcurrentHashMap<>();
    }

    @GetMapping("")
    @ApiOperation(value = "게임방 목록 조회")
    @ApiResponses({
        @ApiResponse(code = 200, message = "성공"),
        @ApiResponse(code = 500, message = "Server Error"),
    })
    public ResponseEntity<GameRoomRes> getGameList(int maxPlayer, int isPublic) {
        List<GameInfo> gameInfoList = new ArrayList<>();

        List<Game> list = gameService.getGameList(maxPlayer, isPublic);
        for (Game game : list) {
            game.setPassword("");
            gameInfoList.add(GameInfo.of(game, sessionService.getParticipantCount(game.getId())));
        }

        return ResponseEntity.status(200)
            .body(GameRoomRes.of(200, "Success", gameInfoList));
    }


    @GetMapping("/checkpw")
    public ResponseEntity<BaseResponseBody> checkPassword(String sessionName, String password) {

        if (gameService.checkPassword(sessionName, password)) {
            return ResponseEntity.status(200)
                .body(BaseResponseBody.of(200, "입장하라"));
        }

        return ResponseEntity.status(401)
            .body(BaseResponseBody.of(401, "비밀번호 불일치"));
    }

    // 방장 교체
    public void ownerChangeMessage(String roomId, String ownerId) {

        JSONObject data = new JSONObject();
        data.put("check", "owner");
        data.put("ownerNickname", ownerId);
        simpMessagingTemplate.convertAndSend("/topic/" + roomId, data.toString());
    }

    // 게임 시작
    @MessageMapping("/start/{roomId}") // 발행경로
    @SendTo("/topic/{roomId}") // 구독경로
    public void gameStartBroadcasting(@DestinationVariable String roomId) throws Exception {
        gameManagerMap.put(roomId, new MafiaceManager(roomId, sessionService, gameService,
            userService, userRecordsService, userGameLogService, gameLogService, userHonorService));
    }

    // 게임이 끝났는지 체크하고 승리팀 판단
    @MessageMapping("/end/{roomId}")
    public void gameEndBroadcasting(@DestinationVariable String roomId, String next)
        throws Exception {
        MafiaceManager manager = gameManagerMap.get(roomId);
        GameEndRes gameEndRes = manager.checkGameEnd(next);
        if (gameEndRes.getEnd().equals("end")) {
            manager.saveRecord();
            gameManagerMap.remove(roomId);
        }
        simpMessagingTemplate.convertAndSend("/topic/" + roomId, gameEndRes);
    }


    //타이머
    @MessageMapping("/timer/{roomId}")
    public void sendToMessage(@DestinationVariable String roomId) {
        simpMessagingTemplate.convertAndSend("/topic/" + roomId, "start");
    }

    // 낮->밤
    @MessageMapping("/night/{roomId}")
    public void toNight(@DestinationVariable String roomId) {
        simpMessagingTemplate.convertAndSend("/topic/" + roomId, "night");
    }

    // 밤->낮
    @MessageMapping("/day/{roomId}")
    public void toDay(@DestinationVariable String roomId) {
        simpMessagingTemplate.convertAndSend("/topic/" + roomId, "day");
    }

    @MessageMapping("/vote/{roomId}")
    public void vote(@DestinationVariable String roomId, String voted) {
        MafiaceManager manager = gameManagerMap.get(roomId);
        manager.addVoteList(voted);
        if(manager.voteAll()){
            simpMessagingTemplate.convertAndSend("/topic/" + roomId, "skip");
        }
    }

    @MessageMapping("/heal/{roomId}")
    public void healByDoctor(@DestinationVariable String roomId, String voted) {
        MafiaceManager manager = gameManagerMap.get(roomId);
        manager.setHealTarget(voted);
    }

    @MessageMapping("/investigate/{roomId}/{nickname}")
    public void investigate(@DestinationVariable String roomId,
        @DestinationVariable String nickname, String voted) {
        MafiaceManager manager = gameManagerMap.get(roomId);
        String role = gameManagerMap.get(roomId).getPlayers().findRoleName(voted);
        if (role.equals("Mafia")) {
            manager.getPlayers().addInvestigateCount(); // 경찰 탐지횟수 +1
        }
        JSONObject data = new JSONObject();
        data.put("role", role);
        data.put("check", "investigate");
        simpMessagingTemplate.convertAndSend("/topic/" + nickname, data.toString());

    }

    // 투표 결과를 얻어옴
    @MessageMapping("/result/{roomId}")
    public void voteResult(@DestinationVariable String roomId) {
        MafiaceManager manager = gameManagerMap.get(roomId);
        VoteRes voteRes = manager.getVoteResult();
        manager.reset();
        if (voteRes.getCheck().equals("selected")) {
            manager.addDeathPlayer(voteRes.getNickname());
            manager.getPlayers().getPlayer(voteRes.getNickname()).setDead();
        }
        simpMessagingTemplate.convertAndSend("/topic/" + roomId, voteRes);
    }

    // 역할 확인
    @MessageMapping("/role/{roomId}/{nickname}")
    public void roleConfirm(@DestinationVariable String roomId,
        @DestinationVariable String nickname)
        throws JSONException {
        String role = gameManagerMap.get(roomId).getPlayers().findRoleName(nickname);
        JSONObject data = new JSONObject();
        data.put("role", role);
        data.put("check", "role");
        simpMessagingTemplate.convertAndSend("/topic/" + nickname, data.toString());
    }

    // 마피아 반환
    @MessageMapping("/mafia/{roomId}/{nickname}")
    public void whoIsMafia(@DestinationVariable String roomId, @DestinationVariable String nickname) {
        GameEndRes mafiaTeam=new GameEndRes();
        mafiaTeam.setEnd("MafiaTeam");
        mafiaTeam.setMafia(gameManagerMap.get(roomId).getPlayers().getMafia());
        simpMessagingTemplate.convertAndSend("/topic/" + nickname, mafiaTeam);
    }

    // 게임하다 나가면 사망처리
    @MessageMapping("/exit/{roomId}/{nickname}")
    public void exit(@DestinationVariable String roomId, @DestinationVariable String nickname) {
        MafiaceManager manager = gameManagerMap.get(roomId);
        manager.addDeathPlayer(nickname);
        manager.getPlayers().getPlayer(nickname).setDead();
        simpMessagingTemplate.convertAndSend("/topic/" + roomId, new VoteRes(nickname, "exit"));
    }

//    /gameset/{roomId}
//    // manager.saveGameLog();
}
