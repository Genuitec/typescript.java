package ts.eclipse.ide.ui.hover;

import ts.client.quickinfo.ITypeScriptQuickInfoCollector;
import ts.eclipse.ide.ui.utils.HTMLTypeScriptPrinter;

public class HTMLTypeScriptQuickInfoCollector implements ITypeScriptQuickInfoCollector {

	private String html;

	@Override
	public void setInfo(String kind, String kindModifiers, int startLine, int startOffset, int endLine, int endOffset,
			String displayString, String documentation) {
		this.html = HTMLTypeScriptPrinter.getQuickInfo(kind, kindModifiers, displayString, documentation);
	}

	public String getInfo() {
		return html;
	}

}
