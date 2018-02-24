# janusgraphPlay

## Set-up required

1. Install mvn  
    `brew install maven`

2. Install Cassandra  
    `brew install cassandra`    

3. Install Elasticsearch  
    `brew install elasticsearch`    

## Steps to run the project
1. Clean existing compiled solution  
    `mvn clean`

2. Compile the codebase  
    `mvn compile`

3. Run the tests  
    `mvn test`


The data required for the program needs to be downloaded and placed in location. Update `wikispeedia.dataset.path` in file wikispeedia.properties in conf with the path to the location which has the data.  Download the wikispeedia_paths-and-graph.tar.gz from [here](https://snap.stanford.edu/data/wikispeedia.html).  


Before running the tests, both cassandra and elasticsearch needs to be running  
    `brew services start elasticsearch`  
    `brew services start cassandra`    

Logs will be present in the following path  
    `/usr/local/var/log/`    
Properties/Configs will be present in the following path    
    `/usr/local/etc/`


## Versions

Install the corresponding versions

| Framework     | Version |
| :-----------: | :-----: |
| Cassandra     | 3.11.2  |
| Elasticsearch | 5.6     |