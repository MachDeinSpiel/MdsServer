function Whiteboard() {


		Whiteboard.prototype.getValue = function(keys){
			var pfad = keys.split(',');
			var value = pfad[length-1];
			pfad.pop();
			return value;
		}


		Whiteboard.prototype.setAttribute = function(base, keys, value){
			// Function: createNestedObject( base, names[, value] )
		//   base: the object on which to create the hierarchy
		//   names: an array of strings contaning the names of the objects
		//   value (optional): if given, will be the last object in the hierarchy
		// Returns: the last object in the hierarchy
		    // If a value is given, remove the last name and keep it for later:
		    var lastName = arguments.length === 3 ? keys.pop() : false;

		    // Walk the hierarchy, creating new objects where needed.
		    // If the lastName was removed, then the last object is not set yet:
		    for( var i = 0; i < keys.length; i++ ) {
		        base = base[ keys[i] ] = base[ keys[i] ] || {};
		    }

		    // If a value was given, set it to the last name:
		    if( lastName ) base = base[ lastName ] = value;

		    // Return the last object in the hierarchy:
		    return base;
		};
	}

	/**
	 * Setzt die Sichtbarkeit eines Attribut des Whiteboards, indem nacheinander die Schlüssel eingesetzt werden, um ann das Attribut zu gelangen
	 * @param value Wert, den das Attribut erhalten soll
	 * @param keys Schlüssel für das Whiteboard, entweder als einzelne Attribute oder als String-Array
	 */
	function setAttributeVisibility(visiblity, keys){
		getAttribute(keys).visibility = visiblity;
	}



	/**
	 * Gibt den Wert des Attributes zurück, dass über die Schlüssel erreicht werden kann
	 * @param keys Schlüssel für das Whiteboard, entweder als einzelne Attribute oder als String-Array
	 * @return
	 */
	function getAttribute(keys){

		var wb = this;

		for (var i = 0; i < keys.length-1; i++) {
			wb = wb.get(keys[i]).value;
		}

		return wb.get(keys[keys.length-1]);
	}


