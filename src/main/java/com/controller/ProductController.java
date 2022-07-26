package com.controller;


import com.entities.Product;
import com.entities.product.ProductModel;
import com.helper.ExcelHelper;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.util.List;

@WebServlet("/product")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 10, //10MB
        maxFileSize = 1024 * 1024 * 50, //50MB
        maxRequestSize = 1024 * 1024 * 100 //100MB
)
public class ProductController extends HttpServlet {

    private static final String UPLOAD_DIR = "assets";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            request.getRequestDispatcher("index.jsp").forward(request, response);
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action.equalsIgnoreCase("import")) {
            try{
                ProductModel productModel = new ProductModel();
                String excelFileName = uploadFile(request);
                String applicationPath = request.getServletContext().getRealPath("");
                String basePath = applicationPath + "assets" + File.separator;
                ExcelHelper excelHelper = new ExcelHelper(basePath + excelFileName);
                List<Product> productList = excelHelper.readData(Product.class.getName());
                for (Product product : productList) {
                    productModel.create(product);
                }
                request.setAttribute("products", productList);
                request.getRequestDispatcher("index.jsp").forward(request, response);
            }catch (Exception e) {
                request.setAttribute("error", "Can't import excel file");
                request.getRequestDispatcher("index.jsp").forward(request, response);
            }
        }

    }

    private String uploadFile(HttpServletRequest request) {
        String fileName = "";
        try {
            Part filePart = request.getPart("excelFile");
            fileName = getFileName(filePart);
            String applicationPath = request.getServletContext().getRealPath("");
            String basePath = applicationPath + File.separator + UPLOAD_DIR + File.separator;
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                File outputFilePath = new File(basePath + fileName);
                inputStream = filePart.getInputStream();
                outputStream = new FileOutputStream(outputFilePath);
                int read = 0;
                final byte[] bytes = new byte[1024];
                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            fileName = "";
        }
        return fileName;
    }


    private String getFileName(Part part) {
        final String partHeader = part.getHeader("content-disposition");
        for (String content : partHeader.split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }
}
