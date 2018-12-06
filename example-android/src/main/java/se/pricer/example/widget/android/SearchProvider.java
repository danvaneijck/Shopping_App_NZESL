/*
 * Pricer Server Software
 *
 * Confidential Property of Pricer AB (publ). Copyright © 1998-2017 Pricer AB (publ),
 * Box 215,Västra Järnvägsgatan 7, SE-101 24 Stockholm, Sweden. All rights reserved.
 */
package se.pricer.example.widget.android;

import se.pricer.widget.android.PricerSearchImpl;
import se.pricer.widget.android.PricerSearchRequest;
import se.pricer.widget.android.PricerSearchResponse;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SearchProvider extends ContentProvider {

  public static final String AUTHORITY = "se.pricer.example.widget.android.SearchProvider";
  public static final Uri CONTENT_URI =
      Uri.parse("content://" + AUTHORITY + "/" + SearchManager.SUGGEST_URI_PATH_QUERY);

  private static final int SEARCH_STARTED = 1;
  private static final int SEARCH_SUGGEST = 2;

  private static final UriMatcher uriMatcher;

  private static final String SUGGEST_COLUMN_RESULT_OBJECT = "SUGGEST_COLUMN_RESULT_OBJECT";
  private static final String[] SEARCH_SUGGEST_COLUMNS = {
      BaseColumns._ID,
      SearchManager.SUGGEST_COLUMN_TEXT_1,
      SearchManager.SUGGEST_COLUMN_TEXT_2,
      SUGGEST_COLUMN_RESULT_OBJECT
  };

  public static final int SUGGEST_COLUMN_RESULT_OBJECT_INDEX =
      Arrays.asList(SEARCH_SUGGEST_COLUMNS).indexOf(SUGGEST_COLUMN_RESULT_OBJECT);

  static {
    uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_STARTED);
    uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
  }

  static public Uri createQueryUri(String query) {
    return SearchProvider.CONTENT_URI.buildUpon().appendPath(query).build();
  }

  @Override
  public boolean onCreate() {
    return true;
  }

  @Override
  public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    switch (uriMatcher.match(uri)) {
      case SEARCH_STARTED:
        return null;
      case SEARCH_SUGGEST:
        // Get the search query
        String query = uri.getLastPathSegment().toLowerCase();

        // Send the search query to the Pricer cloud
        PricerSearchResponse
            searchResponse =
            PricerSearchImpl.getInstance().search(new PricerSearchRequest(0, 5, query), getContext());

        // No hits found - return
        if (searchResponse == null || searchResponse.getSearchResults().isEmpty()) {
          return null;
        }

        List<PricerSearchResponse.SearchResult> searchResults = searchResponse.getSearchResults();

        MatrixCursor resultCursor = new MatrixCursor(SEARCH_SUGGEST_COLUMNS, searchResults.size());

        ObjectMapper mapper = new ObjectMapper();
        try {
          for (int i = 0; i < searchResults.size(); ++i) {
            String[] row = new String[SEARCH_SUGGEST_COLUMNS.length];

            if (searchResults.get(i) instanceof PricerSearchResponse.SearchResultArticleGroup) {
              PricerSearchResponse.SearchResultArticleGroup articleGroup =
                  (PricerSearchResponse.SearchResultArticleGroup) searchResults.get(i);

              row[0/*BaseColumns._ID*/] = Integer.toString(i);
              row[1/*SearchManager.SUGGEST_COLUMN_TEXT_1*/] = articleGroup.getGroupName();
              row[2/*SearchManager.SUGGEST_COLUMN_TEXT_2*/] = articleGroup.getGroupType();
              row[3/*SUGGEST_COLUMN_RESULT_OBJECT*/] = mapper.writeValueAsString(articleGroup);
            } else if (searchResults.get(i) instanceof PricerSearchResponse.SearchResultItem) {
              PricerSearchResponse.SearchResultItem item =
                  (PricerSearchResponse.SearchResultItem) searchResults.get(i);

              row[0/*BaseColumns._ID*/] = Integer.toString(i);
              row[1/*SearchManager.SUGGEST_COLUMN_TEXT_1*/] = item.getItemName();
              row[2/*SearchManager.SUGGEST_COLUMN_TEXT_2*/] = item.getItemId();
              row[3/*SUGGEST_COLUMN_RESULT_OBJECT*/] = mapper.writeValueAsString(item);
            }

            resultCursor.addRow(row);
          }
        } catch (IOException e) {
          return null;
        }

        return resultCursor;
      default:
        throw new IllegalArgumentException("Unknown Uri: " + uri);
    }
  }

  @Override
  public String getType(@NonNull Uri uri) {
    switch (uriMatcher.match(uri)) {
      case SEARCH_SUGGEST:
        return SearchManager.SUGGEST_MIME_TYPE;
      default:
        throw new IllegalArgumentException("Unknown URL " + uri);
    }
  }

  @Override
  public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int delete(@NonNull Uri uri, String s, String[] strings) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int update(@NonNull Uri uri, ContentValues contentValues, String s, String[] strings) {
    throw new UnsupportedOperationException();
  }
}
