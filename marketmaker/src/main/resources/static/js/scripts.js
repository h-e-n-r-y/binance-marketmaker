function tradetick() {
	$.get( "/ticker.js?symbol=" + symbol, function( data, textStatus, jqxhr ) {
		  $("#tickerscript").replaceWith(data);
		  
	});
	console.log(symbol + ": " + price);
	var oldPrice = $("#tickerprice").text();
	if (oldPrice > price) {
		$("#ticker").addClass("red")
		$("#ticker").removeClass("green")
	}
	if (oldPrice < price) {
		$("#ticker").addClass("green")
		$("#ticker").removeClass("red")
	}
	$("#tickerprice").text(price);
}

function initSliders() {

	var slider1 = document.getElementById("winbuy");
	var output1 = document.getElementById("buypercentage");
	var slider2 = document.getElementById("winsell");
	var output2 = document.getElementById("sellpercentage");
	
	var winbuy;
	var winsell;
	
	if (slider1) {
		winbuy = slider1.value / 100.0;
		winsell = slider2.value / 100.0;
		
		output1.innerHTML = winbuy; // Display the default slider value
		output2.innerHTML = winsell; // Display the default slider value
		// Update the current slider value (each time you drag the slider handle)
		slider1.oninput = function() {
			winbuy = this.value / 100.0;
		    output1.innerHTML = winbuy;
		}
		slider2.oninput = function() {
			winsell = this.value / 100.0;
		    output2.innerHTML = winsell;
		}
	}
}