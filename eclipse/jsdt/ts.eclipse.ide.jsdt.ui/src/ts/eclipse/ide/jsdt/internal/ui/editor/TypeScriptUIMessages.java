/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.jsdt.internal.ui.editor;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

/**
 * TypeScript UI messages.
 *
 */
public class TypeScriptUIMessages extends NLS {

	private static final String BUNDLE_NAME = "ts.eclipse.ide.jsdt.internal.ui.editor.TypeScriptUIMessages"; //$NON-NLS-1$

	private static ResourceBundle fResourceBundle;

	// Action
	public static String GotoMatchingBracket_label;
	public static String GotoMatchingBracket_error_invalidSelection;
	public static String GotoMatchingBracket_error_noMatchingBracket;
	public static String GotoMatchingBracket_error_bracketOutsideSelectedElement;

	public static ResourceBundle getResourceBundle() {
		try {
			if (fResourceBundle == null)
				fResourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);
		} catch (MissingResourceException x) {
			fResourceBundle = null;
		}
		return fResourceBundle;
	}

	static {
		NLS.initializeMessages(BUNDLE_NAME, TypeScriptUIMessages.class);
	}
}
