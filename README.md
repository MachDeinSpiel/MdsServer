###MDS Server

####Server starten (Linux / MacOS X)

Um MDS-Server auf einem UNIX-System zu starten empfehlen wird das Programm screen.  
Mit screen ist es möglich einen Prozess in der Konsole zu starten und im Hintergrund laufen zu lassen.  

Beispiel:  
```screen -r java mdsserver.jar http://example.com/config.json```

####Start Parameter
```mdsserver.jar <configuration URL to JSON file> [-debug | -debug -noauth]```

Beim Start muss immer einer URL zu einer JSON-Konfigurationsdatei angegeben werden.  

Mit dem Startparameter ```-debug``` werden Debugging-Informationen auf der Konsole ausgegeben.  
Die Option ```-debug -noauth``` ermöglicht das Deaktivieren der Benutzer-Authentifizierung. Diese Option steht nur im Debug-Modus zur Verfügung.
