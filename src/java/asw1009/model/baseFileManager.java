/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asw1009.model;

import asw1009.ManageXML;
import java.io.File;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Luca
 */
public class baseFileManager {

	public void getClassFields(Class aClass, Object instance){

		String nameClass = aClass.getName();
		Field fieldlist[] = aClass.getDeclaredFields();

		try {
			ManageXML mngXML = new ManageXML();
			Document data = mngXML.newDocument();
			Element root = data.createElement(nameClass);

			Element elements[] = new Element[fieldlist.length];
			
			for (int i = 0; i < fieldlist.length; i++) {

				Field field = fieldlist[i];
				field.setAccessible(true);
				String fieldName = field.getName();

				Object fieldType = field.getType();

				elements[i] = data.createElement(fieldName);
				elements[i].setAttribute("type", fieldType.toString());
				elements[i].setTextContent(field.get(instance).toString());

			}

			for (int i = 0; i < elements.length; i++) {
				root.appendChild(elements[i]);
			}

			data.appendChild(root);
			
			
			// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(data);
		StreamResult result = new StreamResult(new File("/file.xml"));
 
		// Output to console for testing
		// StreamResult result = new StreamResult(System.out);
 
		transformer.transform(source, result);
 
		System.out.println("File saved!");

		} catch (ParserConfigurationException e) {
			System.out.println(e);
		} catch (DOMException e) {
			System.out.println(e);
		} catch (TransformerException e) {
			System.out.println(e);
		} catch (IllegalArgumentException ex) {
			Logger.getLogger(baseFileManager.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			Logger.getLogger(baseFileManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}
