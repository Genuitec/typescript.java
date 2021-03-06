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
package ts.resources;

import java.io.File;

import ts.TypeScriptException;

/**
 * TypeScript project settings API.
 *
 */
public interface ITypeScriptProjectSettings {

	/**
	 * Returns the strategy to synchronize editor content with tsserver.
	 * 
	 * @return the strategy to synchronize editor content with tsserver.
	 */
	SynchStrategy getSynchStrategy();

	/**
	 * Returns the node.js install path.
	 * 
	 * @return the node.js install path.
	 */
	File getNodejsInstallPath() throws TypeScriptException;

	/**
	 * Returns the typescript/bin/tsc file to execute.
	 * 
	 * @return the typescript/bin/tsc file to execute.
	 */
	File getTscFile() throws TypeScriptException;

	/**
	 * Returns the typescript/bin/tsserver file to execute.
	 * 
	 * @return the typescript/bin/tsserver file to execute.
	 * @throws TypeScriptException
	 */
	File getTsserverFile() throws TypeScriptException;

	/**
	 * Dispose the settings.
	 */
	void dispose();

}
