	var map;
	var TILE_SIZE = 256;
	var bremen = new google.maps.LatLng(53.05437, 8.78788);
	var player= 1;
	var players=[];
	var playersDiv=[];
	
	function bound(value, opt_min, opt_max) {
	  if (opt_min != null) value = Math.max(value, opt_min);
	  if (opt_max != null) value = Math.min(value, opt_max);
	  return value;
	}
	
	function degreesToRadians(deg) {
	  return deg * (Math.PI / 180);
	}
	
	function radiansToDegrees(rad) {
	  return rad / (Math.PI / 180);
	}
	
	/** @constructor */
	function MercatorProjection() {
	  this.pixelOrigin_ = new google.maps.Point(TILE_SIZE / 2,
	      TILE_SIZE / 2);
	  this.pixelsPerLonDegree_ = TILE_SIZE / 360;
	  this.pixelsPerLonRadian_ = TILE_SIZE / (2 * Math.PI);
	}
	
	MercatorProjection.prototype.fromLatLngToPoint = function(latLng,
	    opt_point) {
	  var me = this;
	  var point = opt_point || new google.maps.Point(0, 0);
	  var origin = me.pixelOrigin_;
	
	  point.x = origin.x + latLng.lng() * me.pixelsPerLonDegree_;
	
	  // Truncating to 0.9999 effectively limits latitude to 89.189. This is
	  // about a third of a tile past the edge of the world tile.
	  var siny = bound(Math.sin(degreesToRadians(latLng.lat())), -0.9999,
	      0.9999);
	  point.y = origin.y + 0.5 * Math.log((1 + siny) / (1 - siny)) *
	      -me.pixelsPerLonRadian_;
	  return point;
	};
	
	MercatorProjection.prototype.fromPointToLatLng = function(point) {
	  var me = this;
	  var origin = me.pixelOrigin_;
	  var lng = (point.x - origin.x) / me.pixelsPerLonDegree_;
	  var latRadians = (point.y - origin.y) / -me.pixelsPerLonRadian_;
	  var lat = radiansToDegrees(2 * Math.atan(Math.exp(latRadians)) -
	      Math.PI / 2);
	  return new google.maps.LatLng(lat, lng);
	};
	
	function createInfoWindowContent(marker) {
	  var numTiles = 1 << map.getZoom();
	  var projection = new MercatorProjection();
	  var worldCoordinate = projection.fromLatLngToPoint(bremen);
	  var pixelCoordinate = new google.maps.Point(
	      worldCoordinate.x * numTiles,
	      worldCoordinate.y * numTiles);
	  var tileCoordinate = new google.maps.Point(
	      Math.floor(pixelCoordinate.x / TILE_SIZE),
	      Math.floor(pixelCoordinate.y / TILE_SIZE));
	
	  return [
	     marker.title,
	    'Position: ' + marker.position,
	    'Backpack: ' + ' ',
		'Team: ' + '  '
		
	  ].join('<br>');
	}
	function update(longitude, latitude, id){
		setMarker(longitude, latitude, id);
		setDropDown();
	
	}
	function setMarker(longitude, latitude, id) {
		var myLatlng = new google.maps.LatLng(latitude, longitude);
		for(var i = 0; i<players.length; i++){
			if(players[i].id == id){
				players[i].setPosition(myLatlng);
				break;
			}
		}
		  var myLatlng = new google.maps.LatLng(latitude, longitude);
		   var marker =  new google.maps.Marker({
				position:myLatlng,
				map:map,
			    animation: google.maps.Animation.DROP,
				title: 'Player ' +id.toString()
			});
			players.push(marker);
		    alert('Neuer Spieler: ' + marker.title);
			var divOptions = {
				 gmap: map,
				 name: marker.title,
				 title: "Pan to Player",
				 id: "mapOpt",
				 action: function(){
				     centerPlayer(marker, map);
				 }
			}
			var optionDiv1 = new optionDiv(divOptions);
			playersDiv.push(optionDiv1);
			google.maps.event.addListener(marker, 'click', function() {
			    
				var coordInfoWindow = new google.maps.InfoWindow();
				coordInfoWindow.setContent(createInfoWindowContent(marker));
				coordInfoWindow.open(map, marker);
			});

	
	
	}
	function setDropDown(){
      var sep = new separator();
	      
	      //put them all together to create the drop down       
	      var ddDivOptions = {
	      	items: playersDiv,
	      	id: "myddOptsDiv"        		
	      }
	      //alert(ddDivOptions.items[1]);
	      var dropDownDiv = new dropDownOptionsDiv(ddDivOptions);               
	              
	      var dropDownOptions = {
	      		gmap: map,
	      		name: 'Center Player',
	      		id: 'ddControl',
	      		title: 'A custom drop down select with mixed elements',
	      		position: google.maps.ControlPosition.TOP_RIGHT,
	      		dropDown: dropDownDiv 
	      }
	      
	    var dropDown1 = new dropDownControl(dropDownOptions);     

		
	}
	//Sets the map on all markers in the array.
	function setAllMap(map) {
	  for (var i = 0; i < players.length; i++) {
	    players[i].setMap(map);
	  }
	}
	//Removes the markers from the map, but keeps them in the array.
	function clearPlayers() {
	  setAllMap(null);
	}
	
	// Shows any markers currently in the array.
	function showPlayers() {
	  setAllMap(map);
	}
	
	// Deletes all markers in the array by removing references to them.
	function deletePlayers() {
	  clearMarkers();
	  players = [];
	}
	
	function centerPlayer(player, map){
		map.panTo(player.position);
	}
	
	function initialize() {
	  var mapOptions = {
	    zoom: 12,
	    center: bremen
	  };
	
	  map = new google.maps.Map(document.getElementById('map-canvas'),
	      mapOptions);
      var buttonOptions = {
      		gmap: map,
      		name: 'Hide all Players',
      		position: google.maps.ControlPosition.TOP_RIGHT,
      		action: function(){
      			clearPlayers();
      		}
      }
      var button1 = new buttonControl(buttonOptions);

      var buttonOptions = {
      		gmap: map,
      		name: 'Show all Players',
      		position: google.maps.ControlPosition.TOP_RIGHT,
      		action: function(){
      			showPlayers();
      			
      		}
      }
      var button1 = new buttonControl(buttonOptions);

              
    }
	
	
	google.maps.event.addDomListener(window, 'load', initialize);