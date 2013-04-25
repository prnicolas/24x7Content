package com.c24x7.helper;


import java.util.Collection;

import com.c24x7.exception.SemanticAnalysisException;
import com.c24x7.math.utils.CIntArray;
import com.c24x7.models.CTopicPoint;
import com.c24x7.nlservices.CServiceManager;



public final class CPlotHelper {

	private static final String INPUT_TEXT1 =  "The Islamic Republic of Afghanistans, commonly known as Afghanistan, is a landlocked country in south-central Asia. It is bordered by Pakistan in the south and east, Iran in the west, Turkmenistan, Uzbekistan and Tajikistan in the north, and China in the far northeast. The territories now comprising Afghanistan have been an ancient focal point of the Silk Road and human migration. Afghanistan is at an important geostrategic location, connecting East, South, West and Central Asia, and has been home to various peoples through the ages. The region has been a target of various invaders since antiquity, including Alexander the Great, the Mauryan Empire, Muslim armies, and Genghis Khan, and has served as a source from which many kingdoms, such as the Greco-Bactrians, Kushans, Samanids, Ghaznavids, Ghurids, Kartids, Timurids, and many others have risen to form empires of their own. The political history of modern Afghanistan begins in the 18th century with the rise of the Pashtun tribes (known as Afghans in Persian language), when in 1709 the Hotaki dynasty established its rule in Kandahar and, more specifically, when Ahmad Shah Durrani created the Durrani Empire in 1747 which became the forerunner of modern Afghanistan. Its capital was shifted in 1776 from Kandahar to Kabul and most of its territories ceded to neighboring empires by 1893. In the late 19th century, Afghanistan became a buffer state in \"The Great Game\" between the British and Russian empires. On August 19, 1919, following the third Anglo-Afghan war and the signing of the Treaty of Rawalpindi, the nation regained control over its foreign affairs from the British. Since the late 1970s Afghanistan has experienced a continuous state of civil war punctuated by foreign occupations in the forms of the 1979 Soviet invasion and the October 2001 US-led invasion that overthrew the Taliban government. In December 2001, the United Nations Security Council authorized the creation of an International Security Assistance Force (ISAF) to help maintain security and assist the Karzai administration. The country is being rebuilt slowly with support from the international community while dealing with the Taliban insurgency. He was born and won.";
	private static final String INPUT_TEXT2 = "Islamophobia is prejudice against, or an irrational fear of Islam or Muslims. The term seems to date back to the late 1980s, but came into common usage after the September 11, 2001 attacks in the United States to refer to types of political dialogue that appeared prejudicially resistant to pro-Islamic argument. In 1997, the British Runnymede Trust defined Islamophobia as the \"dread or hatred of Islam and therefore, to the fear and dislike of all Muslims,\" stating that it also refers to the practice of discriminating against Muslims by excluding them from the economic, social, and public life of the nation. It includes the perception that Islam has no values in common with other cultures, is inferior to the West and is a violent political ideology rather than a religion. Professor Anne Sophie Roald writes that steps were taken toward official acceptance of the term in January 2001 at the \"Stockholm International Forum on Combating Intolerance\", where Islamophobia was recognized as a form of intolerance alongside Xenophobia and Antisemitism. A perceived trend of increasing \"Islamophobia\" during the 2000s has been attributed by some commentators to the September 11 attacks, while others associate it with the rapidly growing Muslims populations in the Western world, especially in Western Europe, due to both immigration and high fertility rate. In May 2002, the European Monitoring Centre on Racism and Xenophobia (EUMC), a European Union watchdog, released a report entitled \"Summary report on Islamophobia in the EU after 11 September 2001\", which described an increase in Islamophobia-related incidents in European member states post-9/11. Although the term is widely recognized and used, it has not been without controversy. The word Islamophobia is a neologism formed of Islam and -phobia. The compound form Islamo- contains the thematic vowel ', and is found in earlier coinages such as Islamo-Christian from the 19th century.";
	private static final String INPUT_TEXT3 = "Africa is the world's second-largest and second most-populous continent, after Asia. At about 30.2 million km\u00B2 (11.7 million sq mi) including adjacent islands, it covers 6% of the Earth's total surface area and 20.4% of the total land area. With a billion people (as of 2009, see table) in 61 territories, it accounts for about 14.72% of the world's human population. The continent is surrounded by the Mediterranean Sea to the north, both the Suez Canal and the Red Sea along the Sinai Peninsula to the northeast, the Indian Ocean to the southeast, and the Atlantic Ocean to the west. The continent has 54 sovereign states, including Madagascar, various island groups, and the Sahrawi Arab Democratic Republic, a member state of the African Union whose statehood is disputed by Morocco. Africa, particularly central eastern Africa, is widely regarded within the scientific community to be the origin of humans and the Hominidae clade, as evidenced by the discovery of the earliest hominids and their ancestors, as well as later ones that have been dated to around seven million years ago \u2013 including Sahelanthropus tchadensis, Australopithecus africanus, A. afarensis, Homo erectus, H. habilis and H. ergaster \u2013 with the earliest Homo sapiens (modern human) found in Ethiopia being dated to circa 200,000 years ago. Africa straddles the equator and encompasses numerous climate areas; it is the only continent to stretch from the northern temperate to southern temperate zones. The African expected economic growth rate for 2010 is at about 4.7%.";
	private static final String INPUT_TEXT4 = "It's (a plaster cast) is a copy made in plaster of another 3-dimensional form. The original from which the cast is taken may be a sculpture, building, a face, a fossil or other remains such as fresh or fossilised footprints, particularly in palaeontology (a track of dinosaur footprints made in this way can be seen outside the Oxford University Museum of Natural History). Sometimes a blank block of plaster itself was carved to produce mock-ups or first drafts of sculptures (usually relief sculptures) that would ultimately be sculpted in stone, by measuring exactly from the cast, for example by using a pointing machine. These are still described as plaster casts. Examples of these by John Flaxman may be found in the central rotunda of the library at University College London, and elsewhere in the University's collections. It may also describe a finished original sculpture made out of plaster, though these are rarer.";	
	private static final String INPUT_TEXT5 = "George Smith Patton, Jr. was a United States Army officer best known for his leadership while commanding corps and armies as a general during World War II. He was also well known for his eccentricity and controversial outspokenness." +
	" Patton was commissioned in the U.S. Army after his graduation from the U.S. Military Academy at West Point in 1909. In 1916–17, he participated in the unsuccessful Pancho Villa Expedition, a U.S. operation that attempted to capture the Mexican revolutionary." +
	" In World War I, he was the first officer assigned to the new United States Tank Corps and saw action in France. In World War II, he commanded corps and armies in North Africa, Sicily, and the European Theater of Operations." +
	" In 1944, Patton assumed command of the U.S. Third Army, which under his leadership advanced farther, captured more enemy prisoners, and liberated more territory in less time than any other army in military history.";
	

	private static final String[] INPUT_TEXTS = {
		INPUT_TEXT1, INPUT_TEXT2, INPUT_TEXT3, INPUT_TEXT4, INPUT_TEXT5
	};
	
	
	public void benchmarkTestSingle(StringBuilder matrixDimBuf,
									StringBuilder topicPointsBuf,
									StringBuilder sentencesBuf,
									StringBuilder topicsBuf) throws SemanticAnalysisException {
		
		CServiceManager serviceManager = new CServiceManager();
		/*
		for( String inputText : INPUT_TEXTS) {
			serviceManager.addInputText(inputText);
		}
		*/
		serviceManager.addInputText(INPUT_TEXT5);
		Collection<CTopicPoint> topicPointsList = serviceManager.execute(matrixDimBuf, sentencesBuf, topicsBuf);
		
		CIntArray dynamicArray = new CIntArray();
		for(CTopicPoint topicPoint : topicPointsList) {
			topicPoint.getDataPoint(dynamicArray);
		}
		
		
		/*
		 * Collect the list of XYZ coordinates for the 
		 * different topics points.
		 */
		int[] values = dynamicArray.getValues();
		
		for( int k = 0; k < values.length; k++) {
			topicPointsBuf.append(values[k]);
			topicPointsBuf.append(",");
			k++;
			topicPointsBuf.append(values[k]);
			topicPointsBuf.append(",");
			k++;
			topicPointsBuf.append(values[k]);
			if( k < values.length -1) {
				topicPointsBuf.append(",");
			}
		}
	}

}

// ------------------------------------- EOF -----------------------------------------
