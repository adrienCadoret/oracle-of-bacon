# Oracle of Bacon
This application is an Oracle of Bacon implementation based on NoSQL data stores :
* ElasticSearch (http) - localhost:9200
* Redis - localhost:6379
* Mongo - localhost:27017
* Neo4J (bolt) - localhost:7687

To build :
```
./gradlew build
```

To Run, execute class *com.serli.oracle.of.bacon.Application*.


# Init Neo4J

Installation de Neo4J Server
Créer une base de données avec username = "neo4j" et password = "coucou"
Importer les données 'Actors', 'Movies' et ''Roles'
--> Mettre les fichiers .csv dans le dossier 'import' du serveur Neo4J
Commande pour importer : 
bin/neo4j-admin import --nodes:Movie .\import\movies.csv --nodes:Actor .\import\actors.csv --relationships .\import\roles.csv 
Lancer Neo4J en lancant bin/neo4j console

# Init MongoDB
Lancer Mongo Server avec la commande 'mongod' (attention aux variables d'environnements) 
Attention, en amont, ne pas oublier de créer le dossier data/db (par défaut sur MongoDB)
Importer les données 'mongodb-102/src/data/Top_1000_Actors_and_Actresses.csv' en lancant la commande suivante :
'mongoimport -d workshop -c actors --type csv --file Top_1000_Actors_and_Actresses.csv --headerline'


