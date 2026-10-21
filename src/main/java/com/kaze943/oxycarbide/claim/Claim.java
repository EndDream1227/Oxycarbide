package com.kaze943.oxycarbide.claim;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One claimed chunk.
 *
 * <p>{@link #whitelist} maps player UUID to the name seen when they were added,
 * {@link #grounded} forbids non-allowed players from entering the chunk,
 * {@link #publicOpen} lets everyone interact and {@link #forceLoaded} keeps the chunk
 * loaded on the server.</p>
 */
public final class Claim {
	public String owner = "";
	public String ownerName = "";
	public Map<String, String> whitelist = new LinkedHashMap<>();
	public boolean grounded;
	public boolean openToTeam;
	public boolean publicOpen;
	public boolean forceLoaded;
}
