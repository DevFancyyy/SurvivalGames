package de.fancy.survivalgames.utils;

import de.fancy.survivalgames.Main;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.*;
import java.util.List;
import java.util.UUID;

public class MySQL {
    public static String host = Main.getInstance().getConfig().getString("host");
    public static String port = Main.getInstance().getConfig().getString("port");
    public static String database = Main.getInstance().getConfig().getString("database");
    public static String username = Main.getInstance().getConfig().getString("username");
    public static String password = Main.getInstance().getConfig().getString("password");
    public static Connection con;

    public static void connect() {
        if (!isConnected()) {
            try {
                con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void disconnect() {
        if (isConnected()) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isConnected() {
        return con != null;
    }

    public static void reconnect() {
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        connect();
    }

    public static Connection getConnection() {
        return con;
    }

    public static void createTable() {
        try {
            PreparedStatement ps = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS sg (UUID VARCHAR(100),KILLS INT(100),DEATHS INT(100),PGAMES INT(100),LGAMES INT(100),WGAMES INT(100),ACHIEVEMENTS TEXT)");
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean ifUserexists(UUID uuid) {
        try {
            PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT UUID FROM sg WHERE UUID = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void addUser(UUID uuid) {
        try {
            PreparedStatement ps = MySQL.getConnection().prepareStatement("INSERT INTO sg (UUID) VALUE (?)");
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setIntValue(UUID uuid, String change, int value) {
        if(ifUserexists(uuid)) {
            try {
                PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE sg SET " + change +  "= ? WHERE uuid = ?");
                ps.setInt(1, value);
                ps.setString(2, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static Integer getIntValue(UUID uuid, String value) {
        try {
            PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT " + value + " FROM sg WHERE UUID = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Integer.valueOf(rs.getInt(value));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setStringValue(UUID uuid, String change, String value) {
        if(ifUserexists(uuid)) {
            try {
                PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE sg SET " + change +  "= ? WHERE uuid = ?");
                ps.setString(1, value);
                ps.setString(2, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getStringValue(UUID uuid, String value) {
        try {
            PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT " + value + " FROM sg WHERE UUID = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(value);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Achievements> getPlayerAchievements(UUID uuid) {
        try {
            PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT achievements FROM sg WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return Achievements.getAchievementsFromString(rs.getString("achievements"));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void addPlayerAchievement(UUID uuid, Achievements achievement) {
        try {
            List<Achievements> achievementList = getPlayerAchievements(uuid);
            achievementList.add(achievement);

            PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE sg SET achievements = ? WHERE uuid = ?");
            ps.setString(1, achievementList.toString());
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static ResultSet getPlayerData(UUID uuid) {
        try {
            PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM sg WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs;
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ResultSet getPositionData(int position) {
        try {
            PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM sg ORDER BY kills DESC limit ?,1");
            ps.setInt(1, position-1);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs;
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void addPlayerStatistic(UUID uuid, String statistic) {
        try {
            int newStatistic = getIntValue(uuid, statistic) + 1;

            PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE sg SET " + statistic + " = ? WHERE uuid = ?");
            ps.setInt(1, newStatistic);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Integer getPlayerPosition(UUID uuid) {
        try {
            PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT *, FIND_IN_SET(kills, (SELECT GROUP_CONCAT(kills ORDER BY kills DESC) FROM sg)) AS rank FROM sg WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Integer.valueOf(rs.getInt("rank"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
