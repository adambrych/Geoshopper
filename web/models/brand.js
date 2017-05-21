var db = require("../db");

var Brand = db.model("Brand", {
    name: { type: String, required: true, unique: true },
    pin: { type: Number, required: true, unique: true }
});

module.exports = Brand;