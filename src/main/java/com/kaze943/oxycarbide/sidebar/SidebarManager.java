package com.kaze943.oxycarbide.sidebar;

import com.google.gson.Gson;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.kaze943.oxycarbide.Oxycarbide;
import com.kaze943.oxycarbide.config.OxycarbideConfig;
import com.kaze943.oxycarbide.treasury.TreasuryManager;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundResetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.LongFunction;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;

/**
 * Draws the JSON sidebar of {@code config/oxycarbide/scoreboard/scoreboard.json}.
 *
 * <p>Every player gets their own copy: the lines are built for that player, then sent as
 * the vanilla scoreboard packets ({@code objective}, {@code score} and
 * {@code display objective}), so the server scoreboard used by {@code /scoreboard} is
 * never touched. The sidebar shows at most 15 lines, numbers are hidden, and the line
 * text itself carries the values.</p>
 */
public final class SidebarManager {
	private static final SidebarManager INSTANCE = new SidebarManager();

	private static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
			.create();
	private static final String DIRECTORY = "oxycarbide/scoreboard";
	private static final String FILE_NAME = "scoreboard.json";
	private static final int MAX_LINES = 15;
	/** Invisible prefix so two lines can hold the same text. */
	private static final String OWNER_PREFIX = "\u00a7r";

	private SidebarSettings settings = new SidebarSettings();
	private Path file;
	private final Scoreboard model = new Scoreboard();
	private final Map<UUID, View> views = new HashMap<>();
	private final Map<UUID, Integer> joinOrder = new HashMap<>();
	private int nextJoinOrder = 1;
	private int nextObjective = 1;
	private int ticks;

	private SidebarManager() {
	}

	public static SidebarManager get() {
		return INSTANCE;
	}

	public SidebarSettings settings() {
		return settings;
	}

	public Path file() {
		return file;
	}

	/** Re-reads the JSON file and forces every open sidebar to be redrawn. */
	public void load() {
		Path directory = FabricLoader.getInstance().getConfigDir().resolve(DIRECTORY);
		file = directory.resolve(FILE_NAME);
		SidebarSettings loaded = new SidebarSettings();

		try {
			Files.createDirectories(directory);

			if (!Files.exists(file)) {
				Files.writeString(file, EXAMPLE, StandardCharsets.UTF_8);
			}

			loaded = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), SidebarSettings.class);

			if (loaded == null) {
				loaded = new SidebarSettings();
			}
		} catch (IOException | JsonSyntaxException e) {
			Oxycarbide.LOGGER.warn("Failed to read the sidebar file {}", file, e);
		}

		loaded.normalize();
		settings = loaded;
		views.values().forEach(View::markDirty);
	}

	/** Called once per server tick. */
	public void tick(MinecraftServer server) {
		if (!OxycarbideConfig.get().isSidebarEnabled()) {
			if (!views.isEmpty()) {
				clear(server);
			}

			return;
		}

		if (++ticks < settings.updateTicks) {
			return;
		}

		ticks = 0;

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			render(server, player);
		}
	}

	/** Remembers the join order used by {@code sort: join} and draws the sidebar at once. */
	public void onJoin(ServerPlayer player) {
		joinOrder.put(player.getUUID(), nextJoinOrder++);

		if (!OxycarbideConfig.get().isSidebarEnabled()) {
			return;
		}

		MinecraftServer server = player.level().getServer();

		if (server != null) {
			render(server, player);
		}
	}

	public void onQuit(ServerPlayer player) {
		joinOrder.remove(player.getUUID());
		views.remove(player.getUUID());
	}

	/** Removes every sidebar, used when the feature is switched off. */
	public void clear(MinecraftServer server) {
		for (Map.Entry<UUID, View> entry : views.entrySet()) {
			View view = entry.getValue();
			ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());

			if (player != null && view.objective != null) {
				send(player, new ClientboundSetObjectivePacket(view.objective, 1));
			}
		}

		views.clear();
	}

	public void clearState() {
		views.clear();
		joinOrder.clear();
	}

	private void render(MinecraftServer server, ServerPlayer viewer) {
		if (viewer.connection == null) {
			return;
		}

		String titleText = textLine(settings.title, server, viewer);
		List<String> body = buildBody(server, viewer);
		View view = views.get(viewer.getUUID());

		if (view == null) {
			view = new View("oxy" + nextObjective++);
			views.put(viewer.getUUID(), view);
		}

		if (view.objective == null) {
			view.objective = new Objective(model, view.name, ObjectiveCriteria.DUMMY,
					SidebarText.parse(titleText), ObjectiveCriteria.RenderType.INTEGER, false,
					BlankFormat.INSTANCE);
			view.title = titleText;
			view.lines.clear();
			view.sent = 0;
			send(viewer, new ClientboundSetObjectivePacket(view.objective, 0));
		} else if (!titleText.equals(view.title)) {
			view.objective.setDisplayName(SidebarText.parse(titleText));
			view.title = titleText;
			send(viewer, new ClientboundSetObjectivePacket(view.objective, 2));
		}

		for (int index = 0; index < body.size(); index++) {
			String text = body.get(index);
			String previous = view.lines.get(index);

			if (previous != null && previous.equals(text)) {
				continue;
			}

			boolean sent = send(viewer, new ClientboundSetScorePacket(owner(index), view.name,
					MAX_LINES - index, Optional.of(SidebarText.parse(text)), Optional.empty()));

			if (sent) {
				view.lines.put(index, text);
			}
		}

		for (int index = body.size(); index < view.sent; index++) {
			send(viewer, new ClientboundResetScorePacket(owner(index), view.name));
		}

		view.lines.keySet().removeIf(index -> index >= body.size());
		view.sent = body.size();
		// Keep the display slot on our objective; anything else the server sends is replaced.
		send(viewer, new ClientboundSetDisplayObjectivePacket(DisplaySlot.SIDEBAR, view.objective));
	}

	private List<String> buildBody(MinecraftServer server, ServerPlayer viewer) {
		List<String> lines = new ArrayList<>();

		for (SidebarLine line : settings.lines) {
			if (lines.size() >= MAX_LINES) {
				break;
			}

			switch (line.typeLower()) {
				case "title", "bold" -> lines.add("&l" + textLine(line.text, server, viewer));
				case "text" -> lines.add(textLine(line.text, server, viewer));
				case "money" -> {
					boolean includeOffline = offlineScoreboard();
					addSource(lines, line, server, viewer,
							() -> moneyEntries(server, includeOffline),
							TreasuryManager.get().balance(viewer.getUUID()),
							value -> Long.toString(value));
				}
				case "objective" -> {
					String objective = line.objective == null ? "" : line.objective.trim();
					boolean includeOffline = offlineScoreboard();
					addSource(lines, line, server, viewer,
							() -> objectiveEntries(server, objective, includeOffline),
							objectiveValue(server, objective, viewer),
							value -> Long.toString(value));
				}
				case "stat" -> {
					Stat<Identifier> stat = statOf(line);
					addSource(lines, line, server, viewer,
							() -> onlineEntries(server, player -> player.getStats().getValue(stat)),
							viewer.getStats().getValue(stat),
							value -> stat.format((int) value));
				}
				case "custom" -> {
					String counter = line.counter;
					boolean includeOffline = offlineScoreboard();
					addSource(lines, line, server, viewer,
							() -> customEntries(server, counter, includeOffline),
							CounterManager.get().get(counter, viewer.getUUID()),
							value -> Long.toString(value));
				}
				default -> {
					// Unknown type: skip the line instead of breaking the whole sidebar.
				}
			}
		}

		return lines;
	}

	/**
	 * Adds a ranked (or, with {@code self}, a single) line for one score source. The
	 * entries are built lazily, so a {@code self} line never collects a whole list.
	 */
	private void addSource(List<String> out, SidebarLine line, MinecraftServer server, ServerPlayer viewer,
			Supplier<List<Entry>> entries, long selfValue, LongFunction<String> formatValue) {
		if (line.self) {
			out.add(listLine(line.format, server, viewer.getScoreboardName(), "1", formatValue.apply(selfValue)));
			return;
		}

		List<Entry> all = entries.get();

		if (all.isEmpty()) {
			return;
		}

		List<Entry> sorted = new ArrayList<>(all);

		switch (sortOf(line)) {
			case "asc" -> sorted.sort(Comparator.<Entry>comparingLong(Entry::value));
			case "join" -> sorted.sort(Comparator.<Entry>comparingInt(Entry::joinRank));
			default -> sorted.sort(Comparator.<Entry>comparingLong(Entry::value).reversed());
		}

		int limit = line.players >= 0 ? line.players : settings.players;
		int count = limit <= 0 ? sorted.size() : Math.min(limit, sorted.size());
		count = Math.min(count, MAX_LINES - out.size());

		for (int index = 0; index < count; index++) {
			Entry entry = sorted.get(index);
			out.add(listLine(line.format, server, entry.name(),
					Integer.toString(index + 1), formatValue.apply(entry.value())));
		}
	}

	/** Only the players that are online right now. */
	private List<Entry> onlineEntries(MinecraftServer server, ToLongFunction<ServerPlayer> values) {
		List<Entry> entries = new ArrayList<>();

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			entries.add(new Entry(player.getScoreboardName(), values.applyAsLong(player),
					joinOrder.getOrDefault(player.getUUID(), Integer.MAX_VALUE)));
		}

		return entries;
	}

	/** Coin accounts; offline players only when the rule allows it. */
	private List<Entry> moneyEntries(MinecraftServer server, boolean includeOffline) {
		if (!includeOffline) {
			return onlineEntries(server, player -> TreasuryManager.get().balance(player.getUUID()));
		}

		List<Entry> entries = new ArrayList<>();

		for (TreasuryManager.Entry account : TreasuryManager.get().all()) {
			UUID player;

			try {
				player = UUID.fromString(account.uuid);
			} catch (IllegalArgumentException e) {
				continue;
			}

			String name = account.name == null || account.name.isBlank() ? account.uuid : account.name;
			entries.add(new Entry(name, account.balance, joinOrder.getOrDefault(player, Integer.MAX_VALUE)));
		}

		return entries;
	}

	/** Oxycarbide counters; offline players only when the rule allows it. */
	private List<Entry> customEntries(MinecraftServer server, String counter, boolean includeOffline) {
		if (!includeOffline) {
			return onlineEntries(server, player -> CounterManager.get().get(counter, player.getUUID()));
		}

		List<Entry> entries = new ArrayList<>();

		for (CounterManager.Entry entry : CounterManager.get().entries(counter)) {
			entries.add(new Entry(entry.name(), entry.value(),
					joinOrder.getOrDefault(entry.player(), Integer.MAX_VALUE)));
		}

		return entries;
	}

	/** Objective scores; with the rule on, every player the objective knows is listed. */
	private List<Entry> objectiveEntries(MinecraftServer server, String objective, boolean includeOffline) {
		Objective target = objectiveOf(server, objective);

		if (target == null) {
			return List.of();
		}

		if (!includeOffline) {
			return onlineEntries(server, player -> objectiveValue(server, target, player));
		}

		Map<String, Integer> ranks = new HashMap<>();

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			ranks.put(player.getScoreboardName(), joinOrder.getOrDefault(player.getUUID(), Integer.MAX_VALUE));
		}

		List<Entry> entries = new ArrayList<>();

		for (Object tracked : server.getScoreboard().getTrackedPlayers()) {
			// Older versions return names, newer ones return ScoreHolders; both work here.
			String name = tracked instanceof ScoreHolder holder ? holder.getScoreboardName() : String.valueOf(tracked);
			Object2IntMap<Objective> scores = server.getScoreboard()
					.listPlayerScores(ScoreHolder.forNameOnly(name));

			if (!scores.containsKey(target)) {
				continue;
			}

			entries.add(new Entry(name, scores.getOrDefault(target, 0),
					ranks.getOrDefault(name, Integer.MAX_VALUE)));
		}

		return entries;
	}

	/** The rule that decides whether offline players show up in the lists. */
	private static boolean offlineScoreboard() {
		return OxycarbideConfig.get().isDisplayOfflinePlayerScoreboard();
	}

	/** Resolves an objective name, or {@code null} when the objective does not exist. */
	private static Objective objectiveOf(MinecraftServer server, String objective) {
		String name = objective == null ? "" : objective.trim();
		return name.isEmpty() ? null : server.getScoreboard().getObjective(name);
	}

	private static long objectiveValue(MinecraftServer server, String objective, ServerPlayer player) {
		Objective target = objectiveOf(server, objective);
		return target == null ? 0L : objectiveValue(server, target, player);
	}

	private static long objectiveValue(MinecraftServer server, Objective objective, ServerPlayer player) {
		return server.getScoreboard()
				.listPlayerScores(ScoreHolder.forNameOnly(player.getScoreboardName()))
				.getOrDefault(objective, 0);
	}

	private static Stat<Identifier> statOf(SidebarLine line) {
		String name = line.stat == null || line.stat.isBlank() ? "minecraft:deaths" : line.stat.trim();
		Identifier id = Identifier.tryParse(name);

		if (id == null) {
			id = Identifier.parse("minecraft:deaths");
		}

		return Stats.CUSTOM.get(id);
	}

	private String sortOf(SidebarLine line) {
		String value = line.sortLower();
		return value.isEmpty() ? settings.sort.toLowerCase(Locale.ROOT) : value;
	}

	/** A text line: the viewer's own name, the player count and the max player count. */
	private static String textLine(String template, MinecraftServer server, ServerPlayer viewer) {
		return SidebarText.fill(template,
				"online", Integer.toString(server.getPlayerList().getPlayerCount()),
				"max", Integer.toString(server.getMaxPlayers()),
				"player", viewer.getScoreboardName());
	}

	/** A score line: rank, the listed player and the value. */
	private static String listLine(String template, MinecraftServer server, String player, String rank, String value) {
		return SidebarText.fill(template,
				"rank", rank,
				"player", player,
				"value", value,
				"online", Integer.toString(server.getPlayerList().getPlayerCount()),
				"max", Integer.toString(server.getMaxPlayers()));
	}

	private static String owner(int index) {
		return OWNER_PREFIX + index;
	}

	private static boolean send(ServerPlayer player, Packet<?> packet) {
		if (player.connection == null) {
			return false;
		}

		player.connection.send(packet);
		return true;
	}

	/** One entry of a ranked list; the join rank orders online players by join time. */
	private record Entry(String name, long value, int joinRank) {
	}

	/** One player's sidebar: its objective and the text currently on every line. */
	private static final class View {
		private final String name;
		private final Map<Integer, String> lines = new HashMap<>();
		private Objective objective;
		private String title = "";
		private int sent;

		private View(String name) {
			this.name = name;
		}

		/**
		 * Forces a full redraw on the next refresh. {@link #sent} is kept so that lines
		 * dropped by a reloaded layout are still removed from the client.
		 */
		private void markDirty() {
			this.title = "";
			this.lines.clear();
		}
	}


	private static final String EXAMPLE = """
			{
			  "title": "&6&l服务器",
			  "update_ticks": 20,
			  "players": 5,
			  "sort": "desc",
			  "lines": [
			    { "type": "text", "text": "&7在线: &f%online%/%max%" },
			    { "type": "title", "text": "&e金币榜" },
			    { "type": "money", "players": 3, "format": "&7%rank%. &f%player% &e%value%" },
			    { "type": "text", "text": "" },
			    { "type": "title", "text": "&e我的信息" },
			    { "type": "text", "text": "&7玩家: &f%player%" },
			    { "type": "money", "self": true, "format": "&7金币: &e%value%" },
			    { "type": "custom", "self": true, "counter": "points", "format": "&7积分: &b%value%" },
			    { "type": "stat", "self": true, "stat": "minecraft:deaths", "format": "&7死亡: &f%value%" }
			  ]
			}
			""";
}
