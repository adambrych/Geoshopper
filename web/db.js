var mongoose = require('mongoose');
var config = require("./config");

mongoose.Promise = global.Promise;
mongoose.connect("mongodb://localhost:" + config.DB_PORT + "/geoshopper", function () {
    console.log("Connected to mongodb.");
});

module.exports = mongoose;