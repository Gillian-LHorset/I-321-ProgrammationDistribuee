package ch.backoffice.backoffice.models;

import javafx.scene.image.Image;

import java.awt.image.BufferedImage;

public class produit {
    public String ID;
    public String Name;
    public Image Picture;
    public Double Price;

    public produit(String ID, String Name, Image Picture, Double Price) {
        this.ID = ID;
        this.Name = Name;
        this.Picture = Picture;
        this.Price = Price;
    }
}
