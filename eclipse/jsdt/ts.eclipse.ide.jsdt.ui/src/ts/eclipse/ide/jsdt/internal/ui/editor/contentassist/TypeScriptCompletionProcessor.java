package ts.eclipse.ide.jsdt.internal.ui.editor.contentassist;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.jsdt.internal.ui.text.java.JavaCompletionProcessor;
import org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext;

public class TypeScriptCompletionProcessor extends JavaCompletionProcessor {

	private final ITextEditor editor;

	public TypeScriptCompletionProcessor(ITextEditor editor, ContentAssistant assistant, String partition) {
		super(editor, assistant, partition);
		this.editor = editor;
	}

	public ITextEditor getEditor() {
		return editor;
	}
	
	@Override
	protected ContentAssistInvocationContext createContext(ITextViewer viewer, int offset) {
		return new TypeScriptContentAssistInvocationContext(viewer, offset, fEditor);
	}

}
