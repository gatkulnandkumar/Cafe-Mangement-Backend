package com.inn.cafe.serviceImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.pdfbox.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Bill;
import com.inn.cafe.constants.CafeConstants;
import com.inn.cafe.dao.BillDao;
import com.inn.cafe.service.BillService;
import com.inn.cafe.utils.CafeUtils;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Service
public class BillServiceImpl implements BillService{
	
	@Autowired
	JwtFilter jwtFilter;
	
	@Autowired
	BillDao billDao;

	@Override
	public ResponseEntity<String> generateReport(Map<String, Object> requestMap) {
		try {
			String fileName;
			
			if(validateRequestMap(requestMap)) {
				if(requestMap.containsKey("isGenerate") && !(Boolean)requestMap.get("isGenerate")) {
					fileName = (String) requestMap.get("uuid");
				} else {
					fileName = CafeUtils.getUUID();
					requestMap.put("uuid", fileName);
					insertBill(requestMap);
				}
				
				String data = "Name: "+requestMap.get("name") +"\n"+"Contact Number: "+requestMap.get("contactNumber")+
						"\n"+"Email: "+requestMap.get("email")+"\n"+"Payment Method: "+requestMap.get("paymentMethod");
				
				Document document = new Document();
				PdfWriter.getInstance(document, new FileOutputStream(CafeConstants.STORE_LOCATION+"\\"+fileName+".pdf"));
				
				document.open();
				setRectangleInPdf(document);
				
				Paragraph chunk = new Paragraph("Cafe Management System", getFont("Header"));
				chunk.setAlignment(Element.ALIGN_CENTER);
				document.add(chunk);
				
				Paragraph paragraph = new Paragraph(data + "\n \n", getFont("Data"));
				document.add(paragraph);
				
				PdfPTable table = new PdfPTable(5);
				table.setWidthPercentage(100);
				addTableHeader(table);
				
//				JSONArray jsonArray = CafeUtils.getJsonArrayFromString((String) requestMap.get("productDetails"));
//				for(int i=0; i<jsonArray.length(); i++) {
//					addRows(table,CafeUtils.getMapFromJson(jsonArray.getString(i)));
//				}
				
				JSONArray jsonArray = CafeUtils.getJsonArrayFromString((String) requestMap.get("productDetails"));
				System.out.println("JSON Array Content: " + jsonArray.toString());
				for (int i = 0; i < jsonArray.length(); i++) {
				    JSONObject jsonObject = jsonArray.getJSONObject(i);
				    addRows(table, CafeUtils.getMapFromJson(jsonObject.toString()));
				}
				
				document.add(table);
				
				Paragraph footer = new Paragraph("Total : "+requestMap.get("totalAmount")+"\n"
						+"Thank you for visiting.Please visit again!!", getFont("Data"));
				document.add(footer);
				document.close();
				return new ResponseEntity<>("{\"uuid\":\""+fileName+"\"}", HttpStatus.OK);
			}
			return CafeUtils.getResponseEntity("Required data not found.", HttpStatus.BAD_REQUEST);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private void addRows(PdfPTable table, Map<String, Object> mapFromJson) {
		table.addCell((String) mapFromJson.get("name"));
		table.addCell((String) mapFromJson.get("category"));
		table.addCell((String) mapFromJson.get("quantity"));
		table.addCell(Double.toString((Double) mapFromJson.get("price")));
		table.addCell(Double.toString((Double) mapFromJson.get("total")));
		
	}

	private void addTableHeader(PdfPTable table) {
		Stream.of("Name","Category","Quantity","Price","Sub Total")
		        .forEach(columnTitle -> {
		        	PdfPCell header = new PdfPCell();
		        	header.setBackgroundColor(BaseColor.LIGHT_GRAY);
		        	header.setBorderWidth(2);
		        	header.setPhrase(new Phrase(columnTitle));
		        	header.setBackgroundColor(BaseColor.YELLOW);
		        	header.setHorizontalAlignment(Element.ALIGN_CENTER);
		        	header.setVerticalAlignment(Element.ALIGN_CENTER);
		        	table.addCell(header);
		        });
		
	}

	private Font getFont(String type) {
		
		switch(type) {
		    case "Header":
		    	Font headerFont= FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 18,BaseColor.BLACK);
		    	headerFont.setStyle(Font.BOLD);
		    	return headerFont;
		    case "Data":
		    	Font dataFont = FontFactory.getFont(FontFactory.TIMES_ROMAN,11,BaseColor.BLACK);
		    	dataFont.setStyle(Font.BOLD);
		    	return dataFont;
		    default:
		    	return new Font();
		}
	}

	private void setRectangleInPdf(Document document) throws DocumentException{
		
		Rectangle rect = new Rectangle(577,825,18,15);
		rect.enableBorderSide(1);
		rect.enableBorderSide(2);
		rect.enableBorderSide(4);
		rect.enableBorderSide(8);
		rect.setBorderColor(BaseColor.BLACK);
		rect.setBorderWidth(1);
		document.add(rect);
	}

	private void insertBill(Map<String, Object> requestMap) {
		try {
			Bill bill = new Bill();
			bill.setUuid((String) requestMap.get("uuid"));
			bill.setName((String) requestMap.get("name"));
			bill.setEmail((String) requestMap.get("email"));
			bill.setContactNumber((String) requestMap.get("contactNumber"));
			bill.setPaymentMethod((String) requestMap.get("paymentMethod"));
			bill.setTotal(Integer.parseInt((String) requestMap.get("totalAmount")));
			System.out.println("codeeeeee tilll here==>"+bill);
			bill.setProductDetail((String) requestMap.get("productDetails"));
			
			
//			 JSONArray productDetailsArray = new JSONArray((String) requestMap.get("productDetails"));
//			 bill.setProductDetail(productDetailsArray.toString());
			 
//			  String productDetailsJson = (String) requestMap.get("productDetails");
//		        ObjectMapper objectMapper = new ObjectMapper();
//		        JsonNode productDetailsNode = objectMapper.readTree(productDetailsJson);
//		        
//		        bill.setProductDetail(productDetailsNode.toString()); // Set the JsonNode directly
//			 System.out.println("productDetailsArray===>"+productDetailsArray.toString());
//			 System.out.println("productDetailsArray normalll===>"+productDetailsArray);
			    
			bill.setCreatedBy(jwtFilter.getCurrentUser());
			
			billDao.save(bill);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	private boolean validateRequestMap(Map<String, Object> requestMap) {
		
		return requestMap.containsKey("name") && requestMap.containsKey("contactNumber")
				&& requestMap.containsKey("email") && requestMap.containsKey("paymentMethod")
				&& requestMap.containsKey("productDetails") && requestMap.containsKey("totalAmount");
	}

	@Override
	public ResponseEntity<List<Bill>> getBills() {
		List<Bill> list = new ArrayList<>();
		if(jwtFilter.isAdmin()) {
			list = billDao.getAllBills();
		} else {
			list = billDao.getBillByUsername(jwtFilter.getCurrentUser());
		}
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap) {
		try {
			byte[] byteArray = new byte[0];
			if(!requestMap.containsKey("uuid") && validateRequestMap(requestMap)) 
				return new ResponseEntity<>(byteArray, HttpStatus.BAD_REQUEST);
				String filePath = CafeConstants.STORE_LOCATION + "\\" + (String) requestMap.get("uuid") + ".pdf";
			
				if(CafeUtils.isFileExist(filePath)) {
					byteArray = getByteArray(filePath);
					return new ResponseEntity<>(byteArray, HttpStatus.OK);
				}
				else {
					requestMap.put("isGenerate", false);
					generateReport(requestMap);
					byteArray = getByteArray(filePath);
					return new ResponseEntity<>(byteArray,HttpStatus.OK);
				}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private byte[] getByteArray(String filePath) throws Exception {
		File initialFile = new File(filePath);
		InputStream targetStream = new FileInputStream(initialFile);
		byte[] byteArray = IOUtils.toByteArray(targetStream);
		targetStream.close();
		return byteArray;
	}

	@Override
	public ResponseEntity<String> deleteBill(Integer id) {
		try {
			Optional optional = billDao.findById(id);
			if(optional.isPresent()) {
				billDao.deleteById(id);
				return CafeUtils.getResponseEntity("Bill deleted Successfully", HttpStatus.OK);
			}
			return CafeUtils.getResponseEntity("Bill id does not exist", HttpStatus.OK);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
