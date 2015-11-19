package ts.server.nodejs.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import ts.server.protocol.Request;

public class NodeJSProcess {

	private final File projectDir;
	private final File tsserverFile;
	private final File nodejsFile;

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

	private final Map<Integer, Request> requestsMap;
	private final ExecutorService pool = Executors.newFixedThreadPool(1);

	/**
	 * StdOut of the node.js process.
	 */
	private class StdOut implements Runnable {

		@Override
		public void run() {
			try {
				try {
					BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line = null;
					while ((line = r.readLine()) != null) {
						if (line.startsWith("{")) {
							dispatch(line);
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

	public NodeJSProcess(File projectDir, File tsserverFile, File nodejsFile) {
		this.projectDir = projectDir;
		this.tsserverFile = tsserverFile;
		this.nodejsFile = nodejsFile;
		this.requestsMap = new HashMap<Integer, Request>();
	}

	public void notifyErrorProcess(String line) {
		System.err.println(line);
	}

	public void start() {
		try {
			List<String> commands = createCommands();
			ProcessBuilder builder = new ProcessBuilder(commands);
			// builder.redirectErrorStream(true);
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

	private class ShutdownHookThread extends Thread {
		@Override
		public void run() {
			Process process = NodeJSProcess.this.process;
			if (process != null) {
				process.destroy();
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
			commands.add(tsserverFile.getCanonicalPath());
		} catch (IOException e) {
			commands.add(tsserverFile.getPath());
		}
		// commands.addAll(createTernServerArgs());
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
		}
		if (outThread != null) {
			outThread.interrupt();
			outThread = null;
		}
		if (errThread != null) {
			errThread.interrupt();
			errThread = null;
		}
		if (pool != null) {
			pool.shutdown();
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

	public void sendRequest(Request request) throws IOException {
		out.println(request); // add \n for "readline" used by tsserver
		out.flush();
	}

	public JsonObject sendRequestSyncResponse(Request request)
			throws IOException, InterruptedException, ExecutionException {
		// long start = System.currentTimeMillis();
		sendRequest(request);
		synchronized (requestsMap) {
			requestsMap.put(request.getSeq(), request);
		}
		Future<JsonObject> f = pool.submit(request);
		JsonObject response = f.get();
		// System.err.println("time seq=" + request.getSeq() + ": " +
		// (System.currentTimeMillis() - start + "ms"));
		return response;
	}

	private void dispatch(String message) {
		JsonObject response = Json.parse(message).asObject();
		String type = response.getString("type", "");

		if ("event".equals(type) && response.getString("event", null) != null) {

		} else if ("response".equals(type)) {
			int seq = response.getInt("request_seq", -1);
			if (seq != -1) {
				synchronized (requestsMap) {
					Request c = requestsMap.remove(seq);
					c.setResponse(response);
				}
			}
		}

	}

}