/*
 * Pricer Server Software
 *
 * Confidential Property of Pricer AB (publ). Copyright © 1998-2017 Pricer AB (publ),
 * Box 215,Västra Järnvägsgatan 7, SE-101 24 Stockholm, Sweden. All rights reserved.
 */
package nzesl.shopping_app;

import se.pricer.widget.android.PricerPosition;
import se.pricer.widget.android.PricerShape;

class ShoppingObject {

    private String objectId;
    private String name;
    private String price;
    private PricerPosition position;
    private PricerShape shape;
    private boolean picked;

    ShoppingObject(String objectId, String name, String price, PricerPosition position, PricerShape shape) {
        this.objectId = objectId;
        this.name = name;
        this.price = price;
        this.position = position;
        this.shape = shape;
        this.picked = false;
    }

    String getObjectId() {
        return objectId;
    }

    public String getName() {
        return name;
    }

    String getPrice() {
        return price;
    }

    PricerPosition getPosition() {
        return position;
    }

    PricerShape getShape() {
        return shape;
    }

    boolean isPicked() {
        return picked;
    }

    void setPicked(boolean picked) {
        this.picked = picked;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ShoppingObject
                && objectId.equals(((ShoppingObject) other).getObjectId());
    }

    @Override
    public int hashCode() {
        return objectId.hashCode();
    }
}
