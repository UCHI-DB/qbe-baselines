# SQuID - Semantic similarity-aware Query Intent Discovery #

#### What is this repository for?
* This project is targeted to the large group of database users who are not expert in query language and lack knowledge of the database schema, but are aware of a few example tuples. They can now perform Query by Example!
* Version 0.1
* Details: http://squid.cs.umass.edu

## Setup (OSX)
[Should work on any Unix based system. Please contact me if something does not work.]

### Environment
* Java version: 11.0.2 2019-01-15 LTS
* PostgreSQL: 12.1
* Download and extract [JavaFX](https://gluonhq.com/products/javafx/): openjfx-11.0.2
	* You should pick the one suitable for your OS.

### Database setup
In CLI, Run the following commands:

* `createdb`
* `psql -h localhost -c "create user $USER with login;"`
* `psql -h localhost -c "alter user $USER with password '123456';"`
* `psql -h localhost -c "create database $USER;"`
* `psql -h localhost -c "create database dblp;"`
* `psql -h localhost -c "alter database dblp owner to $USER;"`
* `psql -h localhost -c "create database smallimdb;"`
* `psql -h localhost -c "alter database smallimdb owner to $USER;"`
* `cd squid-public/data`
* `psql -U $USER dblp < dblp.sql`
* `psql -U $USER smallimdb < smallimdb.sql`

	
### Running SQuID GUI
In CLI, run the following commands:

* `export PATH_TO_FX=<path_to_java_fx>/lib`
	* Use the path where you extracted JavaFX.
* `cd squid-public`
* `javac -cp "lib/*" --module-path $PATH_TO_FX --add-modules=javafx.controls,javafx.fxml --add-exports javafx.base/com.sun.javafx.event=ALL-UNNAMED -d bin src/**/*.java`
* `java -cp "bin:lib/*" --module-path $PATH_TO_FX --add-modules=javafx.controls,javafx.fxml --add-exports javafx.base/com.sun.javafx.event=ALL-UNNAMED main.Gui --dbName smallimdb --dbUser <username> --dbPassword <password> --pgDumpPath <path to pg_dump> --logLvl FINE --rho 0.1 --eta 100.0 --gamma 2 --tau_a 5 --filterRelaxActive --disambiguateActive --useSkewness --tau_s 2`
	* You should put your __postgres DB username__, __password__, and __path to `pg_dump`__ (which can be found using `which pg_dump` command in CLI).
* You should see a GUI.
* Try with some toy inputs like: Tom Cruise, Tom Hanks, Leonardo DiCaprio. Add one in a row.


### Running SQuID in CLI:
In CLI, run the following commands:

1. `cd squid-public`
2. `javac -cp "lib/*" -d bin src/**/*.java`
3. `java -classpath "bin:lib/*" main.ConsoleMain --dbName smallimdb --dbUser <username> --dbPassword <password> --pgDumpPath <path to pg_dump> --logLvl FINE --rho 0.1 --eta 100.0 --gamma 2 --tau_a 5 --filterRelaxActive --disambiguateActive --useSkewness --tau_s 2`
	* You should put your __postgres DB username___, __password__, and __path to `pg_dump`___ (which can be found using `which pg_dump` command in CLI).
3. Sample input:
	3 1
	jim carrey
	eddie murphy
	adam sandler

* The first line says number of rows <n> to follow and number of columns (the current implementation only supports one column, so it must be 1)Then <n> rows should follow.
* The names in each row should be spelled correctly and should be in lower case. You can try with actor/actress or movie names for the IMDb database.
* Output: For the above input, the last line of the output should be:
	* `OUTPUT: Query: SELECT DISTINCT a.name FROM _persontocountry ac1, person a, _persontolanguage ae1, _persontogenre ad1, _persontocastinfo_role_id aa1 WHERE ((ac1.country_id = 199 AND ac1.freq >= 60)) AND ((ae1.language_id = 162 AND ae1.freq >= 65)) AND ((ad1.genre_id = 19 AND ad1.freq >= 40)) AND ((aa1.role_id = 1 AND aa1.freq >= 61)) AND ((a.birth_year >= 1961.0 AND a.birth_year <= 1966.0)) AND (a.age_group = '50-59') AND (ac1.person_id=a.id) AND (ae1.person_id=a.id) AND (ad1.person_id=a.id) AND (aa1.person_id=a.id)`
* This is the SQL query SQuID infers. You have to execute it in postgresql DB to find the result tuples.


## Setup (Windows) 
[Might not work after new Java Updates. Please contact me if that is the case.]

### Environment
1. Download and install `JDK 11.0.2`
2. Download and install `Eclipse` (`2018-12` version works).
3. Install `PostgreSQL 9.6.12`. You will need to give a password while installing it. Remember this password and provide when asked.
4. Make sure the commands `psql`, `javac`, `java` work in the windows command prompt. If not, add their location to the environmental variable path.
5. Download and extract the source from this repo.

### Database setup:
In command prompt: run the following commands:

  * `psql -U postgres`
  * `create user afariha with login;`
  * `alter user afariha with password '123456';`
  * `create database afariha;`
  * `create database dblp;`
  * `create database smallimdb;`
  * `alter database dblp owner to afariha;`
  * `alter database smallimdb owner to afariha;`
  * `\q`
  * `cd qbee/data`
  * `psql -U postgres dblp < dblp.sql`
  * `psql -U postgres smallimdb < smallimdb.sql`

### Running SQuID:
1. Open Eclipse.
2. Import existing projects, and show path to this repository.
3. Build project. Uncheck the `build automatically` option. You will find it in the top menu `Project>Build All`. You will see several warnings, but there should not be any error.
4. Click `Run>Run Configurations`. Select `Java Application` from the left panel.
5. Select `New Launch Configuration` (an icon on the top right).
6. Set the following values:
	* Main class: `main.Gui` 
7. Under the `Arguments` tab, paste the following content in the 
	* Program argument: `--dbName dblp --dbUser afariha --dbPassword 123456 --pgDumpPath C:\Program Files\PostgreSQL\9.6\bin\pg_dump.exe --logLvl FINE  --rho 0.1 --eta 100  --gamma 1 --tau_a 10 --tau_s 2 --disambiguateActive  --useSkewness`
7. Apply, and run.
8. Try with some toy inputs like: Tom Cruise, Tom Hanks, Leonardo DiCaprio. Add one in a row.
9. Ideally, the program should run smoothly at this point.

## Who do I talk to? ##

* Project Owner: Anna Fariha <afariha@cs.umass.edu>
* Other Contacts: Alexandra Meliou <ameli@cs.umass.edu>
