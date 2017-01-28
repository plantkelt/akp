package net.plantkelt.akp.utils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 */
public class TagFixerTest extends TestCase {

	public static Test suite() {
		return new TestSuite(TagFixerTest.class);
	}

	/**
	 */
	public void testFixTag() {
		// Correct
		testOne("<l><b>Draba verna</b> <a>L.</a> subsp. <e>glabrescens</e> (<a>Jord.</a>) <a>Rouy</a> & <a>Foucaud</a></l>",
				"<l><b>Draba verna</b> <a>L.</a> subsp. <e>glabrescens</e> (<a>Jord.</a>) <a>Rouy</a> & <a>Foucaud</a></l>");

		// Missing closing tag at the end
		testOne("<l><b>Foo bar</b>", "<l><b>Foo bar</b></l>");

		// Inverted tag
		testOne("<l><b>Foo bar</l></b>", "<l><b>Foo bar</b></l>");

		// Incorrectly closed tag
		testOne("<l><b>Foo bar/b> <a>Smith</a></l>",
				"<l><b>Foo bar/b <a>Smith</a></b></l>");

		// Incorrectly opening tag
		testOne("<l>b>Foo bar</b> <a>Smith</a></l>",
				"<l>bFoo bar</l> <a>Smith</a>");

		// Incorrectly opening tag 2
		testOne("<l><<b>Foo bar</b> <a>Smith</a></l>",
				"<l><b>Foo bar</b> <a>Smith</a></l>");
	}

	private void testOne(String toCorrect, String expected) {
		String corrected = TagFixer.fixHtml(toCorrect);
		// System.out.println("------------");
		// System.out.println(toCorrect);
		// System.out.println(corrected);
		// System.out.println(expected);
		assertEquals(expected, corrected);
	}
}
