Dieses SurvivalGames-Plugin dient als Referenz für meine GommeHD.net Developer-Bewerbung.

Befehle:
/start: Der Countdown wird auf 15 Sekunden gesenkt.
/stats: Zeigt eigene Stats an.
/stats <Spielername>: Zeigt die Stats eines Spielers an
/stats #n: Zeit die Stats des Spielers auf dem Platz n.

/setup <start | end>: Den Setup-Modus betreten bzw. verlassen (Blöcke abbaubar, Inventar unbegrenzt nutzbar).
/setup Waiting <setspawn | setworld>: Den Spawn / die Welt der Wartelobby setzen.
/setup map list: Öffne eine Liste aller Maps.
/setup map <map> info>§7: Rufe Infos zu einer Map ab.
/setup map <create | delete> <mapname>§7: Erstelle / Lösche eine Map.
/setup map <map> spawn add§7: Erstelle einen Spawnpunkt.
/setup map <map> spawn list§7: Liste die Spawnpunkte einer Map auf.
/setup map <map> spawn tp <id>§7: Teleportiere dich zu dem Spawnpunkt.
/setup map <map> spawn delete <id>§7: Lösche einen Spawnpunk.
/setup map <map> set <center | world>§7: Setze die Welt / den Mittelpunkt.
/setup map <map> set <border | maxplayer | minplayer> <Wert>: Setze den entsprechenden Wert für die angegebene Map.

Stats und Achievements:
Es gibt 5 verschiedene Achievements, sowie die Speicherung von gespielten, gewonnenen und verlorenen Spielen sowie von den Kills und den Deaths.
Daraus werden vom Plugin KD und Siegesquote errechnet.

Funktionen des Plugins:
- 5 verschiedene Phasen:
  - Lobbyphase
  - Vorbereitungsphase, keine Bewegung möglich, Teleportation auf die Map
  - Schutzphase, kein Kampf möglich
  - Ingame-Phase
  - Deathmatch, 5 Minuten, davon schrumpft 2 1/2 Minuten die Border bis auf 10 Blöcke
- Spectatormode, mit Navigator und Achievement-Item
- Spielertracker, der auf den nächsten lebenden Spieler zeigt
- GG wird im Chat hervorgehoben

Wichtige Info:
In die config.yml müssen MySQL-Daten eingefügt werden, damit dieses Plugin funktioniert.
Die angehängte Loots.yml kann benutzt werden, muss aber nicht zwangsweise. Wichtig ist, dass die loots.yml wie folgt aufgebaut ist:

items:
  - ItemID:SubID, MAX-AMOUNT, CHANCE

Sollte noch keine Map aufgesetzt sein, oder sollten nicht so viele Spawnpunkte gesetzt sind, wie es maximale Spieler gibt, dann
kann die Runde nicht starten und es gibt einen Fehler. Die Wartelobby wird automatisch aufgesetzt, wenn es noch keine gibt.
