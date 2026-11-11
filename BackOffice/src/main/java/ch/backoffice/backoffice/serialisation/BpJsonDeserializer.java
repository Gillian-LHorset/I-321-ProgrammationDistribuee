package ch.backoffice.backoffice.serialisation;

import ch.backoffice.backoffice.models.bpModel;
import ch.backoffice.backoffice.models.produit;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

public class BpJsonDeserializer {

    public static bpModel deserialize(String json) throws IOException {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonObject sp = root.getAsJsonObject("Special");

        return new bpModel(
                root.get("Id").getAsString(),
                root.get("Name").getAsString(),
                decodeBase64Image(root.get("Logo").getAsString()),
                new produit(
                        sp.get("Id").getAsString(),
                        sp.get("Name").getAsString(),
                        decodeBase64Image(sp.get("Picture").getAsString()),
                        sp.has("Price") && !sp.get("Price").isJsonNull()
                                ? sp.get("Price").getAsDouble() : null
                )
        );
    }

    private static Image decodeBase64Image(String dataUri) {
        String base64 = dataUri.contains(",")
                ? dataUri.substring(dataUri.indexOf(',') + 1)
                : dataUri;
        byte[] bytes = Base64.getDecoder().decode(base64);
        return new Image(new ByteArrayInputStream(bytes));
    }
}
