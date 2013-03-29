package com.vaguehope.onosendai.provider.successwhale;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class PostToAccountsXml implements ContentHandler {

	private final List<PostToAccount> accounts = new ArrayList<PostToAccount>();

	public PostToAccountsXml (final InputStream dataIs) throws SAXException {
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp;
		try {
			sp = spf.newSAXParser();
			XMLReader xmlReader = sp.getXMLReader();
			xmlReader.setContentHandler(this);
			try {
				xmlReader.parse(new InputSource(dataIs));
			}
			catch (IOException e) {
				throw new SAXException(e);
			}
		}
		catch (ParserConfigurationException e) {
			throw new SAXException(e);
		}
	}

	public List<PostToAccount> getAccounts () {
		return this.accounts;
	}

	private final Stack<String> stack = new Stack<String>();
	private StringBuilder currentText;
	private String stashedService;
	private String stashedUsername;
	private String stashedUid;
	private boolean stashedEnabled;

	@Override
	public void startElement (final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
		this.stack.push(localName);
		if (this.currentText == null || this.currentText.length() > 0) {
			this.currentText = new StringBuilder();
		}
	}

	@Override
	public void endElement (final String uri, final String localName, final String qName) throws SAXException {
		final String elementName = !localName.isEmpty() ? localName : qName;
		if (this.stack.size() == 3) {
			if ("posttoaccount".equals(elementName)) {
				this.accounts.add(new PostToAccount(this.stashedService, this.stashedUsername, this.stashedUid, this.stashedEnabled));
				this.stashedService = null;
				this.stashedUsername = null;
				this.stashedUid = null;
				this.stashedEnabled = false;
			}
		}
		else if (this.stack.size() == 4) {
			if ("service".equals(elementName)) {
				this.stashedService = this.currentText.toString();
			}
			else if ("username".equals(elementName)) {
				this.stashedUsername = this.currentText.toString();
			}
			else if ("uid".equals(elementName)) {
				this.stashedUid = this.currentText.toString();
			}
			else if ("enabled".equals(elementName)) {
				this.stashedEnabled = Boolean.parseBoolean(this.currentText.toString());
			}
		}

		this.stack.pop();
	}

	@Override
	public void characters (final char[] ch, final int start, final int length) throws SAXException {
		this.currentText.append(ch, start, length);
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	@Override
	public void endDocument () throws SAXException { /* UNUSED */}

	@Override
	public void endPrefixMapping (final String prefix) throws SAXException { /* UNUSED */}

	@Override
	public void ignorableWhitespace (final char[] ch, final int start, final int length) throws SAXException { /* UNUSED */}

	@Override
	public void processingInstruction (final String target, final String data) throws SAXException { /* UNUSED */}

	@Override
	public void setDocumentLocator (final Locator locator) { /* UNUSED */}

	@Override
	public void skippedEntity (final String name) throws SAXException { /* UNUSED */}

	@Override
	public void startDocument () throws SAXException { /* UNUSED */}

	@Override
	public void startPrefixMapping (final String prefix, final String uri) throws SAXException { /* UNUSED */}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}
