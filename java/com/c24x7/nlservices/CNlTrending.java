// Copyright (C) 2010-2011 Patrick Nicolas
package com.c24x7.nlservices;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import com.c24x7.nlservices.content.AContent;
import com.c24x7.nlservices.content.CStructuredOutput;
import com.c24x7.util.CCache;


		/**
		 * <p>Class that collects and manage the latest trends in topics.</p>
		 * @author Patrick Nicolas
		 * @date 01/09/2011
		 * @see com.c24x7.nlservices.INlProcessor 
		 */
public final class CNlTrending implements INlProcessor {
	private List<String> _feeds = null;
	
	public CNlTrending() {
		_feeds = new ArrayList<String>();
	}
	

			/**
			 * <p>Process the feeds from social networks or sites.</p>
			 * @param content content for the feed
			 */
	public AContent process(AContent content) throws IOException {
		CCache cache = new CCache();
		
		if( !cache.uptodate() ) {
			getTopics();
			CNlGenerator gen = new CNlGenerator();
			CNlOutputAnalyzer analyzer = new CNlOutputAnalyzer();
			AContent genContent = null;
			CStructuredOutput output = null;
		
			for( String feed : _feeds) {
				genContent = new AContent(feed);
				genContent = gen.process(genContent);
				genContent = analyzer.process(genContent);
				if( genContent != null) {
					output = (CStructuredOutput)genContent;
					cache.put(feed, output);
				}
			}
			cache.save();
		}

		return content;
	}
	
	
	
	private void getTopics() {
		String[] latestTopics = {
	      "Arizona lawmakers plan to block protesters within 300 feet of funerals",
	      "Disaster declared as Australia flood death toll rises to 10",
	      "Winter storm cripples South, heads north to deliver more misery",
	      "Panel calls for drastic steps to stop future deepwater oil spills",
	      "Problems line up for Nigerian election",
	      "Abortion rate stalls after decade-long decline",
	      "China New weapons not meant to pose threat",
	      "Baby Jenny reunited with doctor who saved her in Haiti"
		};
		
		for( int j = 0; j < latestTopics.length; j++) {
			_feeds.add(latestTopics[j]);
		}
	}
}

// --------------------- EOF ---------------------------------
