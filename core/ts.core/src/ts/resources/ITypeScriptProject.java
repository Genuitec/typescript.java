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

import ts.TypeScriptException;
import ts.client.ITypeScriptClientListener;
import ts.client.ITypeScriptServiceClient;
import ts.client.geterr.ITypeScriptGeterrCollector;
import ts.client.quickinfo.ITypeScriptQuickInfoCollector;
import ts.client.signaturehelp.ITypeScriptSignatureHelpCollector;
import ts.compiler.ITypeScriptCompiler;

/**
 * TypeScript project API.
 *
 */
public interface ITypeScriptProject {

	/**
	 * Returns associated tsclient if any. This call may result in creating one
	 * if it hasn't been created already.
	 * 
	 * @return
	 * @throws TypeScriptException
	 */
	ITypeScriptServiceClient getClient() throws TypeScriptException;

	/**
	 * Returns the tsc compiler.
	 * 
	 * @return
	 */
	ITypeScriptCompiler getCompiler() throws TypeScriptException;

	void signatureHelp(ITypeScriptFile file, int position, ITypeScriptSignatureHelpCollector collector)
			throws TypeScriptException;

	void quickInfo(ITypeScriptFile file, int position, ITypeScriptQuickInfoCollector collector)
			throws TypeScriptException;

	void changeFile(ITypeScriptFile tsFile, int start, int end, String newText) throws TypeScriptException;

	void geterr(ITypeScriptFile tsFile, int delay, ITypeScriptGeterrCollector collector) throws TypeScriptException;

	ITypeScriptFile getOpenedFile(String fileName);

	void dispose() throws TypeScriptException;

	<T> T getData(String key);

	void setData(String key, Object value);

	// -------------- TypeScript server.

	void addServerListener(ITypeScriptClientListener listener);

	void removeServerListener(ITypeScriptClientListener listener);

	void disposeServer();

	void disposeCompiler();
	
	boolean isServerDisposed();

	ITypeScriptProjectSettings getProjectSettings();
}
