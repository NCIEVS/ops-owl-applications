package org.LexGrid.LexBig.dataQA;
/*
 * Copyright: (c) 2004-2007 Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 * 
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 * 		http://www.eclipse.org/legal/epl-v10.html
 * 
 */

import java.io.PrintWriter;

import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeSummary;
import org.LexGrid.LexBIG.admin.CodingSchemeSelectionMenu;

/**
 * Utility functions to support the examples.
 * 
 * @author <A HREF="mailto:johnson.thomas@mayo.edu">Thomas Johnson</A>
 */
class Util {
    static final private String _lineReturn = System.getProperty("line.separator");
	static final private PrintWriter _printWriter = new PrintWriter(System.out); 

	/**
	 * Outputs messages to the error log and console,
	 * with additional tagging to assist servicability.
	 * @param message The message to display.
	 * @param cause Error associated with the message. 
	 */
	static void displayAndLogError(String message, Throwable cause) {
		displayTaggedMessage(message);
	}

	/**
	 * Outputs messages to the error log and console,
	 * with additional tagging to assist servicability.
	 * @param cause Error associated with the message. 
	 */
	static void displayAndLogError(Throwable cause) {
		displayAndLogError(cause.getMessage(), cause);
	}

	/**
	 * Displays a message to the console.
	 * @param message The message to display.
	 */
	static void displayMessage(String message) {
		try {
			_printWriter.println(message);
		} finally {
			_printWriter.flush();
		}
	}

	/**
	 * Displays a message to the console, with additional
	 * tagging to assist servicability.
	 * @param message The message to display.
	 */
	static void displayTaggedMessage(String message) {
		displayTaggedMessage(message, null, null);
	}

	/**
	 * Displays a message to the console, with additional
	 * tagging to assist servicability.
	 * @param message The message to display.
	 * @param cause Optional error associated with the message. 
	 * @param logID Optional identifier as registered in the LexBIG logs.
	 */
	static void displayTaggedMessage(String message, Throwable cause, String logID) {
		StringBuffer sb = new StringBuffer("[LB] ").append(message);
		if (cause != null) {
			String causeMsg = cause.getMessage();
			if (causeMsg != null && !causeMsg.equals(message)) {
				sb.append(_lineReturn).append("\t*** Cause: ").append(causeMsg);
			}
		}
		if (logID != null) {
			sb.append(_lineReturn).append("\t*** Refer to message with ID = ").append(logID).append(" in the log file.");
		}
		displayMessage(sb.toString());
	}
	
	/**
	 * Display a list of available code systems and 
	 * @return The coding scheme summary for the selected code system;
	 * null if no valid selection was made.
	 */
	static CodingSchemeSummary promptForCodeSystem() {
		return new CodingSchemeSelectionMenu().displayAndGetSelection();		
	}
}