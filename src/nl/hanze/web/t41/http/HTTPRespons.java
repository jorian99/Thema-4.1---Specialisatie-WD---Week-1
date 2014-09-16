package nl.hanze.web.t41.http;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import sun.misc.BASE64Encoder;

public class HTTPRespons extends RecursiveAction {

    private OutputStream out;
    private HTTPRequest request;

    public HTTPRespons(OutputStream out) {
        this.out = out;
    }

    public void setRequest(HTTPRequest request) {
        this.request = request;
    }

    public void sendResponse() throws IOException {
        byte[] bytes = new byte[HTTPSettings.BUFFER_SIZE];
        FileInputStream fis = null;
        String fileName = request.getUri();        

        try {
            File file = new File(HTTPSettings.DOC_ROOT, fileName);
            FileInputStream inputStream = getInputStream(file);

            if (file.exists()) {
                out.write(getHTTPHeader(fileName));
            } else {
                out.write(getHTTPHeader(""));
            }
            String fileType = getFileType(fileName);
            if (file.exists() && Arrays.asList(HTTPSettings.SUPPORTED_IMAGETYPES).contains(fileType)) {
                BufferedImage img = ImageIO.read(file);
                BufferedImage newImg;
                String imgstr = encodeToString(img, fileType);
                System.out.println(imgstr);
                out.write(imgstr.getBytes());
            } else {
            
                int ch = inputStream.read(bytes, 0, HTTPSettings.BUFFER_SIZE);
                while (ch != -1) {
                    out.write(bytes, 0, ch);
                    ch = inputStream.read(bytes, 0, HTTPSettings.BUFFER_SIZE);
                }
              }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                fis.close();
            }
        }

    }

    private FileInputStream getInputStream(File file) throws IOException {
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(file);

        } catch (FileNotFoundException e) {

            File notFound = new File(HTTPSettings.DOC_ROOT, HTTPSettings.FILE_NOT_FOUND);
            fis = new FileInputStream(notFound);

        }

        return fis;

    }

    private byte[] getHTTPHeader(String fileName) throws IOException {
        String fileType = getFileType(fileName);
        File file = new File(HTTPSettings.DOC_ROOT, fileName);        
        
        String statusCode;
        if (fileName.equals("")) {
            statusCode = "404 Not Found"; 
            file = new File(HTTPSettings.DOC_ROOT, HTTPSettings.FILE_NOT_FOUND);
        } else if (!Arrays.asList(HTTPSettings.SUPPORTED_FILETYPES).contains(fileType)
                && !Arrays.asList(HTTPSettings.SUPPORTED_IMAGETYPES).contains(fileType)) {
            statusCode = "500 Internal Server Error";
            file = new File(HTTPSettings.DOC_ROOT, HTTPSettings.FILE_NOT_FOUND);
        } else {
            statusCode = "200 OK";
        }
        
        long contentLength = file.length();
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentType = fileNameMap.getContentTypeFor("file://" + fileName);
        
        String header = "HTTP/1.1 " + statusCode + "\n"                
                + "Content-Type: " + contentType + "\n"
                + "Content-Length: " + contentLength + "\n"
//                + "Content-Encoding: BASE64\n"                
                + "\n";
        
        System.out.println("Response:\n" + header);        
        
        byte[] rv = header.getBytes();
        return rv;
    }

    private String getFileType(String fileName) {
        int i = fileName.lastIndexOf(".");
        String ext = "";
        if (i > 0 && i < fileName.length() - 1) {
            ext = fileName.substring(i + 1);
        }

        return ext;
    }

    private void showResponse(byte[] respons) {
        StringBuffer buf = new StringBuffer(HTTPSettings.BUFFER_SIZE);

        for (int i = 0; i < respons.length; i++) {
            buf.append((char) respons[i]);
        }
        System.out.print(buf.toString());

    }
    
    /**
     * Encode image to string
     * @param image The image to encode
     * @param type jpeg, bmp, ...
     * @return encoded string
     */
    public static String encodeToString(BufferedImage image, String type) {
        String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, type, bos);
            byte[] imageBytes = bos.toByteArray();

            BASE64Encoder encoder = new BASE64Encoder();
            imageString = encoder.encode(imageBytes);

            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageString;
    }    

    @Override
    protected void compute() {
        byte[] bytes = new byte[HTTPSettings.BUFFER_SIZE];
        FileInputStream fis = null;
        String fileName = request.getUri();        

        try {
            File file = new File(HTTPSettings.DOC_ROOT, fileName);
            FileInputStream inputStream = getInputStream(file);

            if (file.exists()) {
                out.write(getHTTPHeader(fileName));
            } else {
                out.write(getHTTPHeader(""));
            }
            String fileType = getFileType(fileName);
            if (file.exists() && Arrays.asList(HTTPSettings.SUPPORTED_IMAGETYPES).contains(fileType)) {
                BufferedImage img = ImageIO.read(file);
                BufferedImage newImg;
                String imgstr = encodeToString(img, fileType);
                System.out.println(imgstr);
                out.write(imgstr.getBytes());
            } else {
            
                int ch = inputStream.read(bytes, 0, HTTPSettings.BUFFER_SIZE);
                while (ch != -1) {
                    out.write(bytes, 0, ch);
                    ch = inputStream.read(bytes, 0, HTTPSettings.BUFFER_SIZE);
                }
              }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                    Logger.getLogger(HTTPRespons.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }      
    }

}
