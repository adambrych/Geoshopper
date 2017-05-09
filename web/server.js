var express = require("express");
var bodyParser = require("body-parser");
var path = require("path");

var routes = require("./routes");
var config = require("./config");
var validator = require("./validator");

var server = express();
server.use(bodyParser.json());
server.use(express.static(path.join(__dirname, routes.STATIC_RESOURCES)));

server.get(routes.HOME, function(req, res) {
   res.sendFile(path.join(__dirname + routes.VIEW_REGISTER));
});

server.post(routes.API_REGISTER, function(req, res) {
    var shop = req.body;
    var responseStatus = {};
    console.log(shop.shopName);
    if (validator.validateShop(shop)) {
        responseStatus.status = "1";
        //TODO: save to db
    } else {
        responseStatus.status = "0";
    }
    res.status(200).json(responseStatus);
});

server.listen(config.PORT, function() {
    console.log("Geoshopper server listening on port " + config.PORT);
});