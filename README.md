# GraphDBBenchmarking
Project on performance comparison among graph DB systems Neo4j, OrientDB, Titan to establish benchmarks for Graph Databases

A significant number of graph database systems has emerged in the past few years, most aim at the management of the property graph data structure: where graph elements can be assigned with properties. Large-scale graph processing has been a hot topic in recent days. Systems including GraphLab, GraphX etc., have been widely used. However, comparing with the rapid development of these genetic graph processing frameworks, research on distributed graph databases, on the other hand, is still limited. Even there are a large number of graph databases existing, commercial (Neo4J) or open sourced (Titan, OrientDB), their performance, scalability, usability, and stability, are not well studied.

There is a necessity to establish the benchmarks for graph databases, which motivated us to research on this particular project, graph database performance comparisons.

With this project, we compared three Graph database systems Neo4j, OrienDb, Titan for opertaions of Graph update, BFS Traversal, Memory Usage

Sorce code contains logic for  Graph Update(insertions), BFS traversals for randomly selected 1o nodes, Memory Usage

Use RMATGraphGenerator.java present in titan-commons to generate sample data sets

Test each database with different data sets.

Test Results for comparing Neo4j, OrientDb, Titan databases:

 -Performed test on 5 sample data files that provided in /SampleGraphFiles folder

 -Output folder contains the results for 3 databases

 -Reports folder contains the comparisons for 3 databases
