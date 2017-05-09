var express = require("express");
var bodyParser = require("body-parser");
var paths = require("./paths");
var config = require("./config");

var server = express();
server.use(bodyParser.json());

server.get(paths.HOME, function(req, res) {
   console.log("hello geoshopper");
});

server.listen(config.PORT, function() {
    console.log("Geoshopper listening on port " + config.PORT);
});