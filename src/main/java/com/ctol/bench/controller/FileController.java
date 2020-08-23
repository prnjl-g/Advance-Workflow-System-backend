package com.ctol.bench.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ctol.bench.model.ExcelData;
import com.ctol.bench.model.ExcelHeader;
import com.ctol.bench.model.MetaData;
import com.ctol.bench.payload.UploadFileResponse;
import com.ctol.bench.repositories.ExcelRepository;
import com.ctol.bench.repositories.HeaderRepository;
import com.ctol.bench.repositories.MetaRepository;
import com.ctol.bench.service.FileStorageService;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCursor;

@RestController
@CrossOrigin(origins = "*")
public class FileController {

    private String fileName,fileDownloadUri,finalFile;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    ExcelRepository repository;      
    
    @Autowired
    HeaderRepository headerRepo;
    
    @Autowired
    MetaRepository metaRepo;
    
    @Autowired
    MongoTemplate mongoTemplate;
  
    @PostMapping("/uploadFile")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
        fileName = fileStorageService.storeFile(file);
        
        fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();              
        
        finalFile = "C:\\Users\\"+System.getProperty("user.name")+"\\uploads\\".concat(fileName);        
                       
        String str = processExcel();
        
        if(str.equals("Success"))
        {           
            return new UploadFileResponse(fileName, fileDownloadUri,
                    file.getContentType(), file.getSize());
        }
        else
        {
        	UploadFileResponse ur = new UploadFileResponse("FAILURE");
        	return ur;
        }
 }
    	
    @RequestMapping(value="/query", method= RequestMethod.POST)
    public List<ExcelData> getResult(@RequestBody HashMap<String,List<String>> conditionMap,@RequestParam("page") int n)
    {
    	Query query = new Query();    	
    	
    	Pageable pageableRequest;
    	int k = n-1;
    	pageableRequest = PageRequest.of(k, 10);
    	
    	Iterator mapIterator = conditionMap.entrySet().iterator();    	    
    	List<String> conditions = new ArrayList<>();
    	while(mapIterator.hasNext())
    	{
    		Map.Entry mapElement = (Map.Entry)mapIterator.next();     		
    		conditions = (List<String>) mapElement.getValue();
    		query.addCriteria(Criteria.where("details."+mapElement.getKey()).in(conditions));
    	}    	
    	query.with(pageableRequest); 
    	List<ExcelData> excelRes = mongoTemplate.find(query,ExcelData.class);	
    	return excelRes;
    }
    
    @RequestMapping(value="/no-of-query-records", method= RequestMethod.POST)
    public int getResultRecords(@RequestBody HashMap<String,List<String>> conditionMap)
    {
    	Query query = new Query();    	
    	
    	Iterator mapIterator = conditionMap.entrySet().iterator();    	    
    	List<String> conditions = new ArrayList<>();
    	while(mapIterator.hasNext())
    	{
    		Map.Entry mapElement = (Map.Entry)mapIterator.next();     		
    		conditions = (List<String>) mapElement.getValue();
    		query.addCriteria(Criteria.where("details."+mapElement.getKey()).in(conditions));
    	}    	
    	List<ExcelData> excelRes = mongoTemplate.find(query,ExcelData.class);    	    	

    	int noOfRecords = excelRes.size();
    	
    	return noOfRecords;
    }
     
    @RequestMapping(value = "/metadata", method = RequestMethod.GET)
    public List<MetaData> getMetaData()
    {    
    	return metaRepo.findAll();    
    }
    
    
    @RequestMapping(value = "/getDB", method = RequestMethod.GET)    
    public List<ExcelData> getAllDocs(@RequestParam("page") int n) {    	
    	Pageable pageableRequest;
    	int k = n-1;
    	pageableRequest = PageRequest.of(k, 30);
    	Query query = new Query();
    	query.with(pageableRequest);
    	
    	List<ExcelData> pagination = mongoTemplate.find(query, ExcelData.class);
    	
    	return pagination;	
    }
    
    @RequestMapping(value = "/no-of-pages", method = RequestMethod.GET)   
    public int geNoOfPages() {       
        List<ExcelData> excelData = repository.findAll();
       
        return excelData.size();   
    }
    
    public String processExcel() throws Exception
    {
		/*
		 * Apache POI Debugging ClassLoader classloader =
		 * org.apache.poi.poifs.filesystem.POIFSFileSystem.class.getClassLoader(); URL
		 * res = classloader.getResource("org/apache/poi/util/POILogger.class"); String
		 * path = res.getPath(); return "POI came from " + path;
		 */       	                   
        FileInputStream excelFile = new FileInputStream(new File(finalFile));
        try
        {            
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(0);                                
            
            Iterator<Cell> iteratorHeader = datatypeSheet.getRow(datatypeSheet.getFirstRowNum()).iterator();
            
            ArrayList<String> headers = new ArrayList<String>();           
            
            ExcelHeader eh = new ExcelHeader();                       
            
            while (iteratorHeader.hasNext()) 
            {                
                Cell currentHeader = iteratorHeader.next();                                    
                if (currentHeader.getCellType() == CellType.STRING) 
                {                                        
                    headers.add(currentHeader.getStringCellValue());                                                
                    eh.setHeader(currentHeader.getStringCellValue());
                    
                } 
                else if (currentHeader.getCellType() == CellType.NUMERIC) 
                {
                    headers.add(""+currentHeader.getNumericCellValue());                                           
                    eh.setHeader(""+currentHeader.getNumericCellValue());
                }  
                headerRepo.save(eh);
            }
                
            boolean skipHeader = true;
                       
            
            for (Row row : datatypeSheet) {
            	
            	Map<String,String> nameMap = new HashMap<>();
            	
            	if (skipHeader) {
					skipHeader = false;
					continue;
				}
            					
				int lastColumn = headers.size();
				
				for (int cn = 0; cn < lastColumn; cn++) {
					Cell c = row.getCell(cn);					
					String s = ""+c;					
					nameMap.put( headers.get(cn), s);													
				}							
				
				ExcelData ex = new ExcelData();       
				ex.set_id(ObjectId.get());
				ex.setDetails(nameMap);
				repository.save(ex);				
			}
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            excelFile.close();
        }
        
        List<ExcelHeader> newRepo = new ArrayList<>();
    	List<String> header = new ArrayList<>();
    	newRepo = headerRepo.findAll();
    	String head;
    	Iterator it = newRepo.iterator();
    	while(it.hasNext())
    	{
    		ExcelHeader temp = (ExcelHeader) it.next();
    		head = temp.header;    		
    		header.add(head);
    	}
    	Iterator headerIterator = header.iterator();
    	Map<String, List<String>> metadata = new HashMap<>();
    	
    	MetaData md = new MetaData();
    	
    	while(headerIterator.hasNext())
    	{
    		String headValue = ""+headerIterator.next();
    		DistinctIterable<String> iterable = mongoTemplate.getCollection("excelData").distinct("details.".concat(headValue), String.class);
        	MongoCursor<String> cursor = iterable.iterator();
        	List<String> list = new ArrayList<>();
        	
        	while (cursor.hasNext()) {
        	    list.add(""+cursor.next());
        	}
        	
        	Collections.sort(list);
        	metadata.put(headValue, list);
        
    	}
    	md.setMap(metadata);
    	metaRepo.save(md);
        
        return "Success";
    }    
}


//Iterator criteriaIter = conditions.iterator();
//while(criteriaIter.hasNext())
//{
//	String crit = criteriaIter.next().toString();
//	criteria.orOperator(Criteria.where("details."+mapElement.getKey())
//			.is(crit),Criteria.where("details."+mapElement.getKey())
//			.is("Tier 2"));
//}