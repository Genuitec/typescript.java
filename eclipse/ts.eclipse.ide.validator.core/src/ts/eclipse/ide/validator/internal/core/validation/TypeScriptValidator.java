package ts.eclipse.ide.validator.internal.core.validation;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.wst.validation.AbstractValidator;
import org.eclipse.wst.validation.ValidationResult;
import org.eclipse.wst.validation.ValidationState;
import org.eclipse.wst.validation.internal.core.ValidationException;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;
import org.eclipse.wst.validation.internal.provisional.core.IValidatorJob;

import ts.TypeScriptException;
import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.validator.core.validation.TypeScriptValidationHelper;
import ts.eclipse.ide.validator.internal.core.Trace;

/**
 * WTP TypeScript Validator V2 to validate TypeScript. This validator can be
 * called when project is Build or Validate at hand (with Validate context
 * menu).
 */
public class TypeScriptValidator extends AbstractValidator implements IValidatorJob {

	private static final String TYPESCRIPT_VALIDATOR_CONTEXT = "ts.eclipse.ide.validator.internal.core.validation.validatorContext"; //$NON-NLS-1$

	@Override
	public void validationStarting(IProject project, ValidationState state, IProgressMonitor monitor) {
		if (project != null && TypeScriptCorePlugin.hasTypeScriptNature(project)) {
			try {
				IIDETypeScriptProject tsProject = TypeScriptCorePlugin.getTypeScriptProject(project, false);
				state.put(TYPESCRIPT_VALIDATOR_CONTEXT, tsProject);
				super.validationStarting(project, state, monitor);
			} catch (CoreException e) {
				Trace.trace(Trace.SEVERE, "Error while TypeScript start validation.", e);
			}
		}
	}

	@Override
	public void validationFinishing(IProject project, ValidationState state, IProgressMonitor monitor) {
		if (project != null && TypeScriptCorePlugin.hasTypeScriptNature(project)) {
			super.validationFinishing(project, state, monitor);
			state.put(TYPESCRIPT_VALIDATOR_CONTEXT, null);
		}
	}

	/**
	 * Perform the validation using version 2 of the validation framework.
	 */
	@Override
	public ValidationResult validate(IResource resource, int kind, ValidationState state, IProgressMonitor monitor) {
		ValidationResult result = new ValidationResult();
		IIDETypeScriptProject tsProject = (IIDETypeScriptProject) state.get(TYPESCRIPT_VALIDATOR_CONTEXT);
		if (tsProject != null && TypeScriptCorePlugin.canConsumeTsserver(resource) && tsProject.canValidate(resource)) {
			IReporter reporter = result.getReporter(monitor);

			// Here we call geterr from tsserver for the given file IResource.
			// geterr works only if file is opened. So we need to open the file
			// (even if it is not opened in an editor) and close it after.
			IIDETypeScriptFile tsFile = null;
			boolean wasOpened = false;
			try {
				String fileName = TypeScriptCorePlugin.getFileName(resource);
				// open ts file if needed
				tsFile = (IIDETypeScriptFile) tsProject.getOpenedFile(fileName);
				if (tsFile != null) {
					wasOpened = true;
				} else {
					try {
						tsFile = tsProject.openFile(resource, null);
					} catch (TypeScriptException e) {
						e.printStackTrace();
					}
				}
				if (tsFile != null) {
					// Validate ts file
					TypeScriptValidationHelper.validate(tsFile, reporter, this);
				}
			} finally {
				// close ts file if needed
				if (!wasOpened && tsFile != null) {
					try {
						tsFile.close();
					} catch (TypeScriptException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return result;
	}

	@Override
	public void cleanup(IReporter reporter) {
		// do nothing
	}

	@Override
	public void validate(IValidationContext context, IReporter reporter) throws ValidationException {
		// It seems that it is never called?
	}

	@Override
	public ISchedulingRule getSchedulingRule(IValidationContext context) {
		return null;
	}

	@Override
	public IStatus validateInJob(IValidationContext helper, IReporter reporter) throws ValidationException {
		IStatus status = Status.OK_STATUS;
		validate(helper, reporter);
		return status;
	}

}
