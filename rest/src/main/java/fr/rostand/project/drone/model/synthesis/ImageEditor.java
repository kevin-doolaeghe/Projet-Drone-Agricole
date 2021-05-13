package fr.rostand.project.drone.model.synthesis;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImageEditor {
    private String baseUrl = "http://localhost:8080/";
    private long id;

    private List<BufferedImage> imageList;
    private BufferedImage finalImage;

    private List<Long> positionList;
    private double lat1, lon1, lat2, lon2;
    private long latImgNb, lonImgNb;

    // ===========================================================

    public ImageEditor(long id) {
        this.id = id;
    }

    public void makeAnalysis() {
        loadPlan();
        loadImageList();
        makeFinalImage();
        //deleteCurrentFinalImage();
        saveFinalImage();
    }

    /**
     * Load plan and extract data
     */
    private void loadPlan() {
        /**
         * Send request to server for plan infos
         */
        String url = baseUrl + "/plan/" + id;
        String plan = getRequest(url);
        System.err.println("plan : " + plan + "\n");

        String temp;
        int pos;

        /**
         * Extract plan's coords and data
         */
        pos = plan.indexOf("lat1") + 6;
        temp = plan.substring(pos, plan.indexOf(",", pos));
        lat1 = Double.parseDouble(temp);

        pos = plan.indexOf("lon1") + 6;
        temp = plan.substring(pos, plan.indexOf(",", pos));
        lon1 = Double.parseDouble(temp);

        pos = plan.indexOf("lat2") + 6;
        temp = plan.substring(pos, plan.indexOf(",", pos));
        lat2 = Double.parseDouble(temp);

        pos = plan.indexOf("lon2") + 6;
        temp = plan.substring(pos, plan.indexOf(",", pos));
        lon2 = Double.parseDouble(temp);

        System.out.println("lat1:" + lat1 + ";lon1:" + lon1);
        System.out.println("lat2:" + lat2 + ";lon2:" + lon2);

        pos = plan.indexOf("latImgNb") + 10;
        temp = plan.substring(pos, plan.indexOf(",", pos));
        latImgNb = Long.parseLong(temp);

        pos = plan.indexOf("lonImgNb") + 10;
        temp = plan.substring(pos, plan.indexOf("}", pos));
        lonImgNb = Long.parseLong(temp);

        System.out.println("latImgNb:" + latImgNb + ";lonImgNb:" + lonImgNb);

        if (lat2 < lat1) {
            double t = lat1;
            lat1 = lat2;
            lat2 = t;
        }

        if (lon2 < lon1) {
            double t = lon1;
            lon1 = lon2;
            lon2 = t;
        }
    }

    /**
     * Load image list
     */
    private void loadImageList() {
        // Send request to server for images infos
        String url = baseUrl + "image/info/by-flight-plan/" + id;
        String imageInfoList = getRequest(url);
        System.err.println(imageInfoList);

        /**
         * Extract info list of JSON String
         */
        // Init variables
        int pos = 0;
        List<String> infoList = new ArrayList<>();
        // Extract image info list
        while ((pos = imageInfoList.indexOf("{", pos)) != -1) {
            infoList.add(imageInfoList.substring(pos + 1, imageInfoList.indexOf("}", pos)));
            pos++;
        }

        /**
         * Extract file name list and position list
         */
        // Init variables
        List<String> nameList = new ArrayList<>();
        positionList = new ArrayList<>();
        // Extract name list and position list
        for (String info : infoList) {
            System.out.println(info);

            pos = info.indexOf("name") + 7;
            String name = info.substring(pos, info.indexOf(",", pos) - 1);
            nameList.add(name);

            pos = info.indexOf("position") + 10;
            String temp = info.substring(pos);
            positionList.add(Long.parseLong(temp));

            System.err.println("name:" + name + ";position:" + temp);
        }

        /**
         * Download image list
         */
        // Init variables
        imageList = new ArrayList<>();
        // Download image list
        for (String name : nameList) {
            try {
                imageList.add((BufferedImage) ImageIO.read(new URL(baseUrl + "image/download/" + name)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.err.println(imageList.size() + " images loaded !\n");
    }

    private void makeFinalImage() {
        // Create image
        BufferedImage buffer = new BufferedImage(5000, 5000, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = buffer.createGraphics();

        double width = (double) buffer.getWidth() / (double) lonImgNb;
        double height = (double) buffer.getHeight() / (double) latImgNb;
        System.err.println("width=" + width + ";height=" + height);

        int index = 0;

        long line;
        long column;

        for (BufferedImage image : imageList) {
            long position = positionList.get(index);
            System.err.println("position=" + position);

            line = (position - 1) % latImgNb;
            column = (long) Math.ceil((double) position / (double) latImgNb) - 1;
            System.err.println("line:" + line + ";column:" + column);

            double x = buffer.getWidth() * column / lonImgNb;
            double y;
            if (column % 2 == 0) {
                y = buffer.getHeight() - height - ((double) buffer.getHeight() * (double) line / (double) latImgNb);
            } else {
                y = (double) buffer.getHeight() * (double) line / (double) latImgNb;
            }
            System.err.println("x=" + x + ";y=" + y);

            g2d.drawImage(image, (int) x, (int) y, (int) width, (int) height, null);
            index++;
        }
        g2d.dispose();

        finalImage = buffer;
    }

    private void deleteCurrentFinalImage() {
        String url = baseUrl + "final-image/delete/by-flight-plan/" + id;

        try {
            HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setRequestMethod("DELETE");

            int responseCode = connection.getResponseCode();
            System.out.println("DELETE Response Code : " + responseCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveFinalImage() {
        String url = baseUrl + "final-image/upload/" + id;

        try {
            // Store image in a file to send it
            long timestamp = new Date().getTime();
            File file = new File("final_img_" + id + "_" + timestamp + ".jpg");
            ImageIO.write(finalImage, "jpg", file);

            // Send request to upload final image
            MultipartUtility postRequest = new MultipartUtility(url);
            postRequest.addFilePart("file", file);
            String response = postRequest.finish(); // response from server.
            System.out.println(response);

            // Delete file from hard disk
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===========================================================

    private String getRequest(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            System.out.println("GET Response Code : " + responseCode);
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String line;
                StringBuffer response = new StringBuffer();

                while((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                return response.toString();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private class MultipartUtility {
        private HttpURLConnection httpConn;
        private DataOutputStream request;
        private final String boundary =  "*****";
        private final String crlf = "\r\n";
        private final String twoHyphens = "--";

        public MultipartUtility(String requestURL) throws Exception {
            // creates a unique boundary based on time stamp
            URL url = new URL(requestURL);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setUseCaches(false);
            httpConn.setDoOutput(true); // indicates POST method
            httpConn.setDoInput(true);

            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("Connection", "Keep-Alive");
            httpConn.setRequestProperty("Cache-Control", "no-cache");
            httpConn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + this.boundary);

            request =  new DataOutputStream(httpConn.getOutputStream());
        }

        public void addFormField(String name, String value)throws Exception  {
            request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"" + name + "\""+ this.crlf);
            request.writeBytes("Content-Type: text/plain; charset=UTF-8" + this.crlf);
            request.writeBytes(this.crlf);
            request.writeBytes(value+ this.crlf);
            request.flush();
        }

        public void addFilePart(String fieldName, File uploadFile) throws Exception {
            String fileName = uploadFile.getName();
            request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"" +
                    fieldName + "\";filename=\"" +
                    fileName + "\"" + this.crlf);
            request.writeBytes(this.crlf);

            byte[] bytes = Files.readAllBytes(uploadFile.toPath());
            request.write(bytes);
        }

        public String finish() throws Exception {
            String response ="";

            request.writeBytes(this.crlf);
            request.writeBytes(this.twoHyphens + this.boundary + this.twoHyphens + this.crlf);

            request.flush();
            request.close();

            // checks server's status code first
            int status = httpConn.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {InputStream responseStream = new BufferedInputStream(httpConn.getInputStream());

                BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));

                String line = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                responseStreamReader.close();

                response = stringBuilder.toString();
                httpConn.disconnect();
            } else {
                throw new IOException("Server returned non-OK status: " + status);
            }

            return response;
        }
    }
}
