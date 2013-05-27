package com.vaguehope.onosendai.provider.successwhale;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.vaguehope.onosendai.util.ArrayHelper;

public class SuccessWhaleSources {

	private static final String SOURCE_SEP = ":";

	private final List<SuccessWhaleSource> sources;

	public SuccessWhaleSources (final List<SuccessWhaleSource> sources) {
		this.sources = Collections.unmodifiableList(sources);
	}

	public List<SuccessWhaleSource> getSources () {
		return this.sources;
	}

	public static String toResource (final Collection<SuccessWhaleSource> sources) {
		final List<String> fullurls = new ArrayList<String>();
		for (SuccessWhaleSource source : sources) {
			fullurls.add(source.getFullurl());
		}
		return ArrayHelper.join(fullurls, SOURCE_SEP);
	}

}
