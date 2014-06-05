function WebsocketService(){


            var ws;
        	var whiteboard = new Whiteboard();
        	var open = false;


            //Aufbau der Websocketverbindung
                ws = new WebSocket("ws://195.37.176.178:1387"); // ws://195.37.176.178:1387 ws://feijnox.no-ip.org:8000
                ws.onopen = function() {
                	document.getElementById('log').value = "[WebSocket#onopen]\n";
                	if(open == false){
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
	   					if (obj.updatemode == "full"){
	   						whiteboard = new Whiteboard();
	   					}
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
                
                onSendMessage = function(){
                	var message = '{"mode":"join","id":0,"name":"Monitoring"}'
                	document.getElementById('log').value = "[WebSocket#onmessage] Message: '" + message + "'\n";
                	ws.send(message);   
                }

                    
                ws.onclose = function() {
                	document.getElementById('log').value = "[WebSocket#onclose]\n";
                }

            

        
}