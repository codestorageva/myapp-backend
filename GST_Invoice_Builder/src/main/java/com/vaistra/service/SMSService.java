package com.vaistra.service;



import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SMSService {

//    @Value("${sms.api.url}")
//    private String apiUrl; // The URL of the Bulk SMS Gateway
//
//    @Value("${sms.api.key}")
//    private String apiKey; // Your API key for the Bulk SMS Gateway
//
//    @Value("${sms.sender.id}")
//    private String senderId; // Your Sender ID for the SMS (if applicable)

    private final RestTemplate restTemplate;

    public SMSService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendOtp(String phoneNumber, String otp) {
        // Simulating sending OTP
        System.out.println("Simulating OTP sending...");
        System.out.println("OTP: " + otp + " has been sent to phone number: " + phoneNumber);

    }

    // Method to send OTP via Bulk SMS API
//    public void sendOtp(String phoneNumber, String otp) {
//        String url = apiUrl + "/send-sms"; // Update with your specific endpoint
//        String message = "Your OTP is: " + otp;
//
//        // Constructing request body as per the Bulk SMS Gateway API documentation
//        String payload = String.format(
//                "apikey=%s&sender=%s&to=%s&message=%s",
//                apiKey, senderId, phoneNumber, message
//        );
//
//        // Make an HTTP POST request to send SMS
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-Type", "application/x-www-form-urlencoded");
//
//        HttpEntity<String> entity = new HttpEntity<>(payload, headers);
//
//        // Send the request using RestTemplate
//        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
//
//        if (response.getStatusCode().is2xxSuccessful()) {
//            System.out.println("OTP sent successfully");
//        } else {
//            System.out.println("Failed to send OTP: " + response.getBody());
//        }
//    }
}



// BULK SMS gateway configuration- Bulk sms gateway API documentation
/*
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class SMSSender
{
    public String sendSms(String sToPhoneNo,String sMessage)
    {
        try
        {
            // Construct data
            String data = "user=" + URLEncoder.encode("textlocalusername****", "UTF-8");
            data += "&password=" + URLEncoder.encode("textlocalpassword****", "UTF-8");
            data += "&message=" + URLEncoder.encode(sMessage, "UTF-8");
            data += "&sender=" + URLEncoder.encode("OPTINS", "UTF-8");
            data += "&mobile=" + URLEncoder.encode(sToPhoneNo, "UTF-8");
            data += "&type=" + URLEncoder.encode(1, "UTF-8");
            // Send data
            URL url = new URL("https://www.bulksmsgateway.in/sendmessage.php");
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();
            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            String sResult="";
            while ((line = rd.readLine()) != null)
            {
                // Process line...
                sResult=sResult+line+" ";
            }
            wr.close();
            rd.close();
            return sResult;
        }
        catch (Exception e)
        {
            System.out.println("Error SMS "+e);
            return "Error "+e;
        }
    }
} */

