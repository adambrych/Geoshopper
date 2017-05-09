var express = require("express");
var bodyParser = require("body-parser");
var path = require("path");

var routes = require("./routes");
var config = require("./config");

var server = express();
server.use(bodyParser.json());
server.use(express.static(path.join(__dirname, routes.STATIC_RESOURCES)));

server.get(routes.HOME, function(req, res) {
   res.sendFile(path.join(__dirname + routes.VIEW_REGISTER));
});

server.listen(config.PORT, function() {
    console.log("Geoshopper server listening on port " + config.PORT);
});