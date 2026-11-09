package ch.backoffice.backoffice.serialisation;

import ch.backoffice.backoffice.models.bpModel;
import ch.backoffice.backoffice.models.produit;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

public class BpJsonDeserializer {

    public static bpModel deserialize(String json) throws IOException {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonObject sp = root.getAsJsonObject("Special");

        return new bpModel(
                root.get("ID").getAsString(),
                root.get("Name").getAsString(),
                decodeBase64Image(root.get("Logo").getAsString()),
                new produit(
                        sp.get("ID").getAsString(),
                        sp.get("Name").getAsString(),
                        decodeBase64Image(sp.get("Picture").getAsString()),
                        sp.has("Price") && !sp.get("Price").isJsonNull()
                                ? sp.get("Price").getAsDouble() : null
                )
        );
    }

    private static BufferedImage decodeBase64Image(String dataUri) throws IOException {
        // retire le préfixe "data:image/png;base64,"
        String base64 = dataUri.substring(dataUri.indexOf(',') + 1);
        byte[] bytes = Base64.getDecoder().decode(base64);
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }
}
