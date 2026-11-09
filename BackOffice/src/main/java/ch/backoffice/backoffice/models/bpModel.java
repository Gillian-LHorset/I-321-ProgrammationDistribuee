package ch.backoffice.backoffice.models;

import java.awt.image.BufferedImage;

public class bpModel {
    public String ID;
    public String Name;
    public BufferedImage Logo;
    public produit Special;

    public bpModel(String ID, String Name, BufferedImage Logo, produit Special) {
        this.ID = ID;
        this.Name = Name;
        this.Logo = Logo;
        this.Special = Special;
    }
}