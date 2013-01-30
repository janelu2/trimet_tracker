package com.beagleapps.android.trimettrackerfree;

import org.w3c.dom.Node;


// The base class for the other Document classes used in this app
public class XMLDocument {

	protected String getAttributeValue(Node node, String itemName) {
		String attributeString = "";
		if(node != null){
			attributeString = node.getAttributes().getNamedItem(itemName).getNodeValue();
		}
		return attributeString;
	}
}
