package ts.nodejs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import ts.TypeScriptException;
import ts.client.protocol.Request;
import ts.utils.FileUtils;

public class NodejsProcess extends AbstractNodejsProcess {

	private final File tsFile;

	/**
	 * node.js process.
	 */
	private Process process;

	/**
	 * StdOut thread.
	 */
	private Thread outThread;

	/**
	 * StdErr thread.
	 */
	private Thread errThread;

	private PrintStream out;

	public NodejsProcess(File projectDir, File tsFile, File nodejsFile, INodejsLaunchConfiguration configuration,
			String fileType) throws TypeScriptException {
		super(nodejsFile, projectDir, configuration);
		this.tsFile = checkFile(tsFile, fileType);
	}

	/**
	 * Check if the given tsserver, tsc, etc file is a valid file.
	 * 
	 * @param tsFile
	 *            the tsserver, tsc, etc file to check.
	 * @return the tsFile
	 * @throws TypeScriptException
	 */
	private static File checkFile(File tsFile, String fileType) throws TypeScriptException {
		if (tsFile == null) {
			throw new TypeScriptException("[" + fileType + "] file cannot be null");
		}
		if (!tsFile.exists()) {
			throw new TypeScriptException("Cannot find [" + fileType + "] file " + FileUtils.getPath(tsFile));
		}
		if (!tsFile.isFile()) {
			throw new TypeScriptException("[" + fileType + "] " + FileUtils.getPath(tsFile) + " is not a file.");
		}
		return tsFile;
	}

	/**
	 * StdOut of the node.js process.
	 */
	private class StdOut implements Runnable {

		@Override
		public void run() {
			try {
				try {
					notifyStartProcess(0);
					BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line = null;
					while ((line = r.readLine()) != null) {
						if (line.startsWith("{")) {
							notifyMessage(line);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (process != null) {
					process.waitFor();
				}
				kill();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * StdErr of the node.js process.
	 */
	private class StdErr implements Runnable {
		@Override
		public void run() {
			String line = null;
			InputStream is = process.getErrorStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			try {
				while ((line = br.readLine()) != null) {
					notifyErrorProcess(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void notifyErrorProcess(String line) {
		System.err.println(line);
	}

	@Override
	public void start() {
		try {
			List<String> commands = createCommands();
			ProcessBuilder builder = new ProcessBuilder(commands);
			builder.directory(getProjectDir());

			this.process = builder.start();
			this.out = new PrintStream(process.getOutputStream());

			outThread = new Thread(new StdOut());
			outThread.setDaemon(true);
			outThread.start();

			errThread = new Thread(new StdErr());
			errThread.setDaemon(true);
			errThread.start();

			// add a shutdown hook to destroy the node process in case its not
			// properly disposed
			Runtime.getRuntime().addShutdownHook(new ShutdownHookThread());

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isStarted() {
		return process != null;
	}

	private class ShutdownHookThread extends Thread {
		@Override
		public void run() {
			Process process = NodejsProcess.this.process;
			if (process != null) {
				kill();
			}
		}
	}

	/**
	 * Create process commands to start tern with node.js
	 * 
	 * @return
	 * @throws IOException
	 */
	private List<String> createCommands() {
		List<String> commands = new LinkedList<String>();
		if (nodejsFile == null) {
			// for osx, path of node.js should be setted?
			if (new File("/usr/local/bin/node").exists()) {
				commands.add("/usr/local/bin/node");
			}
			if (new File("/opt/local/bin/node").exists()) {
				commands.add("/opt/local/bin/node");
			} else {
				commands.add("node");
			}
		} else {
			commands.add(nodejsFile.getPath());
		}
		try {
			commands.add(tsFile.getCanonicalPath());
		} catch (IOException e) {
			commands.add(tsFile.getPath());
		}
		List<String> args = createNodeArgs();
		if (args != null) {
			commands.addAll(args);
		}
		return commands;
	}

	public File getProjectDir() {
		return projectDir;
	}

	/**
	 * Kill the process.
	 */
	public void kill() {
		if (out != null) {
			out.close();
			out = null;
		}
		if (process != null) {
			process.destroy();
			process = null;
			notifyStopProcess();
		}
		if (outThread != null) {
			outThread.interrupt();
			outThread = null;
		}
		if (errThread != null) {
			errThread.interrupt();
			errThread = null;
		}
	}

	/**
	 * Join to the stdout thread;
	 * 
	 * @throws InterruptedException
	 */
	public void join() throws InterruptedException {
		if (outThread != null) {
			outThread.join();
		}
	}

	@Override
	public void sendRequest(Request request) throws TypeScriptException {
		out.println(request); // add \n for "readline" used by tsserver
		out.flush();
	}
}
