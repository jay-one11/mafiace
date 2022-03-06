package com.ssafy.mafiace.db.entity;


import java.util.ArrayList;
import java.util.List;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter(AccessLevel.PRIVATE)
@Getter
@ToString
@Entity
@NoArgsConstructor
@Table(name = "game_log")
@AttributeOverrides({
    @AttributeOverride(name = "id",column = @Column(name = "game_log_id",unique = true))
})
public class GameLog extends BaseEntity{
    @Column(name = "win_team")
    String winTeam;
    @Column(name = "play_time  ")
    String playTime;

    @Builder
    private GameLog(String winTeam, String playTime){
        this.id = BaseEntity.shortUUID();
        this.winTeam = winTeam;
        this.playTime = playTime;
    }

    @OneToMany(mappedBy = "gameLog", fetch = FetchType.LAZY)
    private List<UserGameLog> userGameLogs = new ArrayList<>();

    public void addUserGameLogs(UserGameLog userGameLogs) {
        this.userGameLogs.add(userGameLogs);
        userGameLogs.setGameLog(this);
    }
}
