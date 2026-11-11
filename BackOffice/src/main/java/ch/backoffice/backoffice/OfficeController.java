package ch.backoffice.backoffice;

import ch.backoffice.backoffice.models.bpModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;

public class OfficeController {
    @FXML
    public Label Id;
    public Label Name;
    public ImageView  Logo;
    public Label ProductId;
    public Label ProductName;
    public ImageView  ProductImage;

    @FXML
    public void setBackOfficeData(bpModel bp) {
        Id.setText(bp.ID);
        Name.setText(bp.Name);
        Logo.setImage(bp.Logo);
        ProductId.setText(bp.Special.ID);
        ProductName.setText(bp.Special.Name);
        ProductImage.setImage(bp.Special.Picture);
    }
}
