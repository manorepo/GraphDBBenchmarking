package edu.prism;

import com.google.common.collect.Iterators;
import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.branch.LoopPipe;
import com.tinkerpop.pipes.util.iterators.SingleIterator;

import org.apache.commons.cli.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 */
public class TitanApp {

	public static final String PROPS_PATH = "titan-cassandra.properties";

	String graphFile = "C:\\Users\\Manohar\\Desktop\\AOS\\GraphFiles\\6200v100Ke";
	TitanGraph graph;

	static Options options = new Options();

	private static void buildOptions() {
		// build option tables

		options.addOption(new Option("help", "print this message"));

		options.addOption(Option.builder("op").hasArg().desc("test operations;").build());

		options.addOption(Option.builder("id").hasArg().desc("starting from this vertex").build());

		options.addOption(Option.builder("step").hasArg().desc("travel stps").build());

	}

	public static String[] parseArgs(String[] args) {
		String[] rst = new String[3];
		CommandLineParser parser = new DefaultParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			if (args.length == 0) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("prism-titan", options);
				System.exit(0);
			}

			if (line.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("prism-titan", options);
				System.exit(0);
			}

			if (line.hasOption("op")) {
				rst[0] = line.getOptionValue("op", "insert");
			} else {
				throw new ParseException("argument 'op' is required.");
			}

			if (line.hasOption("id")) {
				rst[1] = line.getOptionValue("id", "0");
			}

			if (line.hasOption("step")) {
				rst[2] = line.getOptionValue("step", "1");
			}

		} catch (ParseException exp) {
			System.out.println("Arguments Error:" + exp.getMessage());
			System.exit(-1);
		}
		return rst;
	}

	public void init() throws ConfigurationException {
		Configuration conf = new PropertiesConfiguration(PROPS_PATH);
		this.graph = TitanFactory.open(conf);
	}

	public void initMemoryDB() throws ConfigurationException {
		this.graph = TitanFactory.build().set("storage.backend", "inmemory").open();
		System.out.println("Performance Evaluation For Graph File: " + graphFile);
		System.out.println("-------------------------------------------");
	}

	
	public void insert() throws IOException {
		TitanTransaction tx = graph.newTransaction();
		int writes = 0;
		HashMap<ByteBuffer, TitanVertex> inserted = new HashMap<>();
		BufferedReader br = new BufferedReader(new FileReader(graphFile));
		String line;
		long startTime = 0;
		long endTime = 0;

		TitanManagement mgmt = graph.getManagementSystem();
		final PropertyKey vidKey = mgmt.makePropertyKey("vid").dataType(String.class).make();
		final PropertyKey valKey = mgmt.makePropertyKey("val").dataType(String.class).make();
		mgmt.makeEdgeLabel("Run").multiplicity(Multiplicity.MULTI).make();
		mgmt.makeVertexLabel("Node").make();
		mgmt.commit();
		startTime = System.currentTimeMillis();
		while ((line = br.readLine()) != null) {
			String[] splits = line.split(" ");
			String src = splits[0];
			ByteBuffer bsrc = ByteBuffer.wrap(src.getBytes());
			String dst = splits[1];
			ByteBuffer bdst = ByteBuffer.wrap(dst.getBytes());
			String val = splits[2];

			TitanVertex vsrc, vdst;

			if (!inserted.containsKey(bsrc)) {
				vsrc = graph.addVertexWithLabel("Node");
				vsrc.setProperty("vid", src);
				inserted.put(bsrc, vsrc);
			} else
				vsrc = inserted.get(bsrc);

			if (!inserted.containsKey(bdst)) {
				vdst = graph.addVertexWithLabel("Node");
				vdst.setProperty("vid", dst);
				inserted.put(bdst, vdst);
			} else
				vdst = inserted.get(bdst);

			TitanEdge e = vsrc.addEdge("Run", vdst);
			e.setProperty("val", val);

			writes++;
			if (writes % 100 == 0) {
				tx.commit();
				tx = graph.newTransaction();
			}
		}
		tx.commit();
		endTime = System.currentTimeMillis();
		System.out.println("The total time taken for Graph Update is " + (endTime - startTime) + " ms");

	}

	public void singleInsert(int id) throws IOException, InterruptedException {
		TitanTransaction tx = graph.newTransaction();
		int writes = 0;

		/*
		 * if (id == 0) { TitanManagement mgmt = graph.getManagementSystem();
		 * final PropertyKey vidKey =
		 * mgmt.makePropertyKey("vid").dataType(String.class).make(); final
		 * PropertyKey valKey =
		 * mgmt.makePropertyKey("val").dataType(String.class).make();
		 * mgmt.makeEdgeLabel("Run").multiplicity(Multiplicity.MULTI).make();
		 * mgmt.makeVertexLabel("Node").make(); mgmt.commit(); } else {
		 * Thread.sleep(1000); }
		 */
		String val = "";
		for (int i = 0; i < 128; i++)
			val += "a";

		for (int i = 0; i < 10240; i++) {
			String src = "vertex0";
			String dst = "vertex" + (id * 10240 + i + 1);
			TitanVertex vsrc, vdst;

			// manually create this vertex first.
			vsrc = graph.getVertex(256);

			vdst = graph.addVertexWithLabel("Node");
			vdst.setProperty("vid", dst);
			TitanEdge e = vsrc.addEdge("Run", vdst);
			e.setProperty("val", val);

			writes++;
			if (writes % 100 == 0) {
				tx.commit();
				tx = graph.newTransaction();
			}
		}
		tx.commit();
	}

	public void travel() {
		long startTime = 0;
		long endTime = 0;
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		ArrayList<Vertex> selectedVertices = new ArrayList<Vertex>();
		int iSelectedNodes = 0;
		for (Vertex v : graph.getVertices()) {

			vertices.add(v);
		} 
		//Randomly select 10 vertices to perform traverse 
		while(++iSelectedNodes <= 10) {
		  
		  Random rn = new Random(); int i = rn.nextInt(vertices.size() - 1);
		  selectedVertices.add(vertices.get(i));
		  
		  }
		startTime = System.currentTimeMillis();
		
		  for (int ivertex = 0; ivertex < selectedVertices.size(); ivertex++) {
		GremlinPipeline<Vertex, Vertex> pipe = new GremlinPipeline<Vertex, Vertex>();
		pipe = pipe.start(vertices.get(ivertex));
		for (int i = 0; i < 4; i++) {
			pipe = pipe.outE().inV();
		}
		long rtn = pipe.dedup().count();
		System.out.println("No of nodes in path of BFS Traverse for vertex " + vertices.get(ivertex)+" is " + rtn);
		  }
		endTime = System.currentTimeMillis();
		System.out.println("Avg time for BFS search for 10 randomly selected nodes is " + (endTime - startTime)/10);

	}

	public void close() {
		if (!graph.isClosed())
			graph.shutdown();
	}

	public static void main(String[] args) throws ConfigurationException, IOException, InterruptedException {
		// System.out.println(Arrays.toString(args));
		// buildOptions();
		//
		// String[] rst = parseArgs(args);
		// String op = rst[0];
		// String startId = rst[1];
		// String step = rst[2];
		//
		// TitanApp app = new TitanApp();
		// if (op.equalsIgnoreCase("memory")){
		// app.meminit();
		// }
		// else{
		// app.init();
		//
		// // long curr = System.currentTimeMillis();
		// if (op.equalsIgnoreCase("insert")){
		// app.insert();
		// } else if (op.equalsIgnoreCase("singledir")){
		// app.singleInsert(Integer.parseInt(startId));
		//
		// } else if (op.equalsIgnoreCase("travel")) {
		// app.travel(startId, Integer.parseInt(step));
		//
		// } else {
		// System.out.println("No Such Operations " + op);
		// }
		// //System.out.println("Operation Costs: " +
		// (System.currentTimeMillis() - curr));
		// app.close();
		// }
		TitanApp app = new TitanApp();
		app.initMemoryDB();
		// Perform insert operation and calculate time taken and memory usage
		System.out.println("Graph Update");
		System.out.println("----------------");
		app.insert();

		MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
		mem.gc();
		long memory = (mem.getHeapMemoryUsage().getUsed() + mem.getNonHeapMemoryUsage().getUsed()) / 1024;
		System.out.println("Memory used to store the edges is " + memory + " KB");

		//
		// Perform BFS for randomly selected 10 nodes
		System.out.println("\nBFS Search");
		System.out.println("----------------");
		app.travel();

		app.close();
	}
}
