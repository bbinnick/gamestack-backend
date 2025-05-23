package com.bbinnick.gamestack.utils;

public class IgdbQueryBuilder {

	private IgdbQueryBuilder() {
		throw new IllegalStateException("Utility class");
	}

	public static String buildSearchGameQuery(String[] fields, int limit) {
		String fieldString = String.join(",", fields);
		return String.format("fields %s; limit %d;", fieldString, limit);
	}

	public static String buildPopularGamesQuery(String[] fields, int limit) {
		String fieldString = String.join(",", fields);
		return String.format("fields %s; where hypes > 100; sort hypes desc; limit %d;", fieldString, limit);
	}

	public static String buildNewReleasesQuery(String[] fields, int limit) {
		String fieldString = String.join(",", fields);
		long currentTimestamp = System.currentTimeMillis() / 1000;
		return String.format("fields %s; where first_release_date <= %d; sort first_release_date desc; limit %d;",
				fieldString, currentTimestamp, limit);
	}

}
