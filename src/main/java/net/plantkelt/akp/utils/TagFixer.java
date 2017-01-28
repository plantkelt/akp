package net.plantkelt.akp.utils;

import java.util.LinkedList;

/**
 * I struggled to find a lenient pseudo-XML parser + fixer. jTidy does not work
 * or is not enough, other parsers assume correctly formed XML data. Also, our
 * data is pseudo-XML but with a few customizations (we have fancy tags, no
 * XML-escaping, etc...)
 *
 * So I finally rolled-up my own parser. Use regexp-based string split as a
 * tokenizer, then a simple switch + tag LIFO as an ultra simple lexer state
 * machine.
 */
public class TagFixer {

	public static String fixHtml(String html) {

		StringBuffer out = new StringBuffer(html.length());
		LinkedList<String> tags = new LinkedList<>();
		boolean inTag = false;
		String currentTag = null;
		for (String token : html.split("((?<=\\<|\\>)|(?=\\<|\\>))")) {
			if (token.equals("<")) {
				// if already in tag, ignore
				inTag = true;
			} else if (token.equals(">")) {
				if (!inTag) {
					// Skip
				} else {
					if (currentTag != null) {
						if (currentTag.startsWith("/")) {
							// Close
							String closingTag = currentTag.substring(1);
							if (!tags.isEmpty()) {
								String openingTag = tags.removeLast();
								if (closingTag.equals(openingTag)) {
									// Match
									out.append('<').append(currentTag)
											.append('>');
								} else {
									// Mismatch: use opening
									out.append('<').append('/')
											.append(openingTag).append('>');
								}
							}
						} else {
							// Open
							tags.addLast(currentTag);
							out.append('<').append(currentTag).append('>');
						}
					}
					currentTag = null;
				}
				inTag = false;
			} else {
				if (inTag) {
					currentTag = token;
				} else {
					out.append(token);
				}
			}
		}
		while (!tags.isEmpty()) {
			String openingTag = tags.removeLast();
			out.append('<').append('/').append(openingTag).append('>');
		}

		return out.toString();
	}
}