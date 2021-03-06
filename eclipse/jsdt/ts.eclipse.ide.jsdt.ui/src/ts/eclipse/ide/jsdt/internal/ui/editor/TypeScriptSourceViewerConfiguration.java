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

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.jsdt.internal.ui.JavaScriptPlugin;
import org.eclipse.wst.jsdt.internal.ui.text.ContentAssistPreference;
import org.eclipse.wst.jsdt.internal.ui.text.java.ContentAssistProcessor;
import org.eclipse.wst.jsdt.internal.ui.text.javadoc.JavadocCompletionProcessor;
import org.eclipse.wst.jsdt.ui.text.IColorManager;
import org.eclipse.wst.jsdt.ui.text.IJavaScriptPartitions;
import org.eclipse.wst.jsdt.ui.text.JavaScriptSourceViewerConfiguration;

import ts.eclipse.ide.jsdt.internal.ui.editor.contentassist.TypeScriptCompletionProcessor;
import ts.eclipse.ide.jsdt.internal.ui.editor.format.TypeScriptContentFormatter;
import ts.eclipse.ide.ui.utils.EditorUtils;

/**
 * Extension of JSDT {@link JavaScriptSourceViewerConfiguration}
 *
 */
public class TypeScriptSourceViewerConfiguration extends JavaScriptSourceViewerConfiguration {

	public TypeScriptSourceViewerConfiguration(IColorManager colorManager, IPreferenceStore preferenceStore,
			ITextEditor editor, String partitioning) {
		super(colorManager, preferenceStore, editor, partitioning);
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		if (getEditor() != null) {

			ContentAssistant assistant = new ContentAssistant();
			assistant.enableColoredLabels(true);
			assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

			assistant.setRestoreCompletionProposalSize(getSettings("completion_proposal_size")); //$NON-NLS-1$

			IContentAssistProcessor javaProcessor = new TypeScriptCompletionProcessor(getEditor(), assistant,
					IDocument.DEFAULT_CONTENT_TYPE);
			assistant.setContentAssistProcessor(javaProcessor, IDocument.DEFAULT_CONTENT_TYPE);

			ContentAssistProcessor singleLineProcessor = new TypeScriptCompletionProcessor(getEditor(), assistant,
					IJavaScriptPartitions.JAVA_SINGLE_LINE_COMMENT);
			assistant.setContentAssistProcessor(singleLineProcessor, IJavaScriptPartitions.JAVA_SINGLE_LINE_COMMENT);

			ContentAssistProcessor stringProcessor = new TypeScriptCompletionProcessor(getEditor(), assistant,
					IJavaScriptPartitions.JAVA_STRING);
			assistant.setContentAssistProcessor(stringProcessor, IJavaScriptPartitions.JAVA_STRING);

			assistant.setContentAssistProcessor(stringProcessor, IJavaScriptPartitions.JAVA_CHARACTER);

			ContentAssistProcessor multiLineProcessor = new TypeScriptCompletionProcessor(getEditor(), assistant,
					IJavaScriptPartitions.JAVA_MULTI_LINE_COMMENT);
			assistant.setContentAssistProcessor(multiLineProcessor, IJavaScriptPartitions.JAVA_MULTI_LINE_COMMENT);

			ContentAssistProcessor javadocProcessor = new JavadocCompletionProcessor(getEditor(), assistant);
			assistant.setContentAssistProcessor(javadocProcessor, IJavaScriptPartitions.JAVA_DOC);

			ContentAssistPreference.configure(assistant, fPreferenceStore);

			assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
			assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));

			return assistant;
		}
		return null;
	}

	/**
	 * Returns the settings for the given section.
	 *
	 * @param sectionName
	 *            the section name
	 * @return the settings
	 *
	 */
	private IDialogSettings getSettings(final String sectionName) {
		IDialogSettings settings = JavaScriptPlugin.getDefault().getDialogSettings().getSection(sectionName);
		if (settings == null)
			settings = JavaScriptPlugin.getDefault().getDialogSettings().addNewSection(sectionName);

		return settings;
	}

	@Override
	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
		return new TypeScriptContentFormatter(EditorUtils.getResource(getEditor()));
	}
}
