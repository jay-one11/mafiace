package com.ssafy.mafiace.db.entity;

import java.time.LocalDateTime;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@Entity(name = "user_game_log")
@AttributeOverrides({
    @AttributeOverride(name = "id",column = @Column(name = "user_game_log_id", unique = true))
})
public class UserGameLog extends BaseEntity{

    @NotNull @Column(name = "job")
    String roleName;

    @NotNull @Column(name = "is_win")
    boolean isWin;

    @Column(name = "play_time")
    String playTime;

    @Column(name = "start_time")
    LocalDateTime startTime;

    @Builder
    private UserGameLog(String roleName, boolean isWin, String playTime, GameLog gameLog, User user){
        this.isWin = isWin;
        this.roleName = roleName;
        this.playTime = playTime;
        this.gameLog = gameLog;
        this.startTime = LocalDateTime.now();
        this.user = user;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_log_id")
    private GameLog gameLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_unique_id")
    private User user;

    public void setGameLog(GameLog gameLog) {
        this.gameLog = gameLog;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
