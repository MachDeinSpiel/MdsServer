	var map;
	var TILE_SIZE = 256;
	var bremen = new google.maps.LatLng(53.05437, 8.78788);
	var player= 1;
	// array for player marker
	var players=[];
	// array for item marker
	var items = [];

	
	var totalPlayerMarker;
	var totalItemMarker;
	
	var totalPLayer;
	var totalItem;
	
	var message = null;

	// array for DropDown information
	var playersDiv = [];
    var dropDown1;

	// initializing player information window
	function createInfoWindowContent(marker) {	
	  return [
	     marker.title,
	    'Position: ' + marker.position,
	    'Backpack: ' + ' ',
		'Team: ' + '  '
		
	  ].join('<br>');
	}
	
	// major update function
	function update(changings, qwhiteboard, values){
		var whiteboard = qwhiteboard;
		whiteboard.updateWhiteboard(changings, whiteboard, values);
		updatePlayers(whiteboard);
		updateItems(whiteboard.Bombs, 'bomb', 0);
		updateItems(whiteboard.Medipacks, 'medi', 1);
		console.log(items);
		createItemDropDown();
		
		return whiteboard;
	
	}
	
	// updating player markers
	function updatePlayers(whiteboard) {
		totalPlayer  = whiteboard.Players;
		totalPlayerMarker = 0;
		for (var i in totalPlayer) {
			//console.log(i);
			var myLatlng = null;
			if((typeof totalPlayer[i].latitude !='undefined') &&  (typeof totalPlayer[i].latitude !='undefined')){
				myLatlng = new google.maps.LatLng(totalPlayer[i].latitude, totalPlayer[i].longitude);
			}
		    if (players[totalPlayerMarker] == null) { // Create new marker if player does not already exists
		        players[totalPlayerMarker] = new google.maps.Marker({
		        	position: myLatlng,
		        	map: map,
		        	animation: google.maps.Animation.DROP,
		        	title: 'Player ' + totalPlayer[i].health.toString()
		        });
			    alert('Neuer Spieler: ' + players[totalPlayerMarker].title);
		        
		    } else { // Update the player marker if already exists
				players[totalPlayerMarker].setPosition(myLatlng);
		    }
		

	    google.maps.event.addListener(players[totalPlayerMarker], 'click', function() { 
			console.log(players[totalPlayerMarker]);
	        if (!this.infowindow) { // um bestehende infowindows wiederzuverwenden 
	            this.infowindow = new google.maps.InfoWindow({ 
	                content: createInfoWindowContent(players[totalPlayerMarker])
	            }); 
	        }; 
	        this.infowindow.open(map, this); 
	    });	
	    totalPlayerMarker = totalPlayerMarker + 1;
		}
	}
	
	// Updating the itemmarkers
	function updateItems(item, icon, count) {

		totalItem = item;
		totalItemMarker = 0;
		current = [];
		items.push(current);
		for (var i in totalItem) {
			if((typeof totalItem[i].latitude !='undefined') &&  (typeof totalItem[i].latitude !='undefined')){
				var myLatlng = new google.maps.LatLng(totalItem[i].latitude, totalItem[i].longitude);
			}
		    if (items[count][totalItemMarker] == null) { // Create new marker if not already exist
		        items[count][totalItemMarker] = new google.maps.Marker({
		        	position: myLatlng,
		        	map: map,
		        	animation: google.maps.Animation.DROP,
		        	title: 'Item ' + totalItem[i].toString(),
		        	icon: 'images/'+ icon + '.png'
		        });
		    } else { // update the marker
				items[count][totalItemMarker].setPosition(myLatlng);
		    }

		    google.maps.event.addListener(items[count][totalItemMarker], 'click', function() { 
		    	console.log(items[count][totalItemMarker]);
		        if (!this.infowindow) { // um bestehende infowindows wiederzuverwenden 
		            this.infowindow = new google.maps.InfoWindow({ 
		                content: createInfoWindowContent(items[count][totalItemMarker])
		            }); 
		        }; 
		        this.infowindow.open(map, this); 
		    });
		    totalItemMarker = totalItemMarker + 1;
		}

	}
	
	
	// Shows any markers currently in the array.
	function showPlayers(id) {
		if((typeof players !='undefined')){
			if(document.getElementById(id).style.display == 'block'){
				clearPlayers();
				console.log("block");
			} else {
				setAllPlayersMap(map, players);
				console.log("none");
			}
		}
	}
	
	//Shows all Players with Health lower than 20
	function showLowHealthPlayers(id){
		if((typeof players !='undefined')){
			if(document.getElementById(id).style.display == 'block'){
				playersLow = [];
				for(var i = 0; i < totalPlayer; i ++){
					if(players[i].health <= 20){
						playersLow.push(players[i]);
					}
				}
				if(playersLow[0] != null){
					setAllPlayersMap(map, playersLow);
					clearPlayers();
				}
			} else {
				setAllPlayersMap(map, players);
			}
		}
	}
	
	//Sets the map on all markers in the array.
	function setAllPlayersMap(map, playerArray) {
		console.log("players:" +playerArray);
	  for (var i = 0; i < totalPlayerMarker; i++) {
	    playerArray[i].setMap(map);
	  }
	}

	//Removes the markers from the map, but keeps them in the array.
	function clearPlayers() {
	  setAllPlayersMap(null, players);
	}
	
	
	//Sets the map on all markers in the array.
	function setAllItemsMap(map, arrayCount) {
	  for (var i = 0; i < items.length; i++) {
		  console.log(i);
		  console.log(arrayCount);
	    items[arrayCount][i].setMap(map);
	  }
	}
	
	function clearItems(itemsArray) {
		  setAllItemsMap(null, itemsArray);
		}
	
	function showItems(id, wichItemsArray) {
		if(document.getElementById(id).style.display == 'block'){
			clearItems(wichItemsArray);
			console.log("block");
		} else {
			setAllItemsMap(map, wichItemsArray);
			console.log("none");
		}

	}
	
	// Deletes all markers in the array by removing references to them.
	function deletePlayers() {
	  clearMarkers();
	  players = [];
	}
	
	// focus on the playermarker
	function centerPlayer(player, map){
		map.panTo(player.position);
	}
	
	function createItemDropDown(){
        // Item DropDown
        var itemsDivs = [];
       /* var checkOptions3 = {
        		gmap: map,
        		title: "Click to hide all Items",
        		id: "terrainCheck2",
        		label: "Hide Items",				
        		action: function(){
        			//showItems(checkOptions3.id, items);
        		}        		        		
        }*/
        for(var i = 0; i < items.length; i++){
	        var checkOptions4 = {
	            gmap: map,
	            title: "Click to show Players with low Health",
	            id: "id" + items[i].toString(),
	            label: "Hide " + items[i][0].title,
	            action: function(){
	            	showItems(checkOptions4.id, i);
	            	}        		        		
	            }
	            var check4 = new checkBox(checkOptions4);
	            itemsDivs.push(check4);
	        	console.log("Current " + current);
        	}      
        //create the input box items
        
        //put them all together to create the drop down       
        var ddDivOptions2 = {
        	items: itemsDivs,
        	id: "myddOptsDiv2"        		
        }
        //alert(ddDivOptions.items[1]);
        var dropDownDiv2 = new dropDownOptionsDiv(ddDivOptions2);               
                
        var dropDownOptions2 = {
        		gmap: map,
        		name: 'Item Filter',
        		id: 'ddControl2',
        		title: 'Click to use several Filters',
        		position: google.maps.ControlPosition.TOP_RIGHT,
        		dropDown: dropDownDiv2 
        }
        
        var dropDown2 = new dropDownControl(dropDownOptions2);             

	}
	
	
	// initializing the actual map
	function initialize() {
		var mapOptions = {
			zoom: 12,
			center: bremen
		};
	
		map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);

        var hidePlayersOptions = {
        		gmap: map,
        		title: "Click to hide all active Players",
        		id: "terrainCheck",
        		label: "Hide Players",				
        		action: function(){
        			showPlayers(hidePlayersOptions.id);
        		}        		        		
        }
        var check1 = new checkBox(hidePlayersOptions);
        
        var checkOptions2 = {
        		gmap: map,
        		title: "Click to show Players with low Health",
        		id: "myCheck",
        		label: "Low Health",
        		action: function(){
        			showLowHealthPlayers(checkOptions2.id);
        		}        		        		
        }
        var check2 = new checkBox(checkOptions2);
        
        //create the input box items
        
        //put them all together to create the drop down       
        var ddDivOptions = {
        	items: [check1, check2],
        	id: "myddOptsDiv"        		
        }
        //alert(ddDivOptions.items[1]);
        var dropDownDiv = new dropDownOptionsDiv(ddDivOptions);               
                
        var dropDownOptions = {
        		gmap: map,
        		name: 'Player Filter',
        		id: 'ddControl',
        		title: 'Click to use several Filters',
        		position: google.maps.ControlPosition.TOP_RIGHT,
        		dropDown: dropDownDiv 
        }   
        //Player DropDown
        var dropDown1 = new dropDownControl(dropDownOptions);     
        
        
        
        

 
	}

google.maps.event.addDomListener(window, 'load', initialize);