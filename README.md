Usage:

build from scratch using maven:
`mvn package -f "java/pom.xml"`

from command line:
`java -jar smallest_scope_extractor.jar java/files/testjava "210,210,215,225"`

210,210,212,215 means that we have 2 hunks:
1 hunk is line 210 to line 210 (one line change)
1 hunk is from line 215 to 225 (3 line changes)
