package edu.prism;

import com.orientechnologies.orient.client.remote.OServerAdmin;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientEdge;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import org.apache.commons.cli.*;

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
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 */
public class OrientdbApp {

	OrientGraphFactory factory;
	String graphFile;
	static Options options = new Options();


	/**
	 * Create Database: create database remote:localhost/test root 123 plocal or in memory
	 */
	public OrientdbApp(String file) {
		this.graphFile = file;
		try {
			// Logic to Create database (Change the database name for each test
			// case)
			/*
			 * new OServerAdmin("remote:localhost") .connect("root", "check")
			 * .createDatabase("6200v100Ke", "graph", "plocal").close();
			 */
			System.out.println("Performance Evaluation For Graph File: " + file);
			System.out.println("-------------------------------------------");
			//Create database in memory to evaluate memory usage
			factory = new OrientGraphFactory("memory:6200v100Ke").setupPool(1, 10);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void nonTxInsert() throws IOException {
		OrientGraphNoTx graph = null;
		long start;
		try {
			graph = factory.getNoTx();
			HashMap<ByteBuffer, Vertex> inserted = new HashMap<>();
			BufferedReader br = new BufferedReader(new FileReader(graphFile));
			String line;
			start = System.currentTimeMillis();
			while ((line = br.readLine()) != null) {
				String[] splits = line.split(" ");
				String src = "NoTX" + splits[0];
				ByteBuffer bsrc = ByteBuffer.wrap(src.getBytes());
				String dst = "NoTX" + splits[1];
				ByteBuffer bdst = ByteBuffer.wrap(dst.getBytes());
				String val = splits[2];

				Vertex vsrc, vdst;

				if (!inserted.containsKey(bsrc)) {
					vsrc = graph.addVertex("class:Node");
					vsrc.setProperty("vid", src);
					inserted.put(bsrc, vsrc);
				} else
					vsrc = inserted.get(bsrc);

				if (!inserted.containsKey(bdst)) {
					vdst = graph.addVertex("class:Node");
					vdst.setProperty("vid", dst);
					inserted.put(bdst, vdst);
				} else
					vdst = inserted.get(bdst);

				OrientEdge runs = graph.addEdge(null, vsrc, vdst, "Run");
				runs.setProperty("val", val);
				graph.commit();
			}
		} finally {
			graph.shutdown();
		}
		long end = System.currentTimeMillis();
		System.out.println("Time taken for graph update: " + (end - start));
	}

		// #15:98
	public void travel() {
		OrientGraph graph = null;
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		ArrayList<Object> selectedVertices = new ArrayList<Object>();
		int icount = -1;
		int iSelectedNodes = 0;
		try {
			graph = factory.getTx();
			Iterable<Vertex> v = graph.getVertices();

			for (Vertex v1 : v) {
				vertices.add(v1);
				icount++;
			}
			// Randomly select 10 vertices to perform traverse
			while (++iSelectedNodes <= 10) {

				Random rn = new Random();
				int i = rn.nextInt(icount);
				selectedVertices.add(vertices.get(i).getId());

			}
			long curr = System.currentTimeMillis();
			for (int iVertex = 0; iVertex < selectedVertices.size(); iVertex++) {
				long curr1 = System.currentTimeMillis();

				System.out.println(
						"Testing traverse: Iteration " + iVertex + " For vid " + selectedVertices.get(iVertex));

				String sqlString = "TRAVERSE out('Run') FROM " + selectedVertices.get(iVertex)
						+ " STRATEGY BREADTH_FIRST";
				// sqlString = "Select
				// out('Run').out('Run').out('Run').out('Run') from " +
				// startPoint;
				// OCommandSQL travel = new OCommandSQL(sqlString);
				for (OIdentifiable id : new OSQLSynchQuery<ODocument>(sqlString)) {
					System.out.println(id);
				}
				long end = System.currentTimeMillis();
				System.out.println("Time for current vertex is " + (end - curr1));

			}
			long end = System.currentTimeMillis();
			System.out.println("Avg time for BFS search for 10 randomly selected nodes is " + (end - curr) / 10);

		} finally {
			graph.shutdown();
		}
	}

	public static void main(String[] args) throws IOException {
		
		OrientdbApp app = new OrientdbApp("C:\\Users\\Manohar\\Desktop\\AOS\\GraphFiles\\6200v100Ke");
		// Perform insert operation and calculate time taken and memory usage
		System.out.println("Graph Update");
		System.out.println("----------------");
		app.nonTxInsert();

		MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
		mem.gc();
		long memory = (mem.getHeapMemoryUsage().getUsed() + mem.getNonHeapMemoryUsage().getUsed()) / 1024;
		System.out.println("Memory used to store the edges is " + memory + " KB");

		//
		// Perform BFS for randomly selected 10 nodes
		System.out.println("\nBFS Search");
		System.out.println("----------------");
		app.travel();
	}
}