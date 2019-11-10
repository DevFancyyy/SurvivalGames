package de.fancy.survivalgames.utils;

import de.fancy.survivalgames.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GameCountdown implements Runnable {
    int countdown;
    Gamestate nextGamestate;
    int taskID = -1;
    boolean levelChange;

    public GameCountdown() {
    }

    public void setCountdown(int countdown) {
        this.countdown = countdown;
    }

    public void setNextGamestate(Gamestate nextGamestate) {
        this.nextGamestate = nextGamestate;
    }

    public void setTaskID(int taskID) {
        this.taskID = taskID;
    }

    public void setLevelChange(boolean isEnabled) {
        this.levelChange = isEnabled;
    }

    public int getCountdown() {
        return this.countdown;
    }

    public Gamestate getNextGamestate() {
        return this.nextGamestate;
    }

    public int getTaskID() {
        return this.taskID;
    }

    @Override
    public void run() {
        this.countdown--;

        if(this.levelChange == true) {
            for(Player allPlayers : Bukkit.getOnlinePlayers()) {
                allPlayers.setLevel(this.countdown);
            }
        }

        if((this.countdown == 60 || this.countdown == 45 || this.countdown == 30 || this.countdown == 15 || this.countdown == 10 || this.countdown < 6) && this.countdown != 0) {
            Main.getInstance().getGameManager().sendCountdownMessage(this.countdown, this.nextGamestate);
        } else if(this.countdown == 0) {
            Main.getInstance().getGameManager().sendCountdownMessage(this.countdown, this.nextGamestate);
            Main.getInstance().getGameManager().changeGamestate(this.nextGamestate);
            this.cancel();
        }

        Main.getInstance().getGameManager().updateScoreboard();
    }

    public void cancel() {
        Bukkit.getScheduler().cancelTask(this.taskID);
    }
}
