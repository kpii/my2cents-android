package at.my2c.data;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import at.my2c.utils.AwsSignedRequestsHelper;

public final class ProductInfoManager {
	
	public static String UnknownProductName;
	
	/*
     * Your AWS Access Key ID, as taken from the AWS Your Account page.
     */
    private static final String AWS_ACCESS_KEY_ID = "AKIAJGS22KMPWBW5VWAA";

    /*
     * Your AWS Secret Key corresponding to the above ID, as taken from the AWS
     * Your Account page.
     */
    private static final String AWS_SECRET_KEY = "jQeIfCW/JJXiwc1WEC35Cq6B/mxQD/28mIYcbn71";

    /*
     * Use one of the following end-points, according to the region you are
     * interested in:
     * 
     *      US: ecs.amazonaws.com 
     *      CA: ecs.amazonaws.ca 
     *      UK: ecs.amazonaws.co.uk 
     *      DE: ecs.amazonaws.de 
     *      FR: ecs.amazonaws.fr 
     *      JP: ecs.amazonaws.jp
     * 
     */
    private static final String ENDPOINT = "ecs.amazonaws.com";
	
	
	public final static List<ProductInfo> getProductsFromAmazon(String searchTerm){
		AwsSignedRequestsHelper helper;
        try {
            helper = AwsSignedRequestsHelper.getInstance(ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);
        } catch (Exception e) {
            return null;
        }
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("Service", "AWSECommerceService");
        params.put("Version", "2009-11-01");
        params.put("Operation", "ItemLookup");
        params.put("Condition", "All");
        params.put("IdType", "EAN");
        params.put("SearchIndex", "All");
        params.put("ItemId", searchTerm);
        params.put("ResponseGroup", "ItemAttributes,Images");

        String requestUrl = helper.sign(params);
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            URL url = new URL(requestUrl);
            URLConnection connection = url.openConnection();
            connection.connect();
            InputStream stream = connection.getInputStream();
            
            Document dom = builder.parse(stream);
            
            List<ProductInfo> items = new ArrayList<ProductInfo>();
            
            NodeList itemNodes = dom.getElementsByTagName("Item");
            for (int i=0; i<itemNodes.getLength(); i++) {
            	Node node = itemNodes.item(i);
            	if (node.getNodeType() == Node.ELEMENT_NODE) {
            		Element element = (Element) node;
            		
            		String productId = element.getElementsByTagName("ASIN").item(0).getLastChild().getNodeValue();
            		ProductInfo productInfo = new ProductInfo(productId);
            		productInfo.setProductCode(searchTerm);
            		
            		String manufacturer = element.getElementsByTagName("Manufacturer").item(0).getLastChild().getNodeValue();
                    productInfo.setManufacturer(manufacturer);
                    
                    String detailPageUrl = element.getElementsByTagName("DetailPageURL").item(0).getLastChild().getNodeValue();
                    productInfo.setDetailPageUrl(detailPageUrl);
                    
                    String title = element.getElementsByTagName("Title").item(0).getLastChild().getNodeValue();
                    productInfo.setProductName(title);
                    
                    String imageUrlString = element.getElementsByTagName("MediumImage").item(0).getChildNodes().item(0).getLastChild().getNodeValue();
                    productInfo.setProductImageUrl(imageUrlString);
                    
                    items.add(productInfo);
            	}
             }
            
            stream.close();
            return items;
        } catch (Exception e) {
        	return null;
        }
	}
	
	public final static ProductInfo getProductFromAmazon(String searchTerm){
		AwsSignedRequestsHelper helper;
        try {
            helper = AwsSignedRequestsHelper.getInstance(ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);
        } catch (Exception e) {
            return null;
        }
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("Service", "AWSECommerceService");
        params.put("Version", "2009-11-01");
        params.put("Operation", "ItemLookup");
        params.put("Condition", "All");
        params.put("IdType", "EAN");
        params.put("SearchIndex", "All");
        params.put("ItemId", searchTerm);
        params.put("ResponseGroup", "ItemAttributes,Images");

        String requestUrl = helper.sign(params);
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            URL url = new URL(requestUrl);
            URLConnection connection = url.openConnection();
            connection.connect();
            InputStream stream = connection.getInputStream();
            
            Document dom = builder.parse(stream);
            
            ProductInfo productInfo = null;
            NodeList itemNodes = dom.getElementsByTagName("Item");
            for (int i=0; i<itemNodes.getLength(); i++) {
            	Node node = itemNodes.item(i);
            	if (node.getNodeType() == Node.ELEMENT_NODE) {
            		Element element = (Element) node;
            		
            		String productId = element.getElementsByTagName("ASIN").item(0).getLastChild().getNodeValue();
            		productInfo = new ProductInfo(productId);
            		productInfo.setProductCode(searchTerm);
            		
            		String manufacturer = element.getElementsByTagName("Manufacturer").item(0).getLastChild().getNodeValue();
                    productInfo.setManufacturer(manufacturer);
                    
                    String detailPageUrl = element.getElementsByTagName("DetailPageURL").item(0).getLastChild().getNodeValue();
                    productInfo.setDetailPageUrl(detailPageUrl);
                    
                    String title = element.getElementsByTagName("Title").item(0).getLastChild().getNodeValue();
                    productInfo.setProductName(title);
                    
                    String imageUrlString = element.getElementsByTagName("MediumImage").item(0).getChildNodes().item(0).getLastChild().getNodeValue();
                    productInfo.setProductImageUrl(imageUrlString);
                    
                    break;
            	}
             }
            
            stream.close();
            return productInfo;
        } catch (Exception e) {
        	return null;
        }
	}
}
