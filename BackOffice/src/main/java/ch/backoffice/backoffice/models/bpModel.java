package ch.backoffice.backoffice.models;

import javafx.scene.image.Image;

import java.awt.image.BufferedImage;

public class bpModel {
    public String ID;
    public String Name;
    public Image Logo;
    public produit Special;

    public bpModel(String ID, String Name, Image Logo, produit Special) {
        this.ID = ID;
        this.Name = Name;
        this.Logo = Logo;
        this.Special = Special;
    }
}