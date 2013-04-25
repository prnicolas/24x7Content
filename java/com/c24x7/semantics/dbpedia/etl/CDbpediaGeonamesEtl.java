package com.c24x7.semantics.dbpedia.etl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.c24x7.exception.InitException;
import com.c24x7.util.CEnv;
import com.c24x7.util.logs.CLogger;
import com.c24x7.util.string.CStringUtil;


	/**
	 * <p>
	 * @author Patrick
	 *
	 */

public class CDbpediaGeonamesEtl {
	
	protected static Map<String, String>  geonamesTypes = new HashMap<String,String>();
	static {
		geonamesTypes.put("states", "/entity/physical entity/object,physical object/location/region/district,territory,territorial dominion,dominion/administrative district,administrative division,territorial division/state,province/American state/");
		geonamesTypes.put("countries", "/entity/physical entity/object,physical object/location/region/district,territory,territorial dominion,dominion/administrative district,administrative division,territorial division/country,state,land/");
		geonamesTypes.put("capitals", "/entity/physical entity/object,physical object/location/region/area,country/center,centre,middle,heart,eye/seat/capital/national capital/");
		geonamesTypes.put("urbanCenters", "/entity/physical entity/object,physical object/location/region/geographical area,geographic area,geographical region,geographic region/urban area,populated area/municipality/city,metropolis,urban center/national capital/");
		geonamesTypes.put("cities", "/entity/physical entity/object,physical object/location/region/geographical area,geographic area,geographical region,geographic region/urban area,populated area/municipality/city,metropolis,urban center/");
	}
	protected static int doNoExistCounter = 0;
	protected static int noOntologyCounter = 0;
	protected static int alreadyDefinedCounter = 0;
		
	public abstract static class NLocEntries {
		
		protected abstract boolean extract(String line);
		protected abstract String getFile();
		protected abstract String getDefaultOntology(String ontology);
		protected abstract Map<String, String> getLocationTable();

		protected boolean load() throws IOException {
			BufferedReader reader = null;
				
			try {
				
				FileInputStream fis = new FileInputStream(getFile());
				reader = new BufferedReader(new InputStreamReader(fis));
				String newLine = null;
					
				while ((newLine = reader.readLine()) != null) {
					if( !extract(newLine) ) {
						CLogger.error("E: " + newLine);
					}
				}
				reader.close();
			}
		
			
			finally {
				if(reader != null) {
					reader.close();
				}
			}
			return true;
		}
		
		
		protected boolean loadDatabase() throws SQLException {
			boolean success = true;
	        
			for(String key : getLocationTable().keySet()) {
				if( !map(key) ) {
					success = false;
				}
			}
			return success;
		}

		
			/**
			 * <p>Inserts or update this dbpedia artifact into the database.</p>
			 * @param stmt JDBC statement 
			 * @param label label or keyword for this dbpedia entry.
			 * @param content content or artifact for a dbpedia entry.
			 * @return true if the database has been successfully updated, false otherwise
			 * @throws SQLException
			*/
		public boolean map(String label) throws SQLException {
			boolean success = false;
			
			StringBuilder sqlExpression = new StringBuilder("SELECT id,wordnet FROM 24x7c.dbpedia WHERE label=\'");
			sqlExpression.append(CStringUtil.encodeLatin1(label));
			sqlExpression.append("\';");
			/*
			ResultSet rs = stmt.executeQuery(sqlExpression.toString());
			String ontology = null;
			long id = -1L;
			while( rs.next() ) {
				id = rs.getInt("id");
				ontology = rs.getString("wordnet");
			}
			
			
			String ontologyInput = null;
			if( (id == -1L) ||( ontology == null || ontology.length() < 8) ) {
				ontologyInput = getDefaultOntology(label);
					
				if( id == -1L) {
					doNoExistCounter++;
					
					sqlExpression = new StringBuilder("INSERT INTO 24x7c.dbpedia (label, wordnet) VALUES (\'");
					sqlExpression.append(CStringUtil.encodeLatin1(label));
					sqlExpression.append("\', \'");
					sqlExpression.append(CStringUtil.encodeLatin1(ontologyInput));
					sqlExpression.append("\');");
				} 
				else {
					noOntologyCounter++;
					sqlExpression = new StringBuilder("UPDATE 24x7c.dbpedia SET wordnet=\'");
					sqlExpression.append(CStringUtil.encodeLatin1(ontologyInput));
					sqlExpression.append("\' WHERE label=\'");
					sqlExpression.append(CStringUtil.encodeLatin1(label));
					sqlExpression.append("\';");
				}
			//	success = stmt.execute(sqlExpression.toString());
			}
			else {
				alreadyDefinedCounter++;
			}
			*/
	
			return success;
		}
		
		protected String getDefaultOntology(String locationType, String label) {
			StringBuilder buf = null;
			if( geonamesTypes.containsKey(locationType) ) {
				buf = new StringBuilder(geonamesTypes.get(locationType));
				buf.append(label);
			}
	
			return (buf != null) ? buf.toString() : null;
		}
	}
	
	public static class NGenericLocEntries extends NLocEntries {
		protected Map<String, String> _locationsMap = new HashMap<String,String>();
		protected static String SOURCE_FILE = CEnv.datasetsDir + "locations/";
		
		protected String _locationType = null;
		protected NGenericLocEntries(String location) {
			_locationType = location;
		}
		
		protected String getFile() {
			return SOURCE_FILE + _locationType + ".txt";
		}

		protected boolean extract(String line) {
			line = line.trim();
			_locationsMap.put(line, null);
			return true;
		}
		
		/*
		 * If there is not ontology entries, add it.
		 */
		protected String getDefaultOntology(String label) {
			return super.getDefaultOntology(_locationType, label);
		}
		protected Map<String, String> getLocationTable() {
			return _locationsMap;
		}
	}
	
	
	public static class NWorldCapitalsEntries extends NLocEntries {
		protected Map<String, String> _capitalsMap = new HashMap<String,String>();

		protected static String SOURCE_FILE = CEnv.datasetsDir + "locations/worldcapitals.txt";
		protected String getFile() {
			return SOURCE_FILE;
		}

		protected boolean extract(String line) {
			boolean success = false;
			String[] entries = line.split(CEnv.KEY_VALUE_DELIM);
			String capital = null;
			
			if( entries != null && entries.length > 1) {
				capital = entries[1].trim();
				int indexEndCapital = capital.indexOf("(");
				if( indexEndCapital != -1) {
					capital = capital.substring(0, indexEndCapital-1);
					_capitalsMap.put(capital, null);
					success = true;
				}
			}
			return success;
		}
		
		protected Map<String, String> getLocationTable() {
			return  _capitalsMap;
		}
		
		
			/*
			 * If there is not ontology entries, add it.
			 */
		protected String getDefaultOntology(String label) {
			StringBuilder buf = null;
			String ontologyElement = getDefaultOntology("capitals", label);
			if( ontologyElement != null) {
				buf = new StringBuilder(ontologyElement);
				ontologyElement =  getDefaultOntology("urbanCenters", label);
				
				if( ontologyElement != null) {
					buf.append(CEnv.TAXONOMY_FIELD_DELIM);
					buf.append(ontologyElement);
				}
			}
			return (buf != null) ? buf.toString() : null;
		}
	}
	
	protected static class NCountriesEntries extends NLocEntries {
		protected static String SOURCE_FILE = CEnv.datasetsDir + "locations/worldcapitals.txt";
		
		protected Map<String, String> _countriesMap = new HashMap<String,String>();

		
		protected String getFile() {
			return SOURCE_FILE;
		}

		protected boolean extract(String line) {
			boolean success = false;
			String[] entries = line.split(CEnv.KEY_VALUE_DELIM);
			
			if( entries != null && entries.length > 1) {
				_countriesMap.put(entries[0], null);
				success = true;
			}
			return success;
		}
		
		/*
		 * If there is not ontology entries, add it.
		 */
		protected String getDefaultOntology(String label) {
			return getDefaultOntology("countries", label);
		}
		
		protected Map<String, String> getLocationTable() {
			return _countriesMap;
		}
	}
	
	protected NLocEntries _stateEntries =  new NGenericLocEntries("states");
	protected NLocEntries _citiesEntries =  new NGenericLocEntries("cities");
	protected NLocEntries _worldCapitalsEntries = new NWorldCapitalsEntries();
	protected NLocEntries _countriesEntries = new NCountriesEntries();
		
	
	public CDbpediaGeonamesEtl() {
		try {
			init();
		}
		catch( InitException e) {
			CLogger.error(e.toString());
		}
	}
	
	public void loadGeoNames() throws SQLException {
		_stateEntries.loadDatabase();
		_citiesEntries.loadDatabase();
		_countriesEntries.loadDatabase();
		_worldCapitalsEntries.loadDatabase();
	}
	
	
	
	protected void init() throws InitException {
		try {
			 _worldCapitalsEntries.load();
			 _countriesEntries.load();
			 _stateEntries.load();
			 _citiesEntries.load();
		}
		catch( IOException e) {
			throw new InitException(e.toString());
		}
	}
		
	
	public static void main(String[] args) {
		CDbpediaGeonamesEtl geonamesEtl = new CDbpediaGeonamesEtl();
		try {
			geonamesEtl.loadGeoNames();
			
			CLogger.info(doNoExistCounter + " did not exists!");
			CLogger.info(noOntologyCounter + " had no ontology!");
			CLogger.info(alreadyDefinedCounter + " already defined.!");
		}
		catch( SQLException e) {
			CLogger.error(e.toString());
		}
	}

}