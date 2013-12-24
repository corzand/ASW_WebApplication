/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asw1009.model;

import asw1009.ManageXML;
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

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void getClassFields(Class aClass, Object instance) {

        String itemName = aClass.getName();
        Field[] fields = aClass.getDeclaredFields();

        Document document = null;
        Element root = null;

        try {

            ManageXML mngXML = new ManageXML();
            File f = new File(this.directoryPath + "\\" + this.fileName + ".xml");

            if (f.exists()) {
                //append
                try (InputStream in = new FileInputStream(f)) {
                    document = mngXML.parse(in);
                    root = document.getDocumentElement();
                } catch (IOException | SAXException ex) {
                    Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                //create
                document = mngXML.newDocument();
                root = document.createElement(this.fileName);
                document.appendChild(root);
            }

            Element[] itemFields = new Element[fields.length];

            for (int i = 0; i < fields.length; i++) {

                Field field = fields[i];
                field.setAccessible(true);
                String fieldName = field.getName();
                Object fieldType = field.getType();

                itemFields[i] = document.createElement(fieldName);
                itemFields[i].setAttribute("type", fieldType.toString());
                itemFields[i].setTextContent(field.get(instance).toString());

            }

            Element item = document.createElement(itemName);
            for (int i = 0; i < itemFields.length; i++) {
                item.appendChild(itemFields[i]);
            }
            
            root.appendChild(item);
            
            try (OutputStream out = new FileOutputStream(f)) {
                mngXML.transform(out, document);
            } catch (IOException | TransformerException ex) {
                Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (IllegalArgumentException | IllegalAccessException | TransformerConfigurationException | ParserConfigurationException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}