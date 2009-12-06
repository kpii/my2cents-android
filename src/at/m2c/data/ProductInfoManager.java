package at.m2c.data;

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

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Tweet;
import twitter4j.TwitterException;

import android.graphics.Bitmap;
import android.util.Log;
import at.m2c.util.AwsSignedRequestsHelper;
import at.m2c.util.NetworkManager;


public final class ProductInfoManager {
	private final static String TAG = "ProductInfoManager";
	
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

	
	
	public static final void updateProductInfo(ProductInfo productInfo) {
		updateFromAmazon(productInfo);
	}
	
	private static final boolean updateFromAmazon(ProductInfo productInfo) {
		AwsSignedRequestsHelper helper;
        try {
            helper = AwsSignedRequestsHelper.getInstance(ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);
        } catch (Exception e) {
            return false;
        }
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("Service", "AWSECommerceService");
        params.put("Version", "2009-10-01");
        params.put("Operation", "ItemLookup");
        params.put("Condition", "All");
        params.put("IdType", "EAN");
        params.put("SearchIndex", "All");
        params.put("ItemId", productInfo.getProductCode());
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
            
            NodeList nodes = dom.getElementsByTagName("Title");
            Node node = nodes.item(0);
            String title = node.getLastChild().getNodeValue();
            productInfo.setProductName(title);
            
            nodes = dom.getElementsByTagName("ThumbnailImage");
            node = nodes.item(0);
            NodeList children = node.getChildNodes();
            node = children.item(0);
            String imageUrlString = node.getLastChild().getNodeValue();
            URL imageUrl = new URL(imageUrlString);
            Bitmap image = NetworkManager.getRemoteImage(imageUrl);
            productInfo.setProductImage(image);
            
            stream.close();
        } catch (Exception e) {
        	return false;
        }

		return true;
	}
	
	public final static List<ProductInfo> getProductsFromAmazon(String searchTerm){
		AwsSignedRequestsHelper helper;
        try {
            helper = AwsSignedRequestsHelper.getInstance(ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);
        } catch (Exception e) {
            return null;
        }
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("Service", "AWSECommerceService");
        params.put("Version", "2009-10-01");
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
            		
            		ProductInfo productInfo = new ProductInfo(searchTerm);
                    productInfo.setProductInfoProvider("Amazon");
                    
                    String title = element.getElementsByTagName("Title").item(0).getLastChild().getNodeValue();
                    productInfo.setProductName(title);
                    
                    String imageUrlString = element.getElementsByTagName("ThumbnailImage").item(0).getChildNodes().item(0).getLastChild().getNodeValue();
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
}
