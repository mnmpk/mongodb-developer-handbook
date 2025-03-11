const { AdvancedMarkerElement } = await google.maps.importLibrary("marker");

let poly;
let map;
let markersArray = [];
let infoWindow = new google.maps.InfoWindow();
let start, end;
let stopId;

async function initMap() {
    const { Map } = await google.maps.importLibrary("maps");

    map = new Map(document.getElementById("map"), {
        mapId: "map",
        center: { lat: 22.3193, lng: 114.1694 },
        zoom: 11,
    });
    poly = new google.maps.Polyline({
        strokeColor: "#000000",
        strokeOpacity: 1.0,
        strokeWeight: 3,
    });
    poly.setMap(map);
    map.addListener("click", addLatLng);
    const centerControlDiv = document.createElement("div");
    centerControlDiv.appendChild(createControl("search", search));
    centerControlDiv.appendChild(createControl("clear", clear));
    map.controls[google.maps.ControlPosition.TOP_CENTER].push(centerControlDiv);
}

function addLatLng(event) {
    const path = poly.getPath();

    //path.push(event.latLng);
    //if (path.length == 1)
    findStopsNearby(event.latLng);
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
            clearOverlays();
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
                    let content = s.id+"<br/>"+s.nameTc+"</br>"+s.nameEn;
                    if (!start)
                        content += "<br/><a id='start' href='#'>Set as start</a>";
                    if (!end)
                        content += "<br/><a id='end' href='#'>Set as end</a>";
                    infoWindow.setContent(content);
                    infoWindow.open(marker.map, marker);
                    infoWindow.addListener('domready', function () {
                        $('#start').click(setStart);
                        $('#end').click(setEnd);
                        $('#search').click(search);
                        $('#clear').click(clear);
                    });
                });
                markersArray.push(marker);
            });

        }
    });
}
function setStart() {
    if (start) {
        start.setMap(null);
    }
    start = new AdvancedMarkerElement({
        position: infoWindow.position,
        title: stopId,
        map: map,
    });
    infoWindow.close();
}
function setEnd() {
    if (end) {
        end.setMap(null);
    }
    end = new AdvancedMarkerElement({
        position: infoWindow.position,
        title: stopId,
        map: map,
    });
    infoWindow.close();
}
function search() {
    console.log(start.title, end.title);
    $.ajax({
        url: "http://localhost:8080/ptes/routes/"+start.title+"/"+end.title,
        success: function (result) {
            clearOverlays();
            result.forEach(s => {
                console.log(s);
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
function createControl(label, fn) {
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
    controlButton.title = "Click to "+label;
    controlButton.type = "button";

    // Setup the click event listeners: simply set the map to Chicago.
    controlButton.addEventListener("click", fn);

    return controlButton;
}
function clearOverlays() {
    for (var i = 0; i < markersArray.length; i++) {
        markersArray[i].setMap(null);
    }
    markersArray.length = 0;
}