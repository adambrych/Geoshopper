var db = require("../db");

var Product = db.model("Product", {
    shop: { type: String, required: true, ref: 'Brand' },
    name: { type: String, required: true },
    size: { type: String, required: true },
    price: { type: Number, required: true },
    from: { type: Date, required: true },
    to: { type: Date, required: true }
});

module.exports = Product;