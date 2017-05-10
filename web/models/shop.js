var db = require("../db");

var Shop = db.model("Shop", {
    name: { type: String, required: true },
    city: { type: String, required: true },
    street: { type: String, required: true },
    latitude: { type: Number, required: true },
    longitude: { type: Number, required: true }
});

module.exports = Shop;