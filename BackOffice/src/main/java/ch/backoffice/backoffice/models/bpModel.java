package ch.backoffice.backoffice.models;

public class bpModel {
    public String ID;
    public String Name;
    public String Logo;
    public produit Special;
    public bpModel(String ID, String Name, String Logo, produit Special) {
        this.ID = ID;
        this.Name = Name;
        this.Logo = Logo;
        this.Special = Special;
    }
}
