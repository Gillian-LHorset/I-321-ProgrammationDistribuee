package ch.backoffice.backoffice.models;

import java.awt.image.BufferedImage;

public class produit {
    public String ID;
    public String Name;
    public BufferedImage Picture;
    public Double Price;

    public produit(String ID, String Name, BufferedImage Picture, Double Price) {
        this.ID = ID;
        this.Name = Name;
        this.Picture = Picture;
        this.Price = Price;
    }
}
