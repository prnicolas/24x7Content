// Copyright (C) 2010 Patrick Nicolas
package com.c24x7;

import java.io.IOException;
import com.c24x7.util.CEnv;
import com.c24x7.util.logs.CLogger;
import com.c24x7.nlservices.CNlTrending;
import com.c24x7.proxies.CUIListener;
import java.util.Map;
import java.util.HashMap;


		/**
		 * <p>Main back-end processing routine. The arguments for the command line are
		 * <ul>
		 * <li>-h: help</li>
		 * <li>-dir: root directory for execution of the process</li>
		 * <li>-port: for the port number the process listens for GUI requests</li>
		 * </ul>
		 * </p>
		 * @author Patrick Nicolas
		 * @date 12/22/2010
		 */
public final class CMain {
	private final static String DEBUG_FILE = "debug.txt";
	
	private static Map<String, NCmdArgs> cmdArgs = null;
	static {
		cmdArgs = new HashMap<String, NCmdArgs>();
		cmdArgs.put("-h", new NCmdArgs());
		cmdArgs.put("-dir", new NCmdArgsDir());
		cmdArgs.put("-port", new NCmdArgsPort());
	}
	
	public static class NCmdArgs {
		public void execute(final String arg) {
			if(arg != null) {
				System.out.println(arg);
			}
			String help = "Command line: java -classpath PATH com.c24x7.CMain [args]\n-h help\n-dir root directory\n-port listening port";
			System.out.println(help);
			System.exit(0);
		}
	}
	
	public static class NCmdArgsDir extends NCmdArgs {
		public void execute(final String arg) {
			CEnv.init(arg, 18000);
			System.out.println("argument: project directory: " + arg);
		}
	}
	
	public static class NCmdArgsPort extends NCmdArgs {
		public void execute(final String arg) {
			try {
				CEnv.port = Integer.parseInt(arg);
			}
			catch( NumberFormatException e) {
				System.out.println("Incorrect argument: " + arg);
				System.exit(-1);
			}
		}
	}
	
	
	
	/**
	 * <p>Main routine for the java application server process.</p>
	 * @param args command line arguments as described in the online help (-h option)
	 */
	public static void main(String[] args) {
		processCmdArgs(args);
		CLogger.setLoggerClass(CMain.class, CLogger.MODE.ALL_LOG);
		CLogger.addAppender(CEnv.DEBUG_PATH, DEBUG_FILE);
		
			/*
			 * Preliminary initialization
			 */
		// test();
	
		try {
			initialize();
			CUIListener listener = new CUIListener();
			listener.start();
		}
		catch(IOException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
	}
	


	private static void processCmdArgs(String[] args) {
		if(args.length > 0) {
			if(args[0].toLowerCase().equals("-h")) {
				cmdArgs.get(args[0]).execute(null);
			}
			for(int j = 0; j < args.length; j += 2) {
				if( cmdArgs.containsKey(args[j]) && j+1 < args.length) {
					cmdArgs.get(args[j]).execute(args[j+1]);
				}
				else {
					cmdArgs.get("-h").execute("Incorrect command line arguments");
				}
			}
		}
		StringBuilder buf = new StringBuilder("com.c24x7.CMain");
		for(int j = 0; j < args.length; j++) {
			buf.append(" ");
			buf.append(args[j]);
		}
		System.out.println(buf.toString());
	}
	
	private static void initialize() throws IOException {
		CNlTrending trending = new CNlTrending();
		trending.process(null);
		System.out.println("Initialization completed");
	}
}

// ----------------------  EOF ---------------------------
