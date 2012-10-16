package net.plantkelt.akp.webapp;

public class Regexp {

	public static void main(String[] args) {
		if ("2010 - Naaa".matches("^[0-9]+ - .*"))
				System.err.println("OK!");
		else
			System.err.println("NOK...");
	}
}
