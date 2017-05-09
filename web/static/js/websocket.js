var poznanLatLng = {lat: 52.406939, lng: 16.934962};
var map;
var markers = [];
function initMap() {
    map = new google.maps.Map(document.getElementById('map'), {
        zoom: 14,
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        center: poznanLatLng
    });
}

window.onload = function() {
    var login = prompt("Podaj swój login:", "618739000");
    var connection = new WebSocket('ws://localhost:8888');
    connection.onopen = function() {
        connection.send(login);
    }
    connection.onmessage = function (e) {
        var msg = JSON.parse(e.data);
        if (msg[0] == 1) {
            var marker = new google.maps.Marker({
                position: {lat: msg[1], lng: msg[2]},
                map: map,
                animation: google.maps.Animation.DROP,
                title: 'Poszkodowany!'
            });
            markers[msg[3]] = marker;
            var alert = new Audio('alert.mp3');
            alert.play();
            setTimeout(function() {
                alert.play();
            }, 1000);
            setTimeout(function() {
                alert.play();
            }, 2000);
        }
        else if (msg[0] == 0) {
            markers[msg[1]].setMap(null);
        }
        else if (msg[0] == 2) {
            var center = {lat: msg[1], lng: msg[2]};
            map.setCenter(center);
        }

    };
}
