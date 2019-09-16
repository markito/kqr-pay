package org.acme.quickstart;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
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
            final ResponseBuilder response = Response.status(Status.BAD_REQUEST);
            response.header("Reason", "No QRCode file was uploaded (use form parameter 'file')");
            return response.build();
        }

        ResponseBuilder response = Response.noContent();
        try {
            // read values from QR code
            final Result qrContent = qrParser(formData.getQrFile());
            // call PAYMENT_SERVICE endpoint
            final Response paymentResponse = postPayment(qrContent.getText());
            if (paymentResponse.getStatus() == 200) {
                final String responseFromPayment = paymentResponse.readEntity(String.class);
                
                response = Response.ok(responseFromPayment, MediaType.APPLICATION_JSON);
                return response.build();
            } else {
                return response.status(Status.UNAUTHORIZED).build();
            }

        } catch (NotFoundException e) {
            System.out.println("There is no QR code in the image." + e.getMessage());
            return Response.serverError().build();
        }
    }

    private boolean hasValidExtension(String fileName) {
        final ArrayList<String> VALID_EXTENSIONS = new ArrayList<String>();
        VALID_EXTENSIONS.add("png");
        VALID_EXTENSIONS.add("jpeg");
        VALID_EXTENSIONS.add("gif");

        boolean result = false;
        for (String ext : VALID_EXTENSIONS){
            if (fileName.endsWith(ext)) {
                result = true;
            }
        }
        return result;
    }

    private Result qrParser(File uploadedFile) throws IOException, NotFoundException {

        // TODO: Add file extension validation 

        BufferedImage bufferedImage = ImageIO.read(uploadedFile);

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

        Client client = ClientBuilder.newClient();
        String postUrl = System.getenv("PAYMENT_SERVICE"); //"http://127.0.0.1:5000/";

        WebTarget webTarget = client.target(postUrl);
        // WebTarget paymentTarget = webTarget.path("payment");
        
        String payload= jsonString;

        Response response = webTarget.request().post(Entity.json(payload));

        if (response.getStatus() == 200) {
            String result = response.readEntity(String.class);            
            return Response.ok(result, MediaType.APPLICATION_JSON).build();
        } else {
            System.err.println(String.format("Error processing payment.\n" + 
            "HTTP Status code: %d \n" +
            "HTTP Response: %s \n", response.getStatus(),response.readEntity(String.class)));
            // return Response.status(503, "{'status': 'Error processing payment'}").build();
            return Response.ok("{'status': 'Error processing payment'}", MediaType.APPLICATION_JSON).build();
        }

        // return response;//  ClientResponse.noContent().build();

    }

}
