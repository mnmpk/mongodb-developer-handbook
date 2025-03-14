const { AdvancedMarkerElement } = await google.maps.importLibrary("marker");

let polylines = [];
let map;
let markers = [];
let infoWindow = new google.maps.InfoWindow();
let start, end;
let stopId;
let suggestedRoutes;

async function initMap() {
    const { Map } = await google.maps.importLibrary("maps");

    map = new Map(document.getElementById("map"), {
        mapId: "map",
        center: { lat: 22.3193, lng: 114.1694 },
        zoom: 11, 
        clickableIcons: false 
    });
    map.addListener("click", addLatLng);
    /*const centerControlDiv = document.createElement("div");
    centerControlDiv.appendChild(createControl("search", search));
    centerControlDiv.appendChild(createControl("clear", clear));
    map.controls[google.maps.ControlPosition.TOP_CENTER].push(centerControlDiv);*/
}

function addLatLng(event) {
    if (start && end) {
        clear();
    }
    if (!start && !end) {
        clearOverlays();
        findStopsNearby(event.latLng);
        findRoutesNearby(event.latLng);
        setStart(event.latLng);
    }
    else if (start && !end) {
        //clearOverlays();
        //findStopsNearby(event.latLng);
        //findRoutesNearby(event.latLng);
        setEnd(event.latLng);
        search();
    }
}

initMap();

function findStopsNearby(latLng) {
    $.ajax({
        url: "http://localhost:8080/ptes/stops",
        data: {
            lat: latLng.lat(),
            lng: latLng.lng()
        },
        success: function (result) {
            result.forEach(s => {
                const marker = new AdvancedMarkerElement({
                    position: { lat: s.location.position.values[1], lng: s.location.position.values[0] },
                    title: s.id,
                    gmpClickable: true,
                    map: map,
                });
                // Add a click listener for each marker, and set up the info window.
                marker.addListener("click", ({ domEvent, latLng }) => {
                    const { target } = domEvent;

                    infoWindow.close();
                    stopId = marker.title;
                    let content = s.id + "<br/>" + s.nameTc + "</br>" + s.nameEn;
                    infoWindow.setContent(content);
                    infoWindow.open(marker.map, marker);
                    infoWindow.addListener('domready', function () {
                        $('#start').click(setStart);
                        $('#end').click(setEnd);
                        $('#search').click(search);
                        $('#clear').click(clear);
                    });
                });
                markers.push(marker);
            });

        }
    });
}

function findRoutesNearby(latLng) {
    $.ajax({
        url: "http://localhost:8080/ptes/routes",
        data: {
            lat: latLng.lat(),
            lng: latLng.lng()
        },
        success: function (result) {
            result.forEach(r => {
                let path = [];
                r.stops.forEach(s => {
                    path.push({ lat: s.location.position.values[1], lng: s.location.position.values[0] });
                });
                drawLine(path);
            });
        }
    });
}
function drawLine(path) {
    polylines.push(new google.maps.Polyline({
        map: map,
        path: path,
        strokeColor: "#000000",
        strokeOpacity: 0.5,
        strokeWeight: 1,
    }));
}
function setStart(latLng) {
    if (start) {
        start.setMap(null);
    }
    start = new AdvancedMarkerElement({
        position: latLng,
        map: map,
    });
    infoWindow.close();
}
function setEnd(latLng) {
    if (end) {
        end.setMap(null);
    }
    end = new AdvancedMarkerElement({
        position: latLng,
        map: map,
    });
    infoWindow.close();
}
function search() {
    $.ajax({
        url: "http://localhost:8080/ptes/routes",
        method: "POST",
        data: {
            start: [start.position.lng, start.position.lat],
            end: [end.position.lng, end.position.lat]
        },
        success: function (result) {
            suggestedRoutes = result;
            clearOverlays();
            let stops={};
            suggestedRoutes.forEach(r => {
                r.legs.forEach(l => {
                    let path = [];
                    l.stops.forEach(s => {
                        path.push({ lat: s.location.position.values[1], lng: s.location.position.values[0] });
                    });
                    drawLine(path);
                    const stop = l.stops[l.stopIndex];
                    const stopId = stop.id;
                    if(!stops[stopId])
                        stops[stopId]= {details:stop, routes:[]};
                    stops[stopId].routes.push(l.route+" "+l.serviceType);
                });
            });
            Object.keys(stops).forEach(k=>{
                const marker = new AdvancedMarkerElement({
                    position: { lat: stops[k].details.location.position.values[1], lng: stops[k].details.location.position.values[0] },
                    gmpClickable: true,
                    map: map,
                });
                marker.addListener("click", ({ domEvent, latLng }) => {
                    const { target } = domEvent;
                    let content = stops[k].details.id + "<br/>" + 
                    stops[k].details.nameTc + "</br>" + 
                    stops[k].details.nameEn+"<br/><br/>";
                    stops[k].routes.forEach(r=>{
                        content+=r+"<br/>";
                    });                    
                    infoWindow.close();
                    infoWindow.setContent(content);
                    infoWindow.open(marker.map, marker);
                });
                markers.push(marker);
            });
        }
    });
}
function clear() {
    if (start) {
        start.setMap(null);
    }
    start = null;
    if (end) {
        end.setMap(null);
    }
    end = null;
}
function clearOverlays() {
    for (var i = 0; i < markers.length; i++) {
        markers[i].setMap(null);
    }
    markers.length = 0;
    for (var i = 0; i < polylines.length; i++) {
        polylines[i].setMap(null);
    }
    polylines.length = 0;
}






/*function createControl(label, fn) {
    const controlButton = document.createElement("button");

    // Set CSS for the control.
    controlButton.style.backgroundColor = "#fff";
    controlButton.style.border = "2px solid #fff";
    controlButton.style.borderRadius = "3px";
    controlButton.style.boxShadow = "0 2px 6px rgba(0,0,0,.3)";
    controlButton.style.color = "rgb(25,25,25)";
    controlButton.style.cursor = "pointer";
    controlButton.style.fontFamily = "Roboto,Arial,sans-serif";
    controlButton.style.fontSize = "16px";
    controlButton.style.lineHeight = "38px";
    controlButton.style.margin = "8px 0 22px";
    controlButton.style.padding = "0 5px";
    controlButton.style.textAlign = "center";

    controlButton.textContent = label;
    controlButton.title = "Click to " + label;
    controlButton.type = "button";

    // Setup the click event listeners: simply set the map to Chicago.
    controlButton.addEventListener("click", fn);

    return controlButton;
}*/