package com.reldoapp.sync;

import com.google.gson.Gson;
import com.reldoapp.ReldoConfig;
import java.io.IOException;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class SyncService
{
	private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

	@Inject
	private OkHttpClient httpClient;

	@Inject
	private ReldoConfig config;

	@Inject
	private Gson gson;

	/**
	 * Send the collected payload to Reldo's plugin sync endpoint.
	 * Must be called from a background thread (not the client thread).
	 */
	public SyncResult send(Map<String, Object> payload)
	{
		String token = config.apiToken().trim();
		if (token.isEmpty())
		{
			return SyncResult.error("No API token set. Generate one in Reldo Settings.");
		}

		String url = config.baseUrl().trim().replaceAll("/$", "") + "/api/plugin_sync.php";
		String body = gson.toJson(payload);

		Request request = new Request.Builder()
			.url(url)
			.header("Authorization", "Bearer " + token)
			.put(RequestBody.create(body, JSON))
			.build();

		try (Response response = httpClient.newCall(request).execute())
		{
			if (response.isSuccessful())
			{
				return SyncResult.ok();
			}

			String responseBody = response.body() != null ? response.body().string() : "";
			log.warn("Reldo sync failed: HTTP {} — {}", response.code(), responseBody);

			if (response.code() == 401)
			{
				return SyncResult.error("Invalid API token.");
			}
			if (response.code() == 404)
			{
				return SyncResult.error("Character not found. Add it in Reldo first.");
			}
			return SyncResult.error("Sync failed (HTTP " + response.code() + ").");
		}
		catch (IOException e)
		{
			log.warn("Reldo sync error", e);
			return SyncResult.error("Network error: " + e.getMessage());
		}
	}
}
