package org.acme.quickstart;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;


@Path("/")
public class PaymentResource {

    class Order { 
        public ArrayList<Integer> productIds;
        public int orderAmount; 
        public String email;  
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "hello";
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response decodeQRCode(@MultipartForm FormData formData) throws IOException {

        if (formData.getQrFile() == null || formData.getQrFile().length() == 0) {
            ResponseBuilder response = Response.status(Status.BAD_REQUEST);
            response.header("Reason", "No QRCode file was uploaded (use form parameter 'file')");
            return response.build();
        }

        try {
            Result result = qrParser(formData);
            ResponseBuilder response = Response.ok(result.getText(), MediaType.APPLICATION_JSON);
            
            // read values from QR code
            Response paymentResponse = postPayment(result.getText());
            if (paymentResponse.getStatus() == 200) {
                return response.build();
            } else {
                return response.status(Status.UNAUTHORIZED).build();
            }

        } catch (NotFoundException e) {
            System.out.println("There is no QR code in the image." + e.getMessage());
            return null;
        }
    }

    // private Order parseStringToOrder(Result result) {
    //     Jsonb jsonb = JsonbBuilder.create();
    //     Map map = jsonb.fromJson(result.getText(),Map.class);
    //     Order order = new Order(); 
    //     order.productIds = new ArrayList<>();
    //     order.

        
    //     return map;
    // }

    private Result qrParser(FormData formData) throws IOException, NotFoundException {
        BufferedImage bufferedImage = ImageIO.read( formData.getQrFile());

        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Map<DecodeHintType, Object> hints = new HashMap();
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.DATA_MATRIX));
        hints.put(DecodeHintType.CHARACTER_SET, StandardCharsets.ISO_8859_1.name());
        
        MultiFormatReader multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);

        Result result = multiFormatReader.decodeWithState(bitmap);
        return result;
    }

    private Response postPayment(String jsonString) {
        System.out.println(jsonString);

        Client client = ClientBuilder.newClient();
        // Example: http://<host>/payment?orderNumber=123&email=serverless-interest@redhat.com&amount=100
        String postUrl = System.getenv("PAYMENT_SERVICE"); //"http://127.0.0.1:5000/";

        WebTarget webTarget = client.target(postUrl);
        WebTarget paymentTarget = webTarget.path("payment");
        
        String payload= jsonString;

        Response response = paymentTarget.request().post(Entity.json(payload));

        if (response.getStatus() == 200) {
            String result = response.readEntity(String.class);
            System.out.println(result);
        } else {
            System.err.println(String.format("Error processing payment.\n" + 
            "HTTP Status code: %d \n" +
            "HTTP Response: %s \n", response.getStatus(),response.readEntity(String.class)));
        }

        return response;//  ClientResponse.noContent().build();

    }

}
