package org.b3log.symphony.util;

public class Base64 {

	public Base64() {}

	public static String encodeBase64(String str) {
		try {
			return new String(org.apache.commons.codec.binary.Base64.encodeBase64(str.getBytes("UTF-8")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String encodeBase64(byte[] data) {
		try {
			return new String(org.apache.commons.codec.binary.Base64.encodeBase64(data),"utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static String decodeBase64(String strhex) {
		return new String(org.apache.commons.codec.binary.Base64.decodeBase64(strhex.getBytes()));

	}

	public static byte[] decodeBase64ToByte(String strhex) {
		return org.apache.commons.codec.binary.Base64.decodeBase64(strhex.getBytes());
	}

	public static String encodeBase64ForByte(byte[] src) {
		try {
			return new String(org.apache.commons.codec.binary.Base64.encodeBase64(src));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static void main(String[] args) {
		System.out.println(Base64.encodeBase64("edooon"));
		System.out.println(Base64.encodeBase64("edooonmail3215"));
		System.out.println(Base64.decodeBase64("CsfXsK61xHd4eUAyMTE4LmNvbS5jbqO6CgrE+rXEw9zC69bYyejSqsfz0tG+rbXDtb3R6dakoaPH67Xju/fS1M/CwbS908rkyOvE+tDCtcTD3MLro7oKCihwbGVhZSBjbGljayBvbiB0aGUgZm9sbG93aW5nIGxpbmsgdG8gcmVzZXQgeW91ciBwYXNzd29yZDopCgpodHRwOi8vd3d3LmRvdWJhbi5jb20vbG9naW4/cmVzZXQ9cGFzc3dvcmQmY29uZmlybWF0aW9uPTc1NTlmODhjMTI2MDA5NDEKCsjnufvE+rXEZW1haWyzzNDysrvWp7PWwbS907Xju/ejrMfrvavJz8PmtcS12Na3v72xtNbBxPq1xOSvwMDG9yjA/cjnSUUptcS12Na3wLi9+Mjrtrmw6qGjCgq40NC7ttS2ubDqtcTWp7PWo6zU2bTOz6PN+8T61Nq2ubDqtcTM5dHp09DS5rrN0+S/7KGjCgq2ubDqIGh0dHA6Ly93d3cuZG91YmFuLmNvbQoKKNXiysfSu7fi19S2r7L6yfq1xGVtYWlso6zH687wu9i4tKGjKQo="));
	}
}