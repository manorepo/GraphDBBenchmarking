CUR_DIR:=$(shell pwd)

JAVA:=$(shell which java)
JAVA_VER:=$(shell java -version 2>&1)

MAVEN:=$(shell which mvn)
MAVEN_VER:=$(shell mvn -version)

JAVA_SRC_HOME=$(CUR_DIR)/java

.PHONY: clean
clean:
		mvn clean;
		-rm -rf prism_release

.PHONY: install
install:
		mvn clean install;
		mkdir -p prism_release; mv prism-titan/target/*.tar.gz prism_release/; mv prism-titan/target/*.zip prism_release/;
		mv prism-orientdb/target/*.tar.gz prism_release/; mv prism-orientdb/target/*.zip prism_release/;
		cd prism_release && tar zxf prism-titan-*.tar.gz && tar zxf prism-orientdb-*.tar.gz

.PHONY: all
all: install
		# Start building
