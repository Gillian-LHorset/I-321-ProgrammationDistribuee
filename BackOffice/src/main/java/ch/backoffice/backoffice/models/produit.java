package ch.backoffice.backoffice.models;

public class produit {
    public String ID;
    public String Name;
    public String Picture;
    public Double Price;
    public produit (String ID, String Name, String Picture, Double Price) {
        this.ID = ID;
        this.Name = Name;
        this.Picture = Picture;
        this.Price = Price;
    }
}
