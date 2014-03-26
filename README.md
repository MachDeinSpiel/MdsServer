MdsServer
=========


MDS Client Server Protokoll
==========


Prinzipieller Ablauf (REST und WebSockets)
===========

GET

    1. Client: 1. GET -->> Server
    2. Server: RESPONSE:JSON|FAILED -->> Client
    
POST

    1. Client: POST -->> Server
    2. Server: RESPONSE:OK|FAILED -->> Client
    3. Server: WS: "c" -->> Client 
    4. Client: GET -->> Server
    5. Server: RESPONSE:JSON|FAILED --->> Client
    
PUT

    1. Client: PUT -->> Server
    5. Server: RESPONSE:OK|FAILED -->> Client
    4. Client: GET -->> Server
    5. Server: RESPONSE:JSON|FAILED -->> Client
    
DELET

    1. Client: DELETE -->> Server
    2. Server: RESPONSE:OK|FAILED -->>
    3. Server: WS: "d"-->>
    4. Client: GET -->> Server
    5. Server: RESPONSE:JSON|FAILED -->>


GET, POST, PUT, DELETE = REST-Anfragen
WS = WebSocket Nachricht


Beispiel
===========

1. (Client) WS		Client meldet sich am WebSocket-Server an 

					ws://server:port

2. (Client) REST 	Client holt sich über server:port/mds/appinfo die Übersicht über alle 
					verfügbaren Spiele/Apps.

					GET http://server:port/mds/appinfo

3. (Client) REST	Client postet seine Spieler-Infos an den Server

					POST http://server:port/mds/player 
					Parameter:	player={json:player}

4. (Server) WS		Server sendet an alle angemeldeten WS-Teilnehmer JSON Statusnachricht, dass sich ein neuer Player angemeldet hat.

					{"status":"c", "class"="player", "id":0} 
					Aufbau, siehe "Server Status-Nachrichten"

5. (Client) WS		Client empfängt WS-Nachricht und aktualisiert darauf das entsprechende Objekt über REST

					GET http://server:port/mds/player/0

6. (Client) REST	Client holt sich alle Spiele, die auf dem Server laufen

					GET http://server:port/mds/game

7.1 (Client) REST	Client tritt Spiel mit ID 0 bei

					PUT http://server:port/mds/game/0
					Parameter:	game={json:changed}

7.1.1 (Server) WS	Server sendet an alle angemeldeten WS-Teilnehmer JSON Statusnachricht, dass sich ein Spiel geändert hat.

					{"status":"u", "class"="game", "id":0}

7.1.2 (Client) WS	Client empfängt WS-Nachricht und aktualisiert darauf das entsprechende Objekt über REST

					GET http://server:port/mds/game/0

7.2 (Client) REST	Client erstellt neues Spiel

					POST http://server:port/mds/game/0
					Parameter:	game={json:game}

7.2.1 (Server) WS	Server sendet an alle angemeldeten WS-Teilnehmer JSON Statusnachricht, dass es ein neues Spiel gibt.

					Bsp.: {"status":"c", "class"="game", "id":1}

7.2.2 (Client) WS	Client empfängt WS-Nachricht und aktualisiert darauf das entsprechende Objekt über REST

					BSP.: GET http://server:port/mds/game/1



Server Status-Nachrichten
===========

Aufbau der Nachricht: 

    {"status":"<Status-Char>", "object":"<Klassenname>", "id":<int>}

Status-Chars
--
"c" = neues Object (create)  
"u" = Objekt aktualisiert (update)  
"d" = Objekt gelöscht (delete)  

Objekte
--
"appinfo" = JSON-Apps und -Games  
"player" = Spieler  
"game" = Spiele auf dem Server  
"item" = Items auf der Karte  

Alle WebSocket Nachrichten die mit "#" beginnen, sollen ignoriert werden (kann für sonstige Status oder Debugging genutzt werden).
