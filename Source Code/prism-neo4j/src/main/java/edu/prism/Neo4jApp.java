package edu.prism;


import org.apache.commons.cli.*;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;

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
import java.util.List;


import org.neo4j.tooling.GlobalGraphOperations;


import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;

/**
 */
public class Neo4jApp {

	GraphDatabaseService graphDb;
	String graphFile;
	static Options options = new Options();
	  public static enum RelTypes implements RelationshipType {
    Run
  }

	private static void buildOptions() {
		// build option tables

		options.addOption(new Option("help", "print this message"));

		options.addOption(Option.builder("op").hasArg()
				.desc("test operations;")
				.build());

		options.addOption(Option.builder("id").hasArg()
				.desc("starting from this vertex")
				.build());

		options.addOption(Option.builder("step").hasArg()
				.desc("travel stps")
				.build());

	}

	public static String[] parseArgs(String[] args) {
		String[] rst = new String[3];
		CommandLineParser parser = new DefaultParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			if (args.length == 0) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("prism-orientdb", options);
				System.exit(0);
			}

			if (line.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("prism-orientdb", options);
				System.exit(0);
			}

			if (line.hasOption("op")) {
				rst[0] = line.getOptionValue("op", "0");
			} else {
				throw new ParseException("argument 'op' is required.");
			}

			if (line.hasOption("id")) {
				rst[1] = line.getOptionValue("id");
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
	
	@SuppressWarnings("deprecation")
	public Neo4jApp(String file){
		this.graphFile = file;
		System.out.println("Performance Evaluation For Graph File: " + file);
		System.out.println("-------------------------------------------");
	graphDb = new GraphDatabaseFactory().newEmbeddedDatabase("./DataBases/2500v40Ke.db");
	//registerShutdownHook( graphDb );
	}



	@SuppressWarnings("deprecation")
	public void insert() throws IOException{
	Relationship relationship;
	long startTime,endTime;
	Transaction tx=null;
		try {
			  tx = graphDb.beginTx();
			HashMap<ByteBuffer, Node> inserted = new HashMap<>();
			BufferedReader br = new BufferedReader(new FileReader(graphFile));
			String line;
			startTime=System.currentTimeMillis();
			while ((line = br.readLine()) != null) {
				String[] splits = line.split(" ");
				String src = "NoTX"+splits[0];
				ByteBuffer bsrc = ByteBuffer.wrap(src.getBytes());
				String dst = "NoTX"+splits[1];
				ByteBuffer bdst = ByteBuffer.wrap(dst.getBytes());
				String val = splits[2];

				Node vsrc, vdst;

				if (!inserted.containsKey(bsrc)) {
					 vsrc=graphDb.createNode();
					vsrc.setProperty( "vid", src);
					//vsrc = graph.addVertex("class:Node");
					//vsrc.setProperty("vid", src);
					inserted.put(bsrc, vsrc);
				} else
					vsrc = inserted.get(bsrc);

				if (!inserted.containsKey(bdst)) {
					 vdst=graphDb.createNode();
					vdst.setProperty( "vid", dst);

					//#vdst = graph.addVertex("class:Node");
					//vdst.setProperty("vid", dst);
					inserted.put(bdst, vdst);
				} else
					vdst = inserted.get(bdst);
				relationship = vsrc.createRelationshipTo( vdst, RelTypes.Run );
				relationship.setProperty("val", val);
				//OrientEdge runs = graph.addEdge(null, vsrc, vdst, "Run");
				//runs.setProperty("val", val);
				 tx.success();
			}
			endTime=System.currentTimeMillis();
			System.out.println("Time taken for Graph Update is "+ (endTime-startTime));
			} finally {
			tx.finish();
		}
	}

	//#15:98
@SuppressWarnings("deprecation")
public void travel(){
	ArrayList<Node> vertices=new ArrayList<Node>();
	Iterable<Node> v=null;
	long startTime;
	long endTime;
	int iterations=0;
	 try ( Transaction tx = graphDb.beginTx() )
	 {
	v=graphDb.getAllNodes();
	
	int numberOfFriends=0;
	int icount=0;
String output;
	for(Node v1:v){
		vertices.add(v1);
		icount++;
	}
	startTime=System.currentTimeMillis();
	for(int i=0;i<10;i++){
		
	Node selectedNode=vertices.get(i);
	Traverser friendsTraverser = getFriends(selectedNode);
	output="Friends for "+selectedNode.getProperty("vid")+"\n";
	for ( Path friendPath : friendsTraverser )
	{
	    output += "At depth " + friendPath.length() + " => "
	              + friendPath.endNode()
	                      .getProperty( "vid" ) + "\n";
	    numberOfFriends++;
	}
	output += "Number of friends found: " + numberOfFriends + "\n";
	System.out.println(output);
	++iterations;
	}
	endTime=System.currentTimeMillis();
	System.out.println("Avg time for BFS search for 10 randomly selected nodes is " +(endTime-startTime)/iterations);

    tx.success();
}
	} 

	public static void main(String[] args) throws IOException {
		// Create app
		Neo4jApp app = new Neo4jApp("C:\\Users\\Manohar\\Desktop\\AOS\\GraphFiles\\2500v40Ke");
		//
		//Perform insert operation and calculate time taken and memory usage
		System.out.println("Graph Update");
		System.out.println("----------------");
		app.insert();
		
		MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
		mem.gc();
	    long memory = (mem.getHeapMemoryUsage().getUsed() + mem.getNonHeapMemoryUsage().getUsed()) / 1024;
	    System.out.println("Memory used to store the edges is "+ memory+" KB");
	
		//
		//Perform BFS for randomly selected 10 nodes
		System.out.println("\nBFS Search");
		System.out.println("----------------");
		app.travel();
		
		app.close();
	}
	public void close()
	{
		graphDb.shutdown();
	}

	private org.neo4j.graphdb.traversal.Traverser getFriends(
	        final Node person )
	{
	    TraversalDescription td = graphDb.traversalDescription()
	            .breadthFirst()
	            .relationships( RelTypes.Run, Direction.OUTGOING )
	            .evaluator( Evaluators.excludeStartPosition() );
	    return td.traverse( person );
	}
}