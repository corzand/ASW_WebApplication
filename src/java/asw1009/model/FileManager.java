/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asw1009.model;

import asw1009.ManageXML;
import com.sun.xml.bind.StringInputStream;
import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Luca
 */
public class FileManager {

    private String directoryPath;
    private String fileName;
    protected File xml;
    protected ManageXML xmlManager;
    protected XStream _xstream ;
    
    public void init(String directoryPath, String fileName){        
        this.directoryPath = directoryPath;
        this.fileName = fileName;
        this.xml = new File(this.directoryPath + "\\" + this.fileName + ".xml");
        this._xstream = new XStream();
        try {
            this.xmlManager = new ManageXML();
        } catch (TransformerConfigurationException | ParserConfigurationException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addItem(Object instance, Class type) {
        String itemName = type.getSimpleName();
        Field[] fields = type.getDeclaredFields();
        Document document = null;
        Element root = null;
        
        if (xml.exists()) {
            //append
            try (InputStream in = new FileInputStream(xml)) {
                document = xmlManager.parse(in);
                root = document.getDocumentElement();
            } catch (IOException | SAXException ex) {
                Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            //create
            document = xmlManager.newDocument();
            root = document.createElement(this.fileName);
            document.appendChild(root);
        }
        
        Element[] itemFields = new Element[fields.length];
        for (int i = 0; i < fields.length; i++) {
            try {
                Field field = fields[i];
                field.setAccessible(true);
                String fieldName = field.getName();
                Object fieldType = field.getType();
                
                itemFields[i] = document.createElement(fieldName);
                itemFields[i].setAttribute("type", fieldType.toString());
                itemFields[i].setTextContent(field.get(instance).toString());
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
            }            
        }
        
        Element item = document.createElement(itemName);
        for (int i = 0; i < itemFields.length; i++) {
            item.appendChild(itemFields[i]);
        }
        root.appendChild(item);
        try (OutputStream out = new FileOutputStream(xml)) {
            xmlManager.transform(out, document);
        } catch (TransformerException | IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }    
    }
    
    protected void writeXML(String xmlString){
        try {
            Document document;
            try (InputStream in = new StringInputStream(xmlString)) {
                document = xmlManager.parse(in);
            }
            try (OutputStream out = new FileOutputStream(xml)) {
                xmlManager.transform(out, document);
                out.flush();
            }
        } catch (IOException | SAXException | TransformerException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected Object readXML(){
        String xmlString;
        try (InputStream in = new FileInputStream(xml)) {
            StringBuilder builder = new StringBuilder();
            int ch;
            while((ch = in.read()) != -1){
                builder.append((char)ch);
            }
            
            xmlString = builder.toString();
        
            return _xstream.fromXML(xmlString);
        } catch (IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
