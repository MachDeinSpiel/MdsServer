function WebsocketService(){


            var ws;
            var map = new Map();
            map.initialize();
        	var whiteboard = new Whiteboard();
        	var open = false;
    		var mydropdown = document.getElementById('dropdown');


            //Aufbau der Websocketverbindung
                ws = new WebSocket("ws://localhost:8887"); // ws://195.37.176.178:1387 ws://feijnox.no-ip.org:8000
                ws.onopen = function() {
                	document.getElementById('log').value = "[WebSocket#onopen]\n";
                	if(open === false){
                		ws.send('{"mode":"activegames"}');
                		open = !open;
                	}

                }
                
                ws.onmessage = function(e) {
                	document.getElementById('log').value = "[WebSocket#onmessage] Message: '" + e.data + "'\n";
                    var message = e.data;
                    if(message.startsWith("{")){
	   					obj = JSON.parse(message);
	   					var changings = [];
	   					var values = [];
	   					if(!(obj.data == null)){
	   						for(var i = 0; i<obj.data.length; i++){
		   						changings[i] = obj.data[i].path;
		   						values[i] = obj.data[i].value;
	   						}
	   					}
		
	   				//Updatefunktion zur aktualisierung der Standortdaten der Spieler

	   					
			       		whiteboard = update(changings, whiteboard, values);
		       			console.log(whiteboard);
		   				
                    }
                }

                    
                ws.onclose = function() {
                	document.getElementById('log').value = "[WebSocket#onclose]\n";
                }

                
        		mydropdown.onclick = function(){
                	document.getElementById('log').value = "[WebSocket#onmessage] Message: '" + this.value + "'\n";
					for (var i in whiteboard){
   						if(typeof whiteboard[i] === 'object'){				
   							map.clearMapItems(whiteboard[i]);
   						}
					}
   					whiteboard = new Whiteboard();
        			ws.send(this.value);

        			
        		}
            
        		// major update function
        		function update(changings, whiteboard, values){
        			whiteboard.updateWhiteboard(changings, whiteboard, values);
        			map.update(whiteboard);
        
        			generate_table(whiteboard);
        
        			return whiteboard;
        	
        	}
        
}