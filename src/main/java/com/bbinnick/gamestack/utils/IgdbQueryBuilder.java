package com.bbinnick.gamestack.utils;

public class IgdbQueryBuilder {
	
	private IgdbQueryBuilder() {		
		throw new IllegalStateException("Utility class");
	}

	public static String buildGameQuery(String[] fields, int limit) {
		String fieldString = String.join(",", fields);
		return String.format("fields %s; limit %d;", fieldString, limit);
	}

	public static String buildPopularGamesQuery(String[] fields, int limit) {
		String fieldString = String.join(",", fields);
		return String.format("fields %s; sort popularity desc; limit %d;", fieldString, limit);
	}

	public static String buildNewReleasesQuery(String[] fields, int limit) {
	    String fieldString = String.join(",", fields);
	    return String.format("fields %s; sort first_release_date desc; limit %d;", fieldString, limit);
	}
}
