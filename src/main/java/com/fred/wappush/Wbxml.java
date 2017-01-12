package com.fred.wappush;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


public class Wbxml {
	private static final String TAG = "Wbxml";
	
	private static int slTag;
	private static int siTag;
	private static int indicationTag;
	private static int infoTag;
	private static int itemTag;	
	
	private static byte inlineString;
	private static byte opaqueData;
	private static byte strEnd;
	private static byte tokenEnd;
	private static byte siPublicIdentifier;
	private static byte slPublicIdentifier;
	private static byte strTblRef;
	private static int minDataLen; 
	private static int maxDataLen;
	
	private static int siType;
	private static int slType;
	
	private byte[] data;
	private int dataLen;
	private int version;
	private long publicIdentifier;
	private long charset;	
	private int strTblStart;
	private long strTblLen; 
	private long uIntVar;
	
	private int siToken;
	private int indicationToken;
	private int infoToken;
	private int itemToken;
	private int slToken;
	private int index;	
	private boolean withAttribute;
	private boolean withContent;
	private boolean isWebsiteAttribute;
	private boolean isActionAttribute;	
	private boolean isPublicIdentifierString;
	private String websitePrefix;
	private String attributeName;
	private String strTbl;
	private String content;
	private StringBuilder website;
	
	public static Map<Integer, String> attributeValue = new HashMap<Integer, String>();
	public static Map<Integer, String> publicIdentifierTable = new HashMap<Integer, String>();
	
	static {
		slTag = 5;
		siTag = 5;
		indicationTag = 6;
		infoTag = 7;
		itemTag = 8;
		
		inlineString = 3;
		opaqueData = (byte)0xC3;
		strTblRef = (byte)0x83;
		strEnd = 0;
		tokenEnd = 1;	
		siPublicIdentifier = 5;
		slPublicIdentifier = 6;
		minDataLen = 6; 
		maxDataLen = 300; //XXX: make sure
		siType = 1;
		slType = 2;		
		
		attributeValue.put(0x85, ".com/");
		attributeValue.put(0x86, ".edu/");
		attributeValue.put(0x87, ".net/");
		attributeValue.put(0x88, ".org/");	
		
		publicIdentifierTable.put(0, "String table index follows");
		publicIdentifierTable.put(1, "Unknown or missing public identifier");
		publicIdentifierTable.put(2, "-//WAPFORUM//DTD WML 1.0//EN");
		publicIdentifierTable.put(3, "-//WAPFORUM//DTD WTA 1.0//EN");
		publicIdentifierTable.put(4, "-//WAPFORUM//DTD WML 1.1//EN");
		publicIdentifierTable.put(5, "-//WAPFORUM//DTD SI 1.0//EN");
		publicIdentifierTable.put(6, "-//WAPFORUM//DTD SL 1.0//EN");
		publicIdentifierTable.put(7, "-//WAPFORUM//DTD CO 1.0//EN");
		publicIdentifierTable.put(8, "-//WAPFORUM//DTD CHANNEL 1.1//EN");
		publicIdentifierTable.put(9, "-//WAPFORUM//DTD WML 1.2//EN");
		publicIdentifierTable.put(0xA, "-//WAPFORUM//DTD WML 1.3//EN");
		publicIdentifierTable.put(0xB, "-//WAPFORUM//DTD PROV 1.0//EN");
		publicIdentifierTable.put(0xC, "-//WAPFORUM//DTD WTA-WML 1.2//EN");
		publicIdentifierTable.put(0xD, "-//WAPFORUM//DTD CHANNEL 1.2//EN");
		
		for (int i = 0xE; i <= 0x7F; i++) {
			publicIdentifierTable.put(i, "Reserved");
		}
	}
	
	public Wbxml(byte[] data) {
		version = -1;
		publicIdentifier = 1;
		charset = 0;
		strTblLen = 0;
		strTblStart = 0;
		dataLen = 0;
		siToken = -1;
		indicationToken = -1;
		infoToken = -1;
		itemToken = -1;
		slToken = -1;
		index = 0;
		uIntVar = 0;
		withAttribute = false;
		withContent = false;
		isWebsiteAttribute = false;
		isActionAttribute = false;
		isPublicIdentifierString = false;
		content = null;
		strTbl = null;
		websitePrefix = null;
		attributeName = null;
		website = new StringBuilder();
		this.data = data;		
	}
	
	private void clearTagTokenFlag() {
		withAttribute = false;
		withContent = false;
	}
	
	private boolean checkSiToken(int token) {
		if (((token & 0xFF) & 0x3F) != siTag) {
			Log.e(TAG, "invalid si token:" + (token & 0xFF));
			return false;
		}
		getTagTokenFlag(token);
		Log.d(TAG, "SI" + ((withAttribute == true) ? " with attribute" : "") + ((withContent == true) ? " with content" : ""));
		// si token seems to be with content and without attribute.
		if ((withAttribute == true) || (withContent == false)) {
			Log.e(TAG, "invalid si token flag:" + (token & 0xFF));
			return false;
		}
		return true;
	}	
	
	private boolean parseSiOpaqueDataAttribute() {
		int token;
		boolean result;
		
		token = data[index++];
		if (token != opaqueData) {
			Log.e(TAG, "invalid opaque data format:" + token);
			return false;			
		}

		result = decodeUintVar();
		if (result != true) {
			Log.e(TAG, "opaque data length error.");
			return result;			
		}
		
		token = (int)uIntVar;
		index += token;
		if (index >= dataLen) {
			Log.e(TAG, "invalid opaque data format.");
			return false;			
		}
		
		Log.d(TAG, "opaque data type: " + attributeName);
		return true;
	}
	
	private boolean checkSiAttribute(int token) {
		switch (token & 0xFF)
		{
		case 5:
			attributeName = "signal-none";
			isActionAttribute = true;
			isWebsiteAttribute = false;
			break;
			
		case 6:
			attributeName = "signal-low";
			isActionAttribute = true;
			isWebsiteAttribute = false;
			break;
			
		case 7:
			attributeName = "signal-medium";
			isActionAttribute = true;
			isWebsiteAttribute = false;
			break;
			
		case 8:
			attributeName = "signal-high";
			isActionAttribute = true;
			isWebsiteAttribute = false;
			break;
			
		case 9:
			attributeName = "delete";
			isActionAttribute = true;
			isWebsiteAttribute = false;
			break;
			
		case 0xA:
			attributeName = "created";
			isActionAttribute = false;
			isWebsiteAttribute = false;
			break;
			
		case 0xB:
			attributeName = "href";
			websitePrefix = "";
			isActionAttribute = false;
			isWebsiteAttribute = true;
			break;
			
		case 0xC:
			attributeName = "href";
			websitePrefix = "http://";
			isActionAttribute = false;
			isWebsiteAttribute = true;
			break;
			
		case 0xD:
			attributeName = "href";
			websitePrefix = "http://www.";
			isActionAttribute = false;
			isWebsiteAttribute = true;
			break;
			
		case 0xE:
			attributeName = "href";
			websitePrefix = "https://";
			isActionAttribute = false;
			isWebsiteAttribute = true;
			break;
			
		case 0xF:
			attributeName = "href";
			websitePrefix = "https://www.";
			isActionAttribute = false;
			isWebsiteAttribute = true;
			break;
			
		case 0x10:
			attributeName = "si-expires";
			isActionAttribute = false;
			isWebsiteAttribute = false;
			break;
			
		case 0x11:
			attributeName = "si-id";
			isActionAttribute = false;
			isWebsiteAttribute = false;
			break;
			
		case 0x12:
			attributeName = "class";
			isActionAttribute = false;
			isWebsiteAttribute = false;
			break;

		default:
			{
				Log.e(TAG, "invalid si attribute:" + (token & 0xFF));
				return false;
			}		
		}
		return true;
	}
	
	private String getDomainName(int token) {
		return attributeValue.get(token);
	}
	
	private boolean parseWebsiteAttribute(int type) {
		boolean result;
		int token;
		int mark;
		
		token = data[index++];
		if (token != inlineString) {
			Log.e(TAG, "invalid web site format:" + token);
			return false;			
		}
		
		mark = index;
		while (data[index] != 0) {
			index++;
			if (index >= dataLen) {
				Log.e(TAG, "panic:not found the END token in web site string.");
				return false;				
			}
		}		
		
		String str = null;		
		try {
			str = new String(data, mark, (index - mark), "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;			
		}		

		website.append(websitePrefix);
		website.append(str);
		
		index++;
		token = data[index]; // may be another attribute, do not move index.
			
		/*
		 * domain name may be contained in the first string.
		 * in this case, web site resolving could be over.
		 * otherwise, the next part should be domain name and optional string by order.
		 */
		if (type == siType) {
			result = checkSiAttribute(token);
		} else {
			result = checkSlAttribute(token);
		}
		if (result == true) {
			return true;			
		}
		// the last attribute of the tag.
		if (token == tokenEnd) {
			return true;			
		}
		
		str = getDomainName(token);		
		if (str == null) {
			Log.e(TAG, "domain not exist.");
			return false;					
		}	
		
		website.append(str);
		index++; // pass domain name(one byte).
		
		// optional string.
		token = data[index]; // may be another attribute, do not move index.
		if (token == inlineString) {
			index++;
			mark = index;
			
			while (data[index] != 0) {
				index++;
				if (index >= dataLen) {
					Log.e(TAG, "not found the END token in web site string.");
					return false;				
				}
			}
			
			try {
				str = new String(data, mark, (index - mark), "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return false;			
			}		
		
			website.append(str);
			index++; // move to the next attribute.
		}
		// web site is over.
		return true;
	}
	
	private boolean parseIndicationAttribute() {
		int token;
		boolean result;
		
		while (true) {
			token = data[index++];
			
			if (token == tokenEnd) {
				break;				
			}
			
			if (index >= dataLen) {
				Log.e(TAG, "not found the END token.");
				return false;				
			}
			
			result = checkSiAttribute(token);
			if (result != true) {
				return result;				
			}
			
			if (isWebsiteAttribute == true) { // should enter once
				result = parseWebsiteAttribute(siType);
				if (result != true) {
					return result;					
				}
				Log.d(TAG, "attrName: href. attrValue: " + website);
			} else if (isActionAttribute == true) { // only one byte.
				Log.d(TAG, "attrName: action, attrValue: " + attributeName);
			} else {
				result = parseSiOpaqueDataAttribute();
				if (result != true) {
					return result;					
				}				
			}				
		}
		
		return true;		
	}
	
	private boolean parseContent() {
		int token;
		int mark;
		int loop;
		long offset;
		boolean result;
		boolean isInlineString = true;
		
		token = data[index++];
		if ((token != inlineString) && (token != strTblRef)) {
			Log.e(TAG, "invalid content format:" + token);
			return false;			
		}
		
		if (token == strTblRef) {
			isInlineString = false;
			result = decodeUintVar();
			if (result != true) {
				return result;				
			}
			
			offset = uIntVar;
			if (offset >= strTblLen) {
				Log.e(TAG, "content too long");
				return false;				
			}
			
			loop = strTblStart + (int)offset;
			Log.d(TAG, "content data come from string table.");
		} else {
			loop = index;
		}
		
		String str = null;
		mark = index;
		if (isInlineString == false) {
			mark = loop;			
		}
		while (data[loop] != 0) {
			loop++;
			if (loop >= dataLen) {
				Log.e(TAG, "invalid content format.");
				return false;				
			}
			
			if (isInlineString == true) {
				index++;
			}
		}
		
		try {
			str = new String(data, mark, (loop - mark), "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;			
		}
		content = str;
		// move to the END token.
		if (isInlineString == true) {
			index++;
		}
		Log.d(TAG, "content data:" + content);
		return true;
	}
	
	private void getTagTokenFlag(int token) {
		clearTagTokenFlag();
		if (((token & 0xFF) & 0x80) != 0) {
			withAttribute = true;
		}
		if (((token & 0xFF) & 0x40) != 0) {
			withContent = true;
		}
	}
	
	private boolean getIndicationTokenFlag(int token) {
		getTagTokenFlag(token);
		// indication seems to be with attribute.
		if (withAttribute == false) {
			Log.e(TAG, "indication without attributes!");
			return false;			
		}
		Log.d(TAG, "indication" + ((withAttribute == true) ? " with attribute" : "") + ((withContent == true) ? " with content" : ""));
		return true;
	}
	
	private boolean parseSi() {
		byte tagToken;
		boolean result;
		
		Log.d(TAG, "Start Tag: SI");
		siToken = data[index++];
		result = checkSiToken(siToken);
		if (result != true) {
			return result;			
		}
		
		while (true) {
			tagToken = data[index++];
			if (((tagToken & 0xFF) & 0x3F) == indicationTag) {
				Log.d(TAG, "Start Tag: indication");
				result = getIndicationTokenFlag(tagToken);
				if (result != true) {
					return result;					
				}
				if (withAttribute == true) { // mandatory
					result = parseIndicationAttribute();
					if (result != true) {
						return result;				
					}	
				}
				if (withContent == true) {
					result = parseContent();
					if (result != true) {
						return result;						
					}					
				}
				Log.d(TAG, "End Tag: indication");
				continue;				
			}
			// info attribute provide additional information except indication attribute.
			else if (((tagToken & 0xFF) & 0x3F) == infoTag) {
				// XXX: NOT resolve now.
				continue;
			}
			
			if (tagToken == tokenEnd) {
				break;
			} else {
				Log.e(TAG, "invalid END token:" + tagToken);
				return false;
			}				
		}
		
		Log.d(TAG, "End Tag: SI");
		return true;		
	}
	
	private boolean checkSlToken(int token) {
		if (((token & 0xFF) & 0x3F) != slTag) {
			Log.e(TAG, "invalid sl token:" + (token & 0xFF));
			return false;
		}
		getTagTokenFlag(token);
		Log.d(TAG, "SL" + ((withAttribute == true) ? " with attribute" : "") + ((withContent == true) ? " with content" : ""));
		// sl token should be with attribute.
		if (withAttribute != true) {
			Log.e(TAG, "invalid sl token flag:" + (token & 0xFF));
			return false;
		}
		return true;		
	}	
	
	private boolean checkSlAttribute(int token) {
		switch (token & 0xFF)
		{
		case 5:
			attributeName = "execute-low";
			isActionAttribute = true;
			isWebsiteAttribute = false;
			break;
			
		case 6:
			attributeName = "execute-high";
			isActionAttribute = true;
			isWebsiteAttribute = false;
			break;
			
		case 7:
			attributeName = "cache";
			isActionAttribute = true;
			isWebsiteAttribute = false;
			break;
			
		case 8:
			attributeName = "href";
			websitePrefix = "";
			isActionAttribute = false;
			isWebsiteAttribute = true;
			break;

		case 9:
			attributeName = "href";
			websitePrefix = "http://";
			isActionAttribute = false;
			isWebsiteAttribute = true;
			break;
			
		case 0xA:
			attributeName = "href";
			websitePrefix = "http://www.";
			isActionAttribute = false;
			isWebsiteAttribute = true;
			break;
			
		case 0xB:
			attributeName = "href";
			websitePrefix = "https://";
			isActionAttribute = false;
			isWebsiteAttribute = true;
			break;
			
		case 0xC:
			attributeName = "href";
			websitePrefix = "https://www.";
			isActionAttribute = false;
			isWebsiteAttribute = true;
			break;

         default:
			{
				Log.e(TAG, "invalid sl attribute:" + (token & 0xFF));
				return false;
			}		
		}
		
		return true;
	}
	
	private boolean parseSlAttribute() {
		int token;
		boolean result;
		
		while (true) {
			token = data[index++];
			if (token == tokenEnd) {
				break;				
			}
			if (index >= dataLen) {
				Log.e(TAG, "panic:not found the END token.");
				return false;				
			}
			result = checkSlAttribute(token); 
			if (result != true) {
				return result;				
			}
			if (isWebsiteAttribute == true) { // XXX: should enter once ??
				result = parseWebsiteAttribute(slType);
				if (result != true) {
					return result;					
				}	
				Log.d(TAG, "attrName: href. attrValue: " + website);
			} else { // action.
				Log.d(TAG, "attrName: action, attrValue: " + attributeName);
			}					
		}
		return true;		
	}
	
	private boolean parseSl() {
		boolean result;
		Log.d(TAG, "Start Tag: SL");
		slToken = data[index++];
		result = checkSlToken(slToken);
		if (result != true) {
			return result;			
		}
		result = parseSlAttribute();
		if (result != true) {
			return result;			
		}
		// optional
		if (withContent == true) {
			result = parseContent();
			if (result != true) {
				return result;				
			}			
		}
		Log.d(TAG, "End Tag: SL");
		return true;		
	}
	
	private boolean decodePublicIdentifier() {
		boolean result;
		byte first;
		
		first = data[index];
		if (first == 0) {
			index++;
			isPublicIdentifierString = true;
		}
		result = decodeUintVar();
		if (result != true) {
			Log.e(TAG, "public identifier multi-byte format error.");
			return result;			
		}
		/* public identifier or its index */
		publicIdentifier = uIntVar;
		Log.d(TAG, "public identifer: " + publicIdentifier + ". is string: " + isPublicIdentifierString);
		return true;
	}
	
	private boolean checkPublicIdentifier() {
		int i;
		if (isPublicIdentifierString == false) {
			String str = null;
			str = publicIdentifierTable.get((int)publicIdentifier); //must use int input
			if (str == null) {
				Log.e(TAG, "invalid public identifier:" + publicIdentifier);
				return false;
		    }				
		} else {
			if (publicIdentifier >= strTblLen) {
				Log.e(TAG, "error: public identifier index " + publicIdentifier + " pass string table len " + strTblLen);
				return false;					
			}
			
			i = 0;
			while (data[strTblStart + (int)publicIdentifier + (int)i] != 0) {
				i++;
				if (i >= strTblLen) {
					Log.e(TAG, "public identifier string too long");
					return false;						
				}					
			}
			
			String str = null;	
			try {
				str = new String(data, strTblStart + (int)publicIdentifier, i, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return false;					
			}
			
			Log.d(TAG, "public identifier is string:" + str);
			boolean found = false;
			for (i = 0; i < publicIdentifierTable.size(); i++) {
				if (str.equals(publicIdentifierTable.get(i))) {
					found = true;
					break;					
				}
			}
			
			if (found == false) {
				Log.e(TAG, "invalid public identifier");
				return false;
			}
			
			publicIdentifier = i;
		}
				
		if ((publicIdentifier != siPublicIdentifier) && (publicIdentifier != slPublicIdentifier)) {
			Log.e(TAG, "unsupported public identifier:" + publicIdentifier);
			return false;			
		}
		return true;
	}
	
	private boolean decodeCharset() {
		boolean result;
		result = decodeUintVar();
		if (result != true) {
			return result;			
		}
		charset = uIntVar;
		Log.d(TAG, "charset:" + ((charset == 0x6A) ? "UTF-8" : charset));
		return true;		
	}
	
	private boolean decodeStringTable() {
		boolean result;
		result = decodeUintVar();
		if (result != true) {
			return result;			
		}
		strTblLen = uIntVar;
		if (strTblLen > 0) {
			strTblStart = index;
			result = getStringTable();
			if (result != true) {
				return result;				
			}
			// move to the next tag token.
			index += strTblLen;
			if (index >= dataLen) {
				Log.e(TAG, "string table too long");
				return false;				
			}
		}
		
		Log.d(TAG, "String table length:" + strTblLen + " begin:" + strTblStart);
		return true;		
	}
	
	public boolean parse() {
		boolean result;
		index = 0;
		if (data == null) {
			Log.e(TAG, "wbxml data is null");
			return false;
		}
		dataLen = data.length;
		if (dataLen < minDataLen || dataLen > maxDataLen) {
			Log.e(TAG, "invalid wbxml data length:" + dataLen);
			return false;			
		}
		version = data[index++];
		result = decodePublicIdentifier();
		if (result != true) {
			return result;			
		}
		result = decodeCharset();
		if (result != true) {
			return result;			
		}
		result = decodeStringTable();
		if (result != true) {
			return result;			
		}
		result = checkPublicIdentifier();
		if (result != true) {
			return result;			
		}
		if (publicIdentifier == siPublicIdentifier) {
			return parseSi();			
		} else /*if (this.publicIdentifier == slPublicIdentifier)*/ {
			return parseSl();			
		}
	}
	
	private boolean isValidStrTblHead() {
		return (strTblStart > 3);
	}
	
	private boolean getStringTable() {
		String str = null;
		try {
			str = new String(data, index, (int)strTblLen, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
		strTbl = str;
		return true;
	}
	
	public boolean decodeUintVar() {
		long var = 0;
		int mark = index;
		while ((data[index] & 0x80) != 0) {
			if ((index - mark) >= 4) {
				Log.e(TAG, "invalid multi-byte integer length.");
				return false;				
			}
			var = (var << 7) | (data[index] & 0x7f);
            index++;
		}
		
		var = (var << 7) | (data[index] & 0x7f);
		uIntVar = var;
		index++; // move to the next byte.
		return true;		
	}	
	
	public String getWebsite() {
		return website.toString();
	}
	
	public String getContent() {
		return content;
	}
	
	public int getVersion() {
		return version;
	}
	
	public long getCharset() {
		return charset;
	}
}






